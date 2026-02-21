# Monkey programming language in Java
_Based on [interpreterbook.com](https://interpreterbook.com)_

The base version of interpreter is done.

### Features of Monkey:
- C-like syntax  
- variable bindings  
- integers, booleans, strings, null  
- arithmetic + comparison expressions  
- logical `&&` / `||` with short-circuiting  
- `if` / `else` / `else if`  
- `while` loops with `break` / `continue`  
- built-in functions  
- first-class and higher-order functions  
- closures  
- a string data structure  
- an array data structure  
- a hash data structure  
- structured runtime errors with source positions + call stack  

#### Progress:
| Feature                | Status      | When       |
|------------------------|-------------|------------|
| Lexing                 | done ⭐      | 14.01.2024 |
| Parsing                | done ⭐      | 26.07.2024 |
| Evaluation             | done ⭐      | 04.04.2025 |
| Extension              | done ⭐      | 03.11.2025 |
| REPL enhancements      | done 🤖✨    | 17.02.2026 |
| Language extension pt1 | done 🤖✨    | 21.02.2026 |

#### REPL

- The interpreter runs in a **stateful REPL session**: variables and functions declared in earlier commands stay available in later commands within the same session.
- **Multiline input** is supported. Incomplete constructs (`if/else`, function literals, blocks, etc.) are accumulated until the input is syntactically complete.
- Meta-commands are accepted only when the multiline buffer is empty.
- You can finish the current REPL session with service commands:
  - `:quit`
  - `:exit`
- Debugging commands:
  - `:help` shows available commands
  - `:tokens [input]` tokenizes inline input or the next complete input
  - `:ast [input]` shows `Program.string()` for inline input or the next complete input
  - `:env` prints current environment bindings

#### CLI script mode

You can run Monkey source files without entering REPL:

```bash
java -jar target/monkey-1.0-SNAPSHOT.jar run path/to/program.monkey
java -jar target/monkey-1.0-SNAPSHOT.jar --tokens path/to/program.monkey
java -jar target/monkey-1.0-SNAPSHOT.jar --ast path/to/program.monkey
```

CLI errors are deterministic and concise:
- parser failures: `Parse errors in <path>:` followed by one `- ...` line per parser error
- runtime failures: `Runtime error in <path>:` followed by the formatted runtime error block
- file loading failures: `<reason>: <path>` (for example `File not found: <path>`)

#### Build and test

Run from the project root:

- Build project JAR:

  ```bash
  mvn clean package
  ```

- Run unit tests:

  ```bash
  mvn test
  ```

- Start REPL from built artifact:

  ```bash
  java -jar target/monkey-1.0.jar
  ```


#### To-Do:
- Full Unicode support in syntax and strings
- Comment syntax for source code
- Stdlib module system / built-ins cleanup
