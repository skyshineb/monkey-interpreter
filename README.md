# Monkey programming language in Java
_Based on [interpreterbook.com](https://interpreterbook.com)_

The base version of interpreter is done.

### Features of Monkey:
- C-like syntax  
- variable bindings  
- integers and booleans  
- arithmetic expressions  
- built-in functions  
- first-class and higher-order functions  
- closures  
- a string data structure  
- an array data structure  
- a hash data structure  

#### Progress:
| Feature           | Status      | When       |
|-------------------|-------------|------------|
| Lexing            | done ⭐      | 14.01.2024 |
| Parsing           | done ⭐      | 26.07.2024 |
| Evaluation        | done ⭐      | 04.04.2025 |
| Extension         | done ⭐      | 03.11.2025 |
| REPL enhancements | done 🤖✨    | 17.02.2026 |

#### REPL

- The interpreter runs in a **stateful REPL session**: variables and functions declared in earlier commands stay available in later commands within the same session.
- **Multiline input** is supported. Incomplete constructs (`if/else`, function literals, blocks, etc.) are accumulated until the input is syntactically complete.
- You can finish the current REPL session with service commands:
  - `:quit`
  - `:exit`

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
- Full unicode support in syntax
- Show on which token the error happened
