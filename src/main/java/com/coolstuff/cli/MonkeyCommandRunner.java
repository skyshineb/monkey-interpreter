package com.coolstuff.cli;

import com.coolstuff.repl.REPL;

import java.io.InputStream;
import java.io.PrintStream;

public class MonkeyCommandRunner {

    private final MonkeyCliRunner cliRunner;

    public MonkeyCommandRunner() {
        this(new MonkeyCliRunner());
    }

    MonkeyCommandRunner(MonkeyCliRunner cliRunner) {
        this.cliRunner = cliRunner;
    }

    public int run(String[] args, InputStream in, PrintStream out, PrintStream err) {
        if (args.length == 0) {
            out.println("Welcome to the Monkey programming language!");
            out.println("Feel free by typing commands");
            out.println();
            new REPL(in, out).start();
            return 0;
        }

        return cliRunner.run(args, out, err);
    }
}
