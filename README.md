# Confusion Atoms Refactoring Plugin for IntelliJ IDEA

Confusion Atoms Refactoring is an IntelliJ IDEA plugin that detects Java code patterns known as atoms of confusion and offers quick fixes to rewrite them in a clearer form.

Building upon the foundational work on Atoms of Confusion by Gopstein et al., the empirical studies by Langhout & Aniche, and the inspection capabilities of the [Bohr inspection tool](https://github.com/wendellmfm/bohr) made by Wendell Mendes, this plugin brings automated detection and refactoring of confusion-inducing patterns directly into the developer's IDE workflow. It leverages PSI-based semantic analysis via the JetBrains Plugin SDK to identify and transform complex Java construct patterns into clear, maintainable code.

## Overview

Atoms of Confusion (ACs) are the smallest syntactic code patterns that—despite being syntactically valid and semantically correct—are frequently misinterpreted by developers, leading to cognitive overload and subtle bugs.

This plugin adapts and extends the detection heuristics of [Bohr](https://github.com/wendellmfm/bohr) into a native IntelliJ IDEA tool. By providing real-time code inspections (`LocalInspectionTool`) and automated quick fixes (`LocalQuickFix`), the plugin enables developers to detect, analyze, and refactor nine selected ACs directly within their editor without relying on external analysis pipelines.

## Requirements

| Requirement | Version |
|---|--------|
| IntelliJ IDEA (Community or Ultimate) | 2024.2+ |
| JDK | 21     |
| Gradle | 8.8+   |

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
    │   │   ├── ArithmeticAsLogic.java
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

Detects **every** occurrence of the ternary operator `?:` and offers a refactoring to an explicit `if-else`. The warning is reported on the whole conditional expression. The QuickFix ("Replace with if-else") picks one of three strategies depending on the closest enclosing statement.

| Context | With atom | Without atom (after QuickFix) |
|---|---|---|
| Local variable declaration | `int b = a == 3 ? 2 : 1;` | `int b;`<br>`if (a == 3) { b = 2; } else { b = 1; }` |
| `return` statement | `return a > 0 ? x : y;` | `if (a > 0) { return x; } else { return y; }` |
| Any other statement (assignment, method argument, ...) | `result = a > 0 ? x : y;` | `int _temp;`<br>`if (a > 0) { _temp = x; } else { _temp = y; }`<br>`result = _temp;` |
| Any other statement (assignment, method argument, ...) | `print(a > 0 ? "pos" : "neg");` | `String _temp;`<br>`if (a > 0) { _temp = "pos"; } else { _temp = "neg"; }`<br>`print(_temp);` |

Details:

- In the first two strategies the `if-else` is built directly from the ternary's condition and branches, and replaces the declaration or `return`.
- In the generic strategy, a temporary variable named `_temp` is declared before the enclosing statement, the ternary is replaced by `_temp`, and the type of the temporary is the type of the ternary expression (falling back to `Object` if it cannot be resolved).


### 2. Infix Operator Precedence

Detects expressions where operators of different precedence are mixed without explicit parentheses, in two situations:

1. **Arithmetic:** `*`, `/` or `%` appearing as an operand of `+` or `-`.
2. **Logical:** `&&` and `||` mixed in the same expression.

The warning is reported on the whole parent expression. The QuickFix ("Add parentheses for clarity") wraps each conflicting operand in parentheses, preserving the original evaluation order.

| Case | Trigger | With atom | Without atom  |
|---|---|---|---|
| Arithmetic | `*` under `+` | `int x = a + b * c;` | `int x = a + (b * c);` |
| Arithmetic | `/` under `-` | `int x = a - b / c;` | `int x = a - (b / c);` |
| Arithmetic | `%` under `+` | `int x = a + b % c;` | `int x = a + (b % c);` |
| Arithmetic | Several conflicting operands | `int x = a * b + c / d;` | `int x = (a * b) + (c / d);` |
| Logical | `&&` under `\|\|` (on the right) | `boolean l = p \|\| q && r;` | `boolean l = p \|\| (q && r);` |
| Logical | `&&` under `\|\|` (on the left) | `boolean l = p && q \|\| r;` | `boolean l = (p && q) \|\| r;` |
| Logical | Several conflicting operands | `boolean l = p && q \|\| r && s;` | `boolean l = (p && q) \|\| (r && s);` |

Details:

- Every conflicting operand of the same expression is wrapped by a single QuickFix.
- Expressions that are already enclosed in parentheses are not flagged.
- **String concatenation is excluded** from the arithmetic case (e.g. `"x" + a * b` is not reported), since `+` is not performing arithmetic there.
- If an expression contains both arithmetic and logical mismatches, two separate warnings are reported, each with its own QuickFix.


### 3. Pre-Increment / Pre-Decrement

Detects the prefix `++` / `--` operator in contexts where it may cause confusion: variable assignment, binary operation, method parameter, array index, and return statement. The warning is reported on the most informative parent node (e.g. the full binary expression `3 + ++a` rather than just `++a`).

The QuickFix ("Separate pre-increment/decrement into its own statement") moves the increment into its own statement **before** the parent statement and rewrites it as `+= 1` / `-= 1`, so the refactored code no longer contains the atom. The usage is replaced by the plain variable. Since the new value is the one being used, a single strategy is enough and no temporary variable is needed.

| Context | With atom | Without atom (after QuickFix) |
|---|---|---|
| Local variable declaration | `int b = ++a;` | `a += 1;`<br>`int b = a;` |
| Assignment | `b = --a;` | `a -= 1;`<br>`b = a;` |
| Binary operation | `int r = 3 + ++a;` | `a += 1;`<br>`int r = 3 + a;` |
| Method argument | `print(++a);` | `a += 1;`<br>`print(a);` |
| Array index | `return arr[++i];` | `i += 1;`<br>`return arr[i];` |
| Condition of an `if` / `switch` | `if (++a > 0) { ... }` | `a += 1;`<br>`if (a > 0) { ... }` |
| Field operand | `return ++this.count;` | `this.count += 1;`<br>`return this.count;` |
| Statement is an unbraced `if` / `else` / loop body | `if (c) print(++a);` | `if (c) {`<br>`    a += 1;`<br>`    print(a);`<br>`}` |

Details:

- Supported operands are local variables, parameters and fields (`x`, `this.x`, `Outer.this.x`, `Cls.x`).
- When the statement is the unbraced body of an `if`, `else` or loop, the result is wrapped in a block.
- The QuickFix re-checks safety right before applying, so it does nothing if the code changed after the inspection ran.

**When no fix is offered.** The atom is still reported, but the message says no automatic fix is available, because separating the operation would change behavior. This happens when:

- the expression is a standalone statement (`++i;`), since there is nothing to separate;
- it is evaluated conditionally: right operand of `&&` / `||`, a branch of a ternary (`?:`), or inside a lambda or anonymous/local class;
- it is in a loop condition (`while`, `do-while`, `for`) or in the `for` init/update, since these are re-evaluated or cannot be moved;
- it is inside a rule of a `switch` **expression** (`case X -> foo(++i);`), since wrapping it in a block would require a `yield`;
- the variable is declared in the same statement that uses it (e.g. `int a = 0, b = ++a;`), since the increment would be moved before the declaration;
- the same variable appears more than once in the evaluated part (e.g. `x = i + ++i`);
- the operand is a field and the same statement contains method calls, constructor calls, assignments or other `++` / `--`, since they could observe or modify the field;
- the operand is not a simple variable (e.g. `++arr[0]`, `++obj.f`).



### 4. Post-Increment / Post-Decrement

Detects the suffix `++` / `--` operator in contexts where it may cause confusion: variable assignment, binary operation, method parameter, array index, and return statement. The warning is reported on the most informative parent node (e.g. the full binary expression `3 + a++` rather than just `a++`).

The QuickFix ("Separate post-increment/decrement into its own statement") moves the increment into its own statement and rewrites it as `+= 1` / `-= 1`, so the refactored code no longer contains the atom. It picks one of two strategies, both of which preserve the original value semantics:

| Strategy | When | With atom | Without atom |
|---|---|---|---|
| **After** | Local variable declaration | `int b = a++;` | `int b = a;`<br>`a += 1;` |
| **After** | Plain assignment (`=`) to a simple variable | `b = a--;` | `b = a;`<br>`a -= 1;` |
| **Temp** | `return` statement | `return arr[i++];` | `int oldI = i;`<br>`i += 1;`<br>`return arr[oldI];` |
| **Temp** | Binary operation | `int r = 3 + a++;` | `int oldA = a;`<br>`a += 1;`<br>`int r = 3 + oldA;` |
| **Temp** | Method argument | `print(a++);` | `int oldA = a;`<br>`a += 1;`<br>`print(oldA);` |
| **Temp** | Condition of an `if` / `switch` | `if (a++ > 0) { ... }` | `int oldA = a;`<br>`a += 1;`<br>`if (oldA > 0) { ... }` |
| **Temp** | Field operand | `return this.count++;` | `int oldCount = this.count;`<br>`this.count += 1;`<br>`return oldCount;` |
| **Temp** | Statement is an unbraced `if` / `else` / loop body | `if (c) print(a++);` | `if (c) {`<br>`    int oldA = a;`<br>`    a += 1;`<br>`    print(oldA);`<br>`}` |

Details:

- **After** is used only when the post-increment is the entire right-hand side of a local variable initializer or of a plain `=` assignment statement. Nothing can happen between reading the old value and storing it, so `x = a; a += 1;` is equivalent.
- **Temp** is used in every other case. The old value is saved in a temporary named `old` + the capitalized variable name (e.g. `oldI`, `oldCount`), made unique in scope, and the usage is replaced by that temporary.
- Supported operands are local variables, parameters and fields (`x`, `this.x`, `Outer.this.x`, `Cls.x`).
- When the statement is the unbraced body of an `if`, `else` or loop, the result is wrapped in a block.
- The QuickFix re-checks safety right before applying, so it does nothing if the code changed after the inspection ran.

**When no fix is offered.** The atom is still reported, but the message says no automatic fix is available, because separating the operation would change behavior. This happens when:

- the expression is a standalone statement (`i++;`), since there is nothing to separate;
- it is evaluated conditionally: right operand of `&&` / `||`, a branch of a ternary (`?:`), or inside a lambda or anonymous/local class;
- it is in a loop condition (`while`, `do-while`, `for`) or in the `for` init/update, since these are re-evaluated or cannot be moved;
- the same variable appears more than once in the evaluated part (e.g. `x = i++ + i`, `a[i++] = b[i]`);
- the operand is a field and the same statement contains method calls, constructor calls, assignments or other `++` / `--`, since they could observe or modify the field;
- the operand is not a simple variable (e.g. `arr[0]++`, `obj.f++`).


### 5. Arithmetic as Logic

Detects an equality comparison (`==` or `!=`) between zero and a binary arithmetic expression (`*`, `+` or `-`), where the arithmetic result is being used to answer a logical question. The zero may be on either side (`a * b == 0` or `0 == a * b`), and the arithmetic side may be wrapped in parentheses. The warning is reported on the whole comparison.

The QuickFix ("Replace with equivalent logical expression") rewrites the comparison without arithmetic:

- `*` becomes `||` for `==` and `&&` for `!=`, and each factor is transformed recursively.
- `-` becomes a direct comparison of the two operands.
- `+` becomes a comparison of the left operand with the negated right operand.
- Any other operand is simply compared with `0`.

| With atom       | Without atom                     |
|-----------------|----------------------------------|
| `a * b == 0`    | `a == 0 \|\| b == 0`             |
| `a * b != 0`    | `a != 0 && b != 0`               |
| `0 == a * b`    | `a == 0 \|\| b == 0`             |
| `a - b == 0`    | `a == b`                         |
| `a - b != 0`    | `a != b`                         |
| `a + b == 0`    | `a == -(b)`                      |
| `a + b != 0`    | `a != -(b)`                      |
| `(a * b) * c == 0` | `a == 0 \|\| b == 0 \|\| c == 0` |
| `a * (b - c) == 0` | `a == 0 \|\| b == c`             |
| `a * (b / c) != 0` | `a != 0 && b / c != 0`           |

Details:

- Products are expanded recursively, so each factor of a nested multiplication is handled in a single QuickFix. Factors that are not `*`, `-` or `+` (such as `b / c`) are compared with `0` as a whole.
- Only comparisons with `0` are considered. Comparisons with other values (`a * b == 6`) and relational operators (`<`, `>`) are not reported.
- Only `*`, `+` and `-` are considered. Expressions involving `/` or `%` directly on the compared side are not reported.
### 6. Type Conversion

Detects explicit **narrowing conversions between primitive types** (casts that may lose range or precision) performed without any treatment. The warning is reported on the whole cast expression.

| From | To |
|---|---|
| `byte` | `char` |
| `short` | `byte`, `char` |
| `char` | `byte`, `short` |
| `int` | `byte`, `short`, `char` |
| `long` | `byte`, `short`, `char`, `int` |
| `float` | `byte`, `short`, `char`, `int`, `long` |
| `double` | `byte`, `short`, `char`, `int`, `long`, `float` |

Two QuickFixes are always offered: a comment, plus a second fix that depends on the source type.

| Source type | QuickFix | With atom | Without atom (after QuickFix) |
|---|---|---|---|
| any | **Add comment** (documentation only; does **not** silence the warning) | `int a = (int) x;` | `// Narrowing conversion: double -> int (possible precision loss)`<br>`int a = (int) x;` |
| `float`, `double` | **Wrap operand with `Math.floor()`** | `int a = (int) x;` | `int a = (int) Math.floor(x);` |
| integral (`byte`, `short`, `char`, `int`, `long`) | **Clamp to target range** | `byte b = (byte) n;` | `byte b = (byte) Math.max(Byte.MIN_VALUE, Math.min(Byte.MAX_VALUE, n));` |

For `char` targets the clamp uses `(int) Character.MIN_VALUE` and `(int) Character.MAX_VALUE`.

Details:

- **Not flagged** when the operand:
    - contains a call to `Math.round`, `Math.floor`, `Math.ceil` or `Math.rint` (resolved to `java.lang.Math`, so a same-named method in another class does not count). Other method calls are **not** excluded;
    - contains a modulo operation (`%`), treated as explicit data treatment;
    - is already a `Math.max(MIN, Math.min(MAX, ...))` clamp to the target type's range. This is the pattern generated by the clamp fix, so the warning disappears after applying it.
- **Literal operands** are flagged only when problematic: the value falls outside the target type's range (e.g. `(byte) 300`), or a floating-point literal with a fractional part is cast to an integral type (e.g. `(int) 1.99f`). Casts like `(byte) 10` are not reported.
- The comment is inserted on the line before the enclosing statement (or field declaration).
- The `Math.floor()` fix is only applied when the operand is `float` or `double`.

### 7. Omitted Curly Braces

Detects single-statement bodies of `if` / `else`, `while`, `do-while`, `for`, and `for-each` that are not enclosed in `{ }`, which can cause ambiguity about which statements belong to the block. The QuickFix wraps the body in braces.

| Construct | With atom | Without atom (after QuickFix) |
|---|---|---|
| `if` | `if (a > 0)`<br>`    doSomething();` | `if (a > 0) {`<br>`    doSomething();`<br>`}` |
| `else` | `if (a > 0) {`<br>`    doSomething();`<br>`} else`<br>`    other();` | `if (a > 0) {`<br>`    doSomething();`<br>`} else {`<br>`    other();`<br>`}` |
| `while` | `while (a > 0)`<br>`    a--;` | `while (a > 0) {`<br>`    a--;`<br>`}` |
| `do-while` | `do`<br>`    a--;`<br>`while (a > 0);` | `do {`<br>`    a--;`<br>`} while (a > 0);` |
| `for` | `for (int i = 0; i < n; i++)`<br>`    sum += i;` | `for (int i = 0; i < n; i++) {`<br>`    sum += i;`<br>`}` |
| `for-each` | `for (String s : list)`<br>`    print(s);` | `for (String s : list) {`<br>`    print(s);`<br>`}` |

Details:

- The warning is reported on the **body statement** (not on the whole `if`/loop), and the QuickFix replaces that statement with a block containing it.
- `else if` chains are not flagged twice: the `else` branch is skipped when it is itself an `if`, since that nested `if` is visited independently and its own branches are checked there.
- Excluded from detection:
    - bodies that are already a block (`{ ... }`);
    - empty statements (e.g. `if (a);`);
    - terminal statements: `return`, `break`, `continue` and `throw` (e.g. `if (a) return;`).
- The QuickFix does nothing if the element is no longer valid or is already a block.
### 8. Logic as Control Flow

Detects logical expressions (`&&`, `||`) where a secondary operand contains a side effect or computation (method call, assignment, or increment/decrement) that only runs due to short-circuit evaluation — meaning the logical operator effectively drives control flow instead of evaluating a boolean condition.

The QuickFix rewrites the expression according to the construct that owns it. Chained operators of the same kind (`a || b || c`) are handled as a single expression.

| Host | With Atom                                                       | Without Atom                                                                                                                                                                                                                                                                                                                                                                     |
|---|-----------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `PsiWhileStatement` | <pre>while (a && b()) {<br>    process();<br>}</pre>            | <pre>while (a) {<br>    if (!(b())) {<br>        break;<br>    }<br>    process();<br>}</pre>                                                                                                                                                                                                                                                                                    |
| `PsiIfStatement` | <pre>if (a &#124;&#124; b()) {<br>    doSomething();<br>}</pre> | <pre>if (a) {<br>    doSomething();<br>} else if (b()) {<br>    doSomething();<br>}</pre>                                                                                                                                                                                                                                                                                        |
| `PsiExpressionStatement` | <pre>a && b();</pre>                                            | <pre>if (a) {<br>    b();<br>}</pre>                                                                                                                                                                                                                                                                                                                                             |
| `PsiAssignmentExpression` | <pre>x = a && b();</pre>                                        | <pre>if (!(a)) {<br>    x = false;<br>} else {<br>    x = b();<br>}</pre>                                                                                                                                                                                                                                                                                                        |
| `PsiLocalVariable` | <pre>boolean x = a &#124;&#124; b();</pre>                      | <pre>boolean x;<br>if (a) {<br>    x = true;<br>} else {<br>    x = b();<br>}</pre>                                                                                                                                                                                                                                                                                              |
| `PsiReturnStatement` | <pre>return a && b();</pre>                                     | <pre>if (!(a)) return false;<br>return b();</pre>                                                                                                                                                                                                                                                                                                                                |
| `PsiLambdaExpression` | <pre>() -> a && b()</pre>                                       | <pre>() -> {<br>    if (!(a)) return false;<br>    return b();<br>}</pre>                                                                                                                                                                                                                                                                                                        |

For `if` statements, `|                                                                 | ` produces an `else if` cascade (the `then` body is duplicated), while `&&` produces nested `if`s (the `else` body is duplicated, if present). To avoid code bloat, the fix is only offered when there are at most 4 operands and the duplicated body has at most 3 statements and is not an `else if` chain. `while` loops emit the body only once, so they have no such limit. 
### 9. Change of Literal Encoding

Detects two situations where a numeric literal's base may mislead a reader:

1. An **octal, hexadecimal, or binary** literal used directly as the value of a variable declaration, field initializer, or assignment.
2. A **decimal** literal used as an operand of a bitwise (`&`, `|`, `^`) or shift (`<<`, `>>`, `>>>`) operation, where a non-decimal base would communicate intent better.

| Case | Trigger | With atom | Without atom (after QuickFix) |
|---|---|---|---|
| 1 | Octal literal in a declaration or assignment | `int perm = 0755;` | `int perm = 493;` |
| 1 | Hexadecimal literal in a declaration or assignment | `int flag = 0x1F;` | `int flag = 31;` |
| 1 | Binary literal in a declaration or assignment | `int bits = 0b101;` | `int bits = 5;` |
| 2 | Decimal literal as operand of `&`, `\|`, `^` | `int mask = value & 15;` | `int mask = value & 0xF;` (hexadecimal)<br>`int mask = value & 0b1111;` (binary) |
| 2 | Decimal literal as left operand of `<<`, `>>`, `>>>` | `int big = 256 << n;` | `int big = 0x100 << n;` (hexadecimal)<br>`int big = 0b100000000 << n;` (binary) |

Details:

- In Case 1, only literals that are the **direct** child of the declaration or assignment are reported. A non-decimal literal nested deeper, such as in a method argument or inside another expression, is not flagged. The QuickFix offers a single option: convert to **decimal**.
- In Case 2, both operands of `&`, `|` and `^` are checked. For shifts, only the **left** operand is checked, since the shift distance (right operand) is conventionally written in decimal. The logical operators `&&` and `||` are not considered. The QuickFix offers two alternatives, **hexadecimal** and **binary**, letting the developer pick whichever communicates the bit pattern best.
## Technical Approach

`registerProblem` deliberately highlights the **parent node** of the detected expression when the atom's context adds relevant semantic information — e.g. the full binary expression `3 + a++` rather than just `a++`. Each inspection selects the most informative PSI node to highlight based on parent context.

QuickFixes locate the target sub-expression using `PsiTreeUtil.findChildOfType`, which descends the PSI tree recursively from the highlighted node, so the fix works regardless of which node was used as the highlight target.

## Related Work

This plugin is part of the broader research project:

> *To Refactor or Not to Refactor Atoms of Confusion? Evidence from a Scoping Review, Bytecode-Level Analysis, and Tool Support* (Anonymous, 2026)

## License

This project is developed for academic research purposes.
