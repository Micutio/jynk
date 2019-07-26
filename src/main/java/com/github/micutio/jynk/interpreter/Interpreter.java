package com.github.micutio.jynk.interpreter;

import com.github.micutio.jynk.JYnk;
import com.github.micutio.jynk.RuntimeError;
import com.github.micutio.jynk.ast.Expr;
import com.github.micutio.jynk.lexing.Token;

/**
 * Post-order traversal. Evaluate all children first, before evaluating the expr/stmt.
 */
public class Interpreter implements Expr.Visitor<Object> {

    public void interpret(Expr expression) {
        try {
            Object value = evaluate(expression);
            System.out.println(stringify(value));
        } catch (RuntimeError err) {
            JYnk.runtimeError(err);
        }
    }

    private String stringify(Object object) {
        if (object == null) return "nil";

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

    private Object evaluate(Expr expr) {
        return expr.accept(this);
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
