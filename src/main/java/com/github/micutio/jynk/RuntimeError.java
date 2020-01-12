package com.github.micutio.jynk;

import com.github.micutio.jynk.lexing.Token;

public class RuntimeError extends RuntimeException {
    final Token token;

    public RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }
}
