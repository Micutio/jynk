package com.github.micutio.jynk.interpreter;

import com.github.micutio.jynk.RuntimeError;
import com.github.micutio.jynk.Ynk;
import com.github.micutio.jynk.ast.Expr;
import com.github.micutio.jynk.ast.Stmt;
import com.github.micutio.jynk.lexing.Token;
import com.github.micutio.jynk.parsing.Environment;
import java.util.List;

/**
 * Post-order traversal. Evaluate all children first, before evaluating the expr/stmt.
 */
public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    private Environment environment = new Environment();

    public void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement: statements) {
                execute(statement);
            }
        } catch (RuntimeError err) {
            Ynk.runtimeError(err);
        }
    }

    private String stringify(Object object) {
        if (object == null)
            return "nil";

        // Hack. Work around Java adding .0 to integer-valued doubles.
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case MINUS: checkNumberOperands(expr.operator, left, right); return (double) left - (double) right;
            case SLASH: checkNumberOperands(expr.operator, left, right); return (double) left / (double) right;
            case STAR: checkNumberOperands(expr.operator, left, right); return (double) left * (double) right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }
                if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }
                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
            case GREATER: checkNumberOperands(expr.operator, left, right); return (double) left > (double) right;
            case GREATER_EQUAL: checkNumberOperands(expr.operator, left, right); return (double) left >= (double) right;
            case LESS: checkNumberOperands(expr.operator, left, right); return (double) left < (double) right;
            case LESS_EQUAL: checkNumberOperands(expr.operator, left, right); return (double) left <= (double) right;
            case BANG_EQUAL: return !isEqual(left, right);
            case EQUAL_EQUAL: return isEqual(left, right);
        }

        // unreachable
        return null;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG: return isTruthy(right);
            case MINUS: checkNumberOperand(expr.operator, right); return -(double) right;
        }

        // unreachable
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Stmt statement: statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);

        environment.assign(expr.name, value);
        return value;
    }

    private boolean isEqual(Object a, Object b) {
        // nil is only equal to nil
        if (a == null && b == null)
            return true;
        if (a == null)
            return false;
        return a.equals(b);
    }

    private boolean isTruthy(Object object) {
        if (object == null)
            return false;
        if (object instanceof Boolean)
            return (boolean) object;
        return true;
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double)
            return;

        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double)
            return;

        throw new RuntimeError(operator, "Operands must be numbers.");
    }
}
