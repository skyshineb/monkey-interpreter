# AGENTS.md — monkey-interpreter project guide

This file is the quick-start guide for contributors/agents working in this repository.

## Project overview
- **Name:** `monkey`
- **What it is:** A Java implementation of the Monkey interpreter from *Writing An Interpreter In Go*.
- **Entrypoint:** `com.coolstuff.Main` (starts an interactive REPL).
- **Implementation status:** Lexer, parser, evaluator, and language extensions are implemented (strings, arrays, hashes, built-ins).

## Tech stack and versions
- **Language:** Java
- **Java version:** **21**
  - Set in `maven-compiler-plugin` (`<source>21</source>`, `<target>21</target>`)
  - Also set in Maven properties (`maven.compiler.source/target`)
- **Build system:** Maven (`pom.xml`)
- **Unit test framework:** JUnit 5 (`org.junit.jupiter:junit-jupiter:5.9.3`)
- **Test execution:** Maven Surefire 3.1.2
- **Integration-test plugin configured:** Maven Failsafe 3.1.2 (no integration-test suite currently present)
- **Packaging:** Maven JAR plugin with `Main-Class: com.coolstuff.Main`

## High-level architecture
The interpreter follows a standard pipeline:

1. **REPL input** (`repl.REPL`)
2. **Lexing** (`lexer.Lexer`) → stream of `token.Token`
3. **Parsing** (`parser.Parser`, Pratt parser) → AST rooted at `ast.Program`
4. **Evaluation** (`evaluator.Evaluator`) → runtime values in `evaluator.object.*`
5. **Printed result** (`inspect()` on runtime objects)

### Runtime model
- Runtime values are represented by `MonkeyObject<?>` implementations:
  - Integers, booleans, strings, arrays, hashes, functions, built-ins, null, return wrapper.
- Variable bindings and lexical scope are handled by `evaluator.Environment`.
- Closures work by capturing the environment when a `MonkeyFunction` is created.

## Core source layout
- `src/main/java/com/coolstuff/Main.java`
  - Process entrypoint and welcome text.
- `src/main/java/com/coolstuff/repl/REPL.java`
  - Loop that reads input, parses, evaluates, and prints.
- `src/main/java/com/coolstuff/token/`
  - `Token` + `TokenType` definitions.
- `src/main/java/com/coolstuff/lexer/Lexer.java`
  - Character scanner/tokenizer.
- `src/main/java/com/coolstuff/parser/`
  - Pratt parser and precedence table.
- `src/main/java/com/coolstuff/ast/`
  - AST interfaces + concrete node records/classes.
  - Statement node implementations are under `ast/Nodes/`.
- `src/main/java/com/coolstuff/evaluator/`
  - AST evaluator, environments, built-ins, evaluation errors.
- `src/main/java/com/coolstuff/evaluator/object/`
  - Runtime object type hierarchy.
- `src/test/java/com/coolstuff/`
  - Unit tests for tokenization, AST, parser behavior, and evaluator behavior.

## Implemented Monkey features
- Integers, booleans, strings, null.
- Prefix/infix expressions with precedence.
- `let` bindings and `return`.
- `if` / `else` conditionals.
- First-class functions, function calls, closures.
- Arrays and index access.
- Hash literals and hash indexing.
- Built-ins including: `len`, `first`, `last`, `rest`, `push`, `puts`.

## Build, test, and run
Run commands from repository root.

- **Compile/package:**
  - `mvn clean package`
- **Run tests:**
  - `mvn test`
- **Run the built artifact:**
  - `java -jar target/monkey-1.0-SNAPSHOT.jar`

> Note: `mvn exec:java ...` is not configured via `exec-maven-plugin` in this repository by default.

## Coding style/conventions (follow existing code)
- Package root: `com.coolstuff`.
- Modern Java patterns are used heavily:
  - `record` for immutable data carriers.
  - `switch` expressions (including type-pattern style in evaluator).
  - `var` where readability stays high.
- AST contracts:
  - All AST nodes implement `tokenLiteral()` and `string()`.
  - Marker interfaces distinguish `Statement` vs `Expression`.
- Error strategy:
  - Parser accumulates errors (`Parser#getErrors()`) for reporting.
  - Evaluator throws explicit exceptions (`EvaluationException`, etc.).
- Tests are table-driven in many places using local test-case records.

## Contributor checklist for changes
- Keep changes small and package-local when possible.
- For behavior changes, update/add tests under `src/test/java/com/coolstuff`.
- Preserve existing error-message formats where tests assert exact strings.
- For parser/evaluator edits, validate both successful evaluation and failure cases.

## Known TODOs (from README)
- Improve Unicode support in syntax handling.
- Improve parse error context (e.g., token/location details).
