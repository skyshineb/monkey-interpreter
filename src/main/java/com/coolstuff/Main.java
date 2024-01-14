package com.coolstuff;

import com.coolstuff.repl.REPL;

public class Main {
    public static void main(String[] args) {
        System.out.println("Welcome to the Monkey programming language!");
        System.out.println("Feel free by typing commands\n");
        REPL repl = new REPL();
        repl.start();
    }

}