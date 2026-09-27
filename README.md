# Confusion Atoms Refactoring Plugin for IntelliJ IDEA

An IntelliJ IDEA extension developed as part of the research project **"To Refactor or Not to Refactor Atoms of Confusion? Evidence from a Scoping Review, Bytecode-Level Analysis, and Tool Support"**.

The plugin detects and refactors *Atoms of Confusion* (ACs) in Java source code using PSI-based semantic analysis via the JetBrains Plugin SDK.

## Overview

Atoms of Confusion are the smallest syntactic code patterns that are formally correct but commonly misinterpreted by developers. This plugin provides IDE-assisted detection and refactoring of nine selected ACs directly within IntelliJ IDEA, offering real-time warnings and quick fixes without requiring external tools.

## Requirements

| Requirement | Version             |
|---|---------------------|
| IntelliJ IDEA (Community or Ultimate) | 2024.2+             |
| JDK | 17+                 |
| Gradle | managed via wrapper |

## Installation

### Run in a sandbox (recommended)

```bash
git clone <repository-url>
cd aoc-refactor
./gradlew runIde
```

The IntelliJ sandbox opens automatically with the plugin installed.

### Build from source

```bash
./gradlew buildPlugin
```

The generated `.zip` is placed in `build/distributions/`. Install it via:

```
Settings → Plugins → ⚙️ → Install Plugin from Disk
```

## Project Structure

```text
aoc-refactor/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── src/main/
    ├── java/com/anon/aocrefactor/
    │   ├── inspection/
    │   │   ├── ConditionalOperatorInspection.java
    │   │   ├── InfixOperatorPrecedenceInspection.java
    │   │   ├── PreIncrementDecrementInspection.java
    │   │   ├── PostIncrementDecrementInspection.java
    │   │   ├── ArithmeticAsLogicInspection.java
    │   │   ├── TypeConversionInspection.java
    │   │   ├── OmittedCurlyBracesInspection.java
    │   │   ├── LogicAsControlFlowInspection.java
    │   │   └── ChangeOfLiteralEncodingInspection.java
    │   ├── quickfix/
    │   │   ├── ReplaceConditionalOperatorQuickFix.java
    │   │   ├── WrapWithParenthesesQuickFix.java
    │   │   ├── ReplacePreIncrementQuickFix.java
    │   │   ├── ReplacePostIncrementQuickFix.java
    │   │   ├── ReplaceArithmeticAsLogicQuickFix.java
    │   │   ├── ReplaceTypeConversionQuickFix.java
    │   │   ├── AddCurlyBracesQuickFix.java
    │   │   ├── ReplaceLogicAsControlFlow.java
    │   │   └── ReplaceChangeOfLiteralEncoding.java
    │   └── util/
    │       ├── ExpressionUtils.java
    │       ├── FixUtils.java
    │       └── OperatorUtils.java
    └── resources/META-INF/
        ├── plugin.xml
        └── pluginIcon.svg
```

## Implementation

The plugin uses the **JetBrains Plugin SDK**'s Local Inspections mechanism over the **PSI (Program Structure Interface)** — the semantic tree IntelliJ uses internally to represent Java source code.

Each atom is implemented as an **Inspection / QuickFix** pair:

- The **Inspection** traverses the PSI tree via the Visitor pattern, identifying nodes matching the atom's syntactic pattern.
- The **QuickFix** applies the refactoring directly on the tree (no textual manipulation), so the transformation respects the program's semantic structure.

Shared logic lives in the `util` package and is reused across inspections:

| Class | Responsibility |
|---|---|
| `OperatorUtils` | Operator classification, operand extraction |
| `ExpressionUtils` | Literal parsing, type checks, expression unwrapping |
| `FixUtils` | Shared quick-fix helpers |

## Implemented Atoms

### 1. Conditional Operator

Detects every occurrence of the ternary operator `?:` and offers a refactoring to an explicit `if-else` structure. Covered contexts: variable assignment, method parameter, return statement.

```java
// Before
int b = a == 3 ? 2 : 1;

// After
int b;
if (a == 3) { b = 2; } else { b = 1; }
```

### 2. Infix Operator Precedence

Detects arithmetic expressions where `*`, `/` or `%` appear under `+` or `-` without explicit parentheses, and logical expressions where `&&` and `||` are mixed without parentheses. String concatenation is excluded from detection.

```java
// Before
int x = a + b * c;
boolean l = p || q && r;

// After
int x = a + (b * c);
boolean l = p || (q && r);
```

### 3. Pre-Increment / Pre-Decrement

Detects the prefix `++` / `--` operator in contexts where it may cause confusion: variable assignment, binary operation, method parameter, array index, and return statement. The increment is moved to its own line **before** the parent statement.

```java
// Before
int b = ++a;

// After
a++;
int b = a;
```

### 4. Post-Increment / Post-Decrement

Same contexts as above, for the suffix operator. The increment is moved **after** the parent statement, preserving the original value semantics.

```java
// Before
int b = a++;

// After
int b = a;
a++;
```

### 5. Arithmetic as Logic

Detects arithmetic expressions compared to zero via `==` or `!=` and replaces them with explicit logical equivalents.

| Before | After |
|---|---|
| `a * b == 0` | `a == 0 OR b == 0` |
| `a * b != 0` | `a != 0 AND b != 0` |
| `a - b == 0` | `a == b` |
| `a - b != 0` | `a != b` |
| `a + b == 0` | `a == -b` |

> Note: `OR` / `AND` above represent `\|\|` and `&&` in the generated code.

### 6. Type Conversion

Detects explicit narrowing conversions between primitive types performed without treatment APIs or the modulo operator.

| From | To |
|---|---|
| `short` | `byte`, `char` |
| `char` | `byte`, `short` |
| `int` | `byte`, `short`, `char` |
| `long` | `byte`, `short`, `char`, `int` |
| `float` | `byte`, `short`, `char`, `int`, `long` |
| `double` | `byte`, `short`, `char`, `int`, `long`, `float` |

The QuickFix inserts a warning comment on the preceding line instead of altering behavior:

```java
// Before
int a = (int) 1.99f;

// After (QuickFix applied)
int a = ( int ) Math . floor (1.99f);
```

Excluded: casts whose operand contains a method invocation (possible API usage) or a modulo operation (explicit data treatment). For literal operands, detection triggers only when the value is outside the target type's representable range.

### 7. Omitted Curly Braces

Detects single-statement bodies of `if` / `else`, `while`, `do-while`, `for`, and `for-each` blocks not enclosed in `{ }`, which can cause ambiguity about block membership. The QuickFix wraps the body in braces.

```java
// Before
if (a > 0)
    doSomething();

// After
if (a > 0) {
    doSomething();
}
```

`else if` chains are not flagged twice (the nested `if` is visited independently). Terminal statements (`return`, `break`, `continue`, `throw`) and empty statements (e.g. `if (a);`) are excluded.

### 8. Logic as Control Flow

Detects logical expressions (`&&`, `||`) where a secondary operand contains a side effect or computation (method call, assignment, or increment/decrement) that only runs due to short-circuit evaluation — meaning the logical operator effectively drives control flow instead of evaluating a boolean condition.

```java
// Before
if (isValid() && update()) { ... }

// After (conceptual)
if (isValid()) {
    if (update()) { ... }
}
```

The warning is reported once per top-level logical expression; nested logical sub-expressions defer to their root to avoid duplicate alerts.

### 9. Change of Literal Encoding

Detects two situations where a numeric literal's base may mislead a reader:

1. An **octal, hexadecimal, or binary** literal used directly in a variable assignment or declaration.
2. A **decimal** literal used as an operand of a bitwise (`&`, `|`, `^`) or shift (`<<`, `>>`, `>>>`) operation, where a non-decimal base would communicate intent better.

```java
// Case 1 — Before
int flag = 0x1F;

// Case 2 — Before
int mask = value & 15;

// After (QuickFix suggestion)
int mask = value & 0xF; // or & 0b1111
```

The QuickFix offers conversion to the encoding(s) most appropriate for the context: decimal, hexadecimal, or binary.

## Technical Approach

`registerProblem` deliberately highlights the **parent node** of the detected expression when the atom's context adds relevant semantic information — e.g. the full binary expression `3 + a++` rather than just `a++`. Each inspection selects the most informative PSI node to highlight based on parent context.

QuickFixes locate the target sub-expression using `PsiTreeUtil.findChildOfType`, which descends the PSI tree recursively from the highlighted node, so the fix works regardless of which node was used as the highlight target.

## Related Work

This plugin is part of the broader research project:

> *To Refactor or Not to Refactor Atoms of Confusion? Evidence from a Scoping Review, Bytecode-Level Analysis, and Tool Support* (Anonymous, 2026)

## License

This project is developed for academic research purposes.
