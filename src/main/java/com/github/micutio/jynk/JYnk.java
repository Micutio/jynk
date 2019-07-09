package com.github.micutio.jynk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Java interpreter for the programming language `ynk`.
 * Uses UNIX sysexists.h exit codes.
 * @author michael
 */
public class JYnk {

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jynk [script]");
            System.exit(64);
        } else if (args.length == 1) {
            System.out.println("parsing source file " + args[0]);
            // runFile(args[0]);
        } else {
            System.out.println("launching ynk prompt...");
            // runPrompt();
        }
    }

}
