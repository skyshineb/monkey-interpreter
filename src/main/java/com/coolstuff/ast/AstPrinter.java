package com.coolstuff.ast;

import com.coolstuff.ast.Nodes.BlockStatement;
import com.coolstuff.ast.Nodes.BreakStatement;
import com.coolstuff.ast.Nodes.ContinueStatement;
import com.coolstuff.ast.Nodes.ExpressionStatement;
import com.coolstuff.ast.Nodes.LetStatement;
import com.coolstuff.ast.Nodes.ReturnStatement;
import com.coolstuff.ast.Nodes.WhileStatement;

public final class AstPrinter {
    private static final String INDENT = "  ";

    private AstPrinter() {
    }

    public static String print(Node node) {
        var builder = new StringBuilder();
        appendNode(builder, node, 0);
        return builder.toString();
    }

    private static void appendNode(StringBuilder builder, Node node, int depth) {
        if (node == null) {
            appendLine(builder, depth, "<null>");
            return;
        }

        if (node instanceof Program program) {
            appendLine(builder, depth, "Program");
            for (var statement : program.statements()) {
                appendNode(builder, statement, depth + 1);
            }
            return;
        }

        if (node instanceof LetStatement let) {
            appendLine(builder, depth, "LetStatement");
            appendLabeledChild(builder, depth, "Name", let.name());
            appendLabeledChild(builder, depth, "Value", let.value());
            return;
        }

        if (node instanceof ReturnStatement ret) {
            appendLine(builder, depth, "ReturnStatement");
            appendLabeledChild(builder, depth, "Value", ret.returnValue());
            return;
        }

        if (node instanceof ExpressionStatement expressionStatement) {
            appendLine(builder, depth, "ExpressionStatement");
            appendLabeledChild(builder, depth, "Expression", expressionStatement.expression());
            return;
        }

        if (node instanceof BlockStatement block) {
            appendLine(builder, depth, "BlockStatement");
            for (var statement : block.statements()) {
                appendNode(builder, statement, depth + 1);
            }
            return;
        }

        if (node instanceof WhileStatement whileStatement) {
            appendLine(builder, depth, "WhileStatement");
            appendLabeledChild(builder, depth, "Condition", whileStatement.condition());
            appendLabeledChild(builder, depth, "Body", whileStatement.body());
            return;
        }

        if (node instanceof BreakStatement) {
            appendLine(builder, depth, "BreakStatement");
            return;
        }

        if (node instanceof ContinueStatement) {
            appendLine(builder, depth, "ContinueStatement");
            return;
        }

        if (node instanceof IfExpression ifExpression) {
            appendLine(builder, depth, "IfExpression");
            appendLabeledChild(builder, depth, "Condition", ifExpression.condition());
            appendLabeledChild(builder, depth, "Consequence", ifExpression.consequence());
            if (ifExpression.alternative() != null) {
                appendLabeledChild(builder, depth, "Alternative", ifExpression.alternative());
            }
            return;
        }

        if (node instanceof InfixExpression infix) {
            appendLine(builder, depth, "InfixExpression(%s)".formatted(infix.operator()));
            appendLabeledChild(builder, depth, "Left", infix.left());
            appendLabeledChild(builder, depth, "Right", infix.right());
            return;
        }

        if (node instanceof PrefixExpression prefix) {
            appendLine(builder, depth, "PrefixExpression(%s)".formatted(prefix.operator()));
            appendLabeledChild(builder, depth, "Right", prefix.right());
            return;
        }

        if (node instanceof FunctionLiteral functionLiteral) {
            appendLine(builder, depth, "FunctionLiteral");
            appendLine(builder, depth + 1, "Parameters");
            for (var parameter : functionLiteral.parameters()) {
                appendNode(builder, parameter, depth + 2);
            }
            appendLabeledChild(builder, depth, "Body", functionLiteral.body());
            return;
        }

        if (node instanceof CallExpression callExpression) {
            appendLine(builder, depth, "CallExpression");
            appendLabeledChild(builder, depth, "Function", callExpression.function());
            appendLine(builder, depth + 1, "Arguments");
            for (var argument : callExpression.arguments()) {
                appendNode(builder, argument, depth + 2);
            }
            return;
        }

        if (node instanceof ArrayLiteral arrayLiteral) {
            appendLine(builder, depth, "ArrayLiteral");
            for (var element : arrayLiteral.elements()) {
                appendNode(builder, element, depth + 1);
            }
            return;
        }

        if (node instanceof HashLiteral hashLiteral) {
            appendLine(builder, depth, "HashLiteral");
            var index = 0;
            for (var pair : hashLiteral.pairs()) {
                appendLine(builder, depth + 1, "Pair[%d]".formatted(index));
                appendLabeledChild(builder, depth + 1, "Key", pair.key());
                appendLabeledChild(builder, depth + 1, "Value", pair.value());
                index++;
            }
            return;
        }

        if (node instanceof IndexExpression indexExpression) {
            appendLine(builder, depth, "IndexExpression");
            appendLabeledChild(builder, depth, "Left", indexExpression.left());
            appendLabeledChild(builder, depth, "Index", indexExpression.index());
            return;
        }

        if (node instanceof IdentifierExpression identifier) {
            appendLine(builder, depth, "Identifier(%s)".formatted(identifier.value()));
            return;
        }

        if (node instanceof IntegerLiteralExpression integerLiteral) {
            appendLine(builder, depth, "IntegerLiteral(%d)".formatted(integerLiteral.value()));
            return;
        }

        if (node instanceof BooleanExpression booleanExpression) {
            appendLine(builder, depth, "BooleanLiteral(%s)".formatted(booleanExpression.value()));
            return;
        }

        if (node instanceof StringLiteralExpression stringLiteralExpression) {
            appendLine(builder, depth, "StringLiteral(\"%s\")".formatted(stringLiteralExpression.value()));
            return;
        }

        appendLine(builder, depth, "%s(%s)".formatted(node.getClass().getSimpleName(), node.string()));
    }

    private static void appendLabeledChild(StringBuilder builder, int depth, String label, Node child) {
        appendLine(builder, depth + 1, label);
        appendNode(builder, child, depth + 2);
    }

    private static void appendLine(StringBuilder builder, int depth, String text) {
        builder.append(INDENT.repeat(Math.max(0, depth)));
        builder.append(text);
        builder.append(System.lineSeparator());
    }
}
