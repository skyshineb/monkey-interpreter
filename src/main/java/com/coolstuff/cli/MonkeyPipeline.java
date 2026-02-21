package com.coolstuff.cli;

import com.coolstuff.ast.Program;
import com.coolstuff.evaluator.EvaluationException;
import com.coolstuff.evaluator.Evaluator;
import com.coolstuff.evaluator.object.MonkeyObject;
import com.coolstuff.lexer.Lexer;
import com.coolstuff.parser.Parser;
import com.coolstuff.token.Token;
import com.coolstuff.token.TokenType;

import java.util.ArrayList;
import java.util.List;

public class MonkeyPipeline {

    public List<String> tokenStream(String input) {
        var lines = new ArrayList<String>();
        var lexer = new Lexer(input);
        Token token;
        do {
            token = lexer.nextToken();
            lines.add("%s('%s') @ %s".formatted(token.type(), token.token(), token.position()));
        } while (token.type() != TokenType.EOF);

        return lines;
    }

    public ParseResult parseProgram(String input) {
        var parser = new Parser(new Lexer(input));
        var program = parser.parseProgram();
        return new ParseResult(program, List.copyOf(parser.getErrors()));
    }

    public EvaluationResult evaluate(String input, Evaluator evaluator) {
        var parseResult = parseProgram(input);
        if (!parseResult.errors().isEmpty()) {
            return EvaluationResult.withParseErrors(parseResult.errors());
        }

        try {
            var evaluated = evaluator.eval(parseResult.program());
            return EvaluationResult.success(evaluated);
        } catch (EvaluationException exc) {
            return EvaluationResult.withEvaluationError(exc);
        }
    }

    public record ParseResult(Program program, List<String> errors) {
    }

    public record EvaluationResult(MonkeyObject<?> value, List<String> parseErrors, EvaluationException evaluationException) {
        public static EvaluationResult success(MonkeyObject<?> value) {
            return new EvaluationResult(value, List.of(), null);
        }

        public static EvaluationResult withParseErrors(List<String> parseErrors) {
            return new EvaluationResult(null, parseErrors, null);
        }

        public static EvaluationResult withEvaluationError(EvaluationException evaluationException) {
            return new EvaluationResult(null, List.of(), evaluationException);
        }

        public boolean hasParseErrors() {
            return !parseErrors.isEmpty();
        }

        public boolean hasEvaluationError() {
            return evaluationException != null;
        }
    }
}
