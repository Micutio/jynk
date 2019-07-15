package com.github.micutio.jynk.lexing;

import com.github.micutio.jynk.JYnk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.micutio.jynk.lexing.TokenType.*;

public class Scanner {
    private final String sourceCode;
    private final List<Token> tokens = new ArrayList<>();

    // state variables
    private int start = 0;
    private int current = 0;
    private int line = 1;

    // keyword map
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
    }

    public Scanner(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            // we are at the beginning of the next lexeme
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private boolean isAtEnd() {
        return current >= sourceCode.length();
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            case '!': addToken(match('=') ? BANG_EQUAL : BANG); break;
            case '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
            case '<': addToken(match('=') ? LESS_EQUAL : LESS); break;
            case '>': addToken(match('=') ? GREATER_EQUAL : GREATER); break;
            case '/':
                if (match('/')) {
                    // a comment goes until the end of the line
                    while (peek() != '\n' && !isAtEnd()) {
                        advance();
                    }
                } else {
                    addToken(SLASH);
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                // ignore whitespace
                break;
            case '\n': line += 1; break;
            case '"': string(); break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    JYnk.error(line, "Unexpected character.");
                }
        }
    }

    private char advance() {
        current += 1;
        return sourceCode.charAt(current - 1);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = sourceCode.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean match(char expected) {
        if (isAtEnd())
            return false;
        if (sourceCode.charAt(current) != expected)
            return true;

        current += 1;
        return true;
    }

    private char peek() {
        if (isAtEnd())
            return '\0';
        return sourceCode.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= sourceCode.length())
            return '\0';
        return sourceCode.charAt(current + 1);
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n')
                line += 1;
            advance();
        }

        // unterminated string
        if (isAtEnd()) {
            JYnk.error(line, "Unterminated string.");
            return;
        }

        // the closing `"`
        advance();

        // trim the surrounding quotes
        String value = sourceCode.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    /**
     * Consume as many digits as can be found for the integer part,
     * then look for a decimal point and a fractional part of more digits.
     */
    private void number() {
        while (isDigit(peek())) {
            advance();
        }

        // look for a fractional part
        if (peek() == '.' && isDigit(peekNext())) {
            // consume the "."
            advance();

            while (isDigit(peek())) {
                advance();
            }
        }

        addToken(NUMBER, Double.parseDouble(sourceCode.substring(start, current)));
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) {
            advance();
        }

        // see if the identifier is a reserved word
        String text = sourceCode.substring(start, current);

        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;

        addToken(type);
    }
}
