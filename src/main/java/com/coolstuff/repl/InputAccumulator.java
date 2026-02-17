package com.coolstuff.repl;

class InputAccumulator {

    boolean isInputComplete(String input) {
        int openBraces = 0;
        int openParentheses = 0;
        int openBrackets = 0;
        boolean inString = false;
        boolean escaped = false;

        for (char currentChar : input.toCharArray()) {
            if (inString) {
                if (escaped) {
                    escaped = false;
                    continue;
                }

                if (currentChar == '\\') {
                    escaped = true;
                    continue;
                }

                if (currentChar == '"') {
                    inString = false;
                }
                continue;
            }

            switch (currentChar) {
                case '"' -> inString = true;
                case '{' -> openBraces++;
                case '}' -> {
                    if (openBraces > 0) {
                        openBraces--;
                    }
                }
                case '(' -> openParentheses++;
                case ')' -> {
                    if (openParentheses > 0) {
                        openParentheses--;
                    }
                }
                case '[' -> openBrackets++;
                case ']' -> {
                    if (openBrackets > 0) {
                        openBrackets--;
                    }
                }
                default -> {
                }
            }
        }

        return !inString && openBraces == 0 && openParentheses == 0 && openBrackets == 0;
    }
}
