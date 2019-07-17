package com.github.micutio.jynk;

import com.github.micutio.jynk.ast.AstPrinter;
import com.github.micutio.jynk.ast.Expr;
import com.github.micutio.jynk.lexing.Scanner;
import com.github.micutio.jynk.lexing.Token;
import com.github.micutio.jynk.lexing.TokenType;
import com.github.micutio.jynk.parsing.Parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Java interpreter for the programming language `Ynk`.
 * Uses UNIX sysexists.h exit codes.
 * @author michael
 */
public class JYnk {

    static boolean hadError;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jynk [script]");
            System.exit(64);
        } else if (args.length == 1) {
            System.out.println("parsing source file " + args[0]);
             runFile(args[0]);
        } else {
            System.out.println("launching ynk prompt...");
             runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        // Indicate an error n the exit code.
        if (hadError) System.exit(65);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        while (true) {
            System.out.println("> ");
            run(reader.readLine());

            // clear error flag from any side effects
            hadError = false;
        }
    }

    private static void run(String sourceCode) {
        Scanner scanner = new Scanner(sourceCode);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        Expr expression = parser.parse();

        // stop if there was a syntax error
        if (hadError) return;

        System.out.println(new AstPrinter().print(expression));
    }

    public static void error(int line, String message) {
        report(line, "", message);
    }

    public static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

}
