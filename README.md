# Recursive Expressions

Java library for working with recursive expressions and context-free languages and grammars.

## Prerequisites

- Java 6

## Characteristics

- Not Thread-safe

## Usage

Copy the Maven dependency into your Maven project:

```
<dependency>
    <groupId>cz.net21.ttulka.recexp</groupId>
    <artifactId>recursive-expressions</artifactId>
    <version>0.0.2-beta</version>
</dependency>
```

### Recursive Expressions as an Extension of Regular Expressions 

Regular expression standard Java library does not use hierarchical grouping of a parsing result:
```
Matcher matcher = Pattern.compile("(a)((b))").matcher("ab");    // this is standard Java
matcher.groupCount();   // 3
matcher.group(1);       // a
matcher.group(2);       // b
matcher.group(3);       // b
```

With Recursive Expressions are groups created hierarchically:
```
RecexpMatcher matcher = Recexp.compile("(a)((b))").matcher("ab");

matcher.groupCount();       // 2

matcher.group(1).name();    // a
matcher.group(1).value();   // a

matcher.group(2).name();    // (b)
matcher.group(2).value();   // b

matcher.group(2).groupCount();      // 1 
matcher.group(2).group(1).name();   // b
matcher.group(2).group(1).value();  // b
```

Hierarchical expression tree from the example above:
```
    (a)((b))
    /     \
   a      (b)
            \
             b
```

#### Match flags

Recursive Expressions are using the standard match flags from the `Pattern` class:
```
Recexp.compile("a", Pattern.CASE_INSENSITIVE)    // case-insetive
    .matches("A");  // true
```
The flags are applied to all the rules.

### Recursive Expressions as a Context-Free Grammar

A context-free grammar can be defined as a set of rules with a starting rule `S`. The rules are of the form `A → w`, where `A` is a name of the rule and `w` is a string which can contain characters and rule references.

```
Recexp grammar = Recexp.builder()
    .rule("S", "@A@B")
    .rule("A", "a")
    .rule("B", "b")
    .build();
    
RecexpMatcher matcher = grammar.matcher("S", "ab");
matcher.matches();  // true - the grammar accepts the input "ab"
```

The *matcher* contains a result of a derivation from the starting rule. If the starting rule is omitted, each rule is a starting rule.

When the rule name is omitted, the whole expression is used as a rule name and the rule cannot be referenced.

**Tip:** *Use the convenience shortcut `Recexp.compile(rule1, ..., ruleN)` for defining a grammar with multiple anonymous rules.*  

#### Recursive rule references

Rule expression can reference another rule and/or itself. 

References have syntax `@RefName` where `RefName` can contain only word characters (letters, digits and underscore `_`).

```
Recexp recexp = Recexp.builder()
    .rule("MyRef", "@A@MyRef?@B")
    .rule("A", "a")
    .rule("B", "b")
    .build();
    
RecexpMatcher matcher = recexp.matcher("aabb");
    
matcher.matches();              // true
matcher.groupCount();           // 3    

matcher.group(1).name();        // @A
matcher.group(1).value();       // a   

matcher.group(2).name();        // @MyRef?
matcher.group(2).value();       // ab

matcher.group(3).name();        // @B
matcher.group(3).value();       // b
```

#### Self-reference `@this`
```
Recexp recexp = Recexp.compile("a@this|b");

recexp.matches("b");     // true
recexp.matches("ab");    // true
recexp.matches("aab");   // true
recexp.matches("aaab");  // true

recexp.matcher("a").matches();     // false
```

#### Empty reference `@eps` (Epsilon)

*Epsilon* has syntax `@eps` and can be use as a rule defining an empty string:
```
Recexp recexp = Recexp.compile("a|@eps");

recexp.matches("");      // true
recexp.matches("a");     // true
```

Epsilon is a shortcut for an empty rule:
```
Recexp recexp = Recexp.builder()
    .rule("epsilon", "")
    ...
```

## Examples

### Palindromes
```
S → 0S0 | 1S1 | 0 | 1 | ε 
```
```
Recexp recexp = Recexp.builder()
    .rule("S", "0")
    .rule("S", "1")
    .rule("S", "0(@S)0")
    .rule("S", "1(@S)1")
    .rule("S", "@eps")
    .build();
    
recexp.matches("");        // true
recexp.matches("0");       // true
recexp.matches("1");       // true
recexp.matches("11");      // true
recexp.matches("00");      // true
recexp.matches("010");     // true
recexp.matches("101");     // true
recexp.matches("000");     // true
recexp.matches("111");     // true
recexp.matches("0110");    // true
recexp.matches("1001");    // true
recexp.matches("10101");   // true
recexp.matches("10");      // false
recexp.matches("01");      // false
recexp.matches("1101");    // false
```
The same definition can be compactly created like:
```
Recexp recexp = Recexp.builder()
    .rule("S", "0(@S)0|1(@S)1|0|1|@eps")
    .build();
```
Or alternatively by using the `@this` self-reference:
```
Recexp recexp = Recexp.compile(
    "0(@this)0|1(@this)1|0|1|@eps");
```

### Strings with the same number of 0s and 1s
```
S → 0S1S | 1S0S | ε 
```
```
Recexp recexp = Recexp.builder()
    .rule("S", "0(@S)1(@S)")
    .rule("S", "1(@S)0(@S)")
    .rule("S", "@eps") 
    .build();
    
recexp.matches("");         // true
recexp.matches("0101");     // true
recexp.matches("1010");     // true
recexp.matches("1100");     // true
recexp.matches("110010");   // true
recexp.matches("110100");   // true
recexp.matches("11000101"); // true
recexp.matches("0");        // false
recexp.matches("1");        // false
recexp.matches("00");       // false
recexp.matches("11");       // false
recexp.matches("101");      // false
recexp.matches("010");      // false     
```
The same definition can be compactly created like:
```
Recexp recexp = Recexp.builder()
    .rule("S", "0(@S)1(@S)|1(@S)0(@S)|@eps")
    .build();
```
Or alternatively by using the `@this` self-reference:
```
Recexp recexp = Recexp.compile(
    "0(@this)1(@this)|1(@this)0(@this)|@eps");
```  

### Arithmetic expressions over variables X and Y
```
E → E±T | T           (expressions)
T → T×F | F           (terms)
F → (E) | X | Y       (factors)
```
```
Recexp recexp = Recexp.builder()
    .rule("E", "@E±@T|@T")
    .rule("T", "@T×@F|@F")
    .rule("F", "\\(@E\\)|X|Y")
    .build();

recexp.matches("X±Y");            // true
recexp.matches("X×Y");            // true
recexp.matches("(X±X)×Y");        // true
recexp.matches("(X±X)×(Y×X)");    // true

recexp.matches("(X×X)(Y×X)");     // false
```

### More examples

For more examples see [unit tests](http://github.com/ttulka/recursive-expressions/blob/master/src/test/java/cz/net21/ttulka/recexp/test/RecexpTest.java).

## Release Changes

Not released yet.

## License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)
