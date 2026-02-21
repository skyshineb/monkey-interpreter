package com.coolstuff;

import com.coolstuff.cli.MonkeyCommandRunner;

public class Main {
    public static void main(String[] args) {
        var exitCode = new MonkeyCommandRunner().run(args, System.in, System.out, System.err);
        if (args.length > 0) {
            System.exit(exitCode);
        }
    }
}
