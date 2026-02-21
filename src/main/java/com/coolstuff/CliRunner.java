package com.coolstuff;

import com.coolstuff.cli.MonkeyCliRunner;

import java.io.PrintStream;

public class CliRunner {

    private final MonkeyCliRunner runner;

    public CliRunner() {
        this.runner = new MonkeyCliRunner();
    }

    public int run(String[] args, PrintStream out, PrintStream err) {
        return runner.run(args, out, err);
    }

    public static void printUsage(PrintStream err) {
        MonkeyCliRunner.printUsage(err);
    }
}
