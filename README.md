# Recursive Expressions

Java Library for working with Recursive Expressions.

**!!! IN PROGRESS !!!**

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
    <version>0.0.1-beta</version>
</dependency>
```

### Recursive Expressions as a Regular Expressions Extension

Regular expression standard Java library doesn't use hierarchical grouping of a parsing result:
```
Matcher matcher = Pattern.compile("(a)((b))").matcher("ab");    // this is standard Java
matcher.groupCount();   // 3
matcher.group(1);       // a
matcher.group(2);       // b
matcher.group(3);       // b
```

With Recursive Expressions are groups created hierarchically:
```
RecexpMatcher matcher = RecexpGrammar.compile("(a)((b))").matcher("ab");

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

### Recursive References

Rule expression can reference another rule and/or itself. 

References have syntax `@RefName` where `RefName` can contain only word characters (letters, digits and underscore `_`).

```
RecexpGrammar grammar = RecexpGrammar.builder()
    .rule("MyRef", "@A@MyRef?@B")
    .rule("A", "a")
    .rule("B", "b")
    .build();
    
RecexpMatcher matcher = grammar.matcher("aabb");
    
matcher.matches();              // true
matcher.groupCount();           // 3    

matcher.group(1).name();        // @A
matcher.group(1).value();       // a   

matcher.group(1).name();        // @RULE?
matcher.group(1).value();       // ab
matcher.group(1).groupCount();  // 1
```

Self-reference has syntax `@this`:

```
RecexpGrammar grammar = RecexpGrammar.compile("a@this|b");

grammar.matcher("b").matches();     // true
grammar.matcher("ab").matches();    // true
grammar.matcher("aab").matches();   // true
grammar.matcher("aaab").matches();  // true

grammar.matcher("a").matches();     // false
```

## Recursive expressions as Context-Free Grammar

to be done

## Examples

### Palindromes
```
S → 0S0 | 1S1 | 0 | 1 | ε 
```
```
RecexpGrammar grammar = RecexpGrammar.builder()
    .rule("S", "0")
    .rule("S", "1")
    .rule("S", "0(@S)0")
    .rule("S", "1(@S)1")
    .rule("S", "@eps")
    .build();
    
grammar.matches("");        // true
grammar.matches("0");       // true
grammar.matches("1");       // true
grammar.matches("11");      // true
grammar.matches("00");      // true
grammar.matches("010");     // true
grammar.matches("101");     // true
grammar.matches("000");     // true
grammar.matches("111");     // true
grammar.matches("0110");    // true
grammar.matches("1001");    // true
grammar.matches("10101");   // true
grammar.matches("10");      // false
grammar.matches("01");      // false
grammar.matches("1101");    // false
```
The same grammar can be compactly created like:
```
RecexpGrammar grammar = RecexpGrammar.builder()
    .rule("S", "0(@S)0|1(@S)1|0|1|@eps")
    .build();
```
Or alternatively by using the `@this` self-reference:
```
RecexpGrammar grammar = RecexpGrammar.compile(
    "0(@this)0|1(@this)1|0|1|@eps");
```

### Strings with the same number of 0s and 1s
```
S → 0S1S | 1S0S | ε 
```
```
RecexpGrammar grammar = RecexpGrammar.builder()
    .rule("S", "0(@S)1(@S)")
    .rule("S", "1(@S)0(@S)")
    .rule("S", "@eps") 
    .build();
    
grammar.matches("");         // true
grammar.matches("0101");     // true
grammar.matches("1010");     // true
grammar.matches("1100");     // true
grammar.matches("110010");   // true
grammar.matches("110100");   // true
grammar.matches("11000101"); // true
grammar.matches("0");        // false
grammar.matches("1");        // false
grammar.matches("00");       // false
grammar.matches("11");       // false
grammar.matches("101");      // false
grammar.matches("010");      // false     
```
The same grammar can be compactly created like:
```
RecexpGrammar grammar = RecexpGrammar.builder()
    .rule("S", "0(@S)1(@S)|1(@S)0(@S)|@eps")
    .build();
```
Or alternatively by using the `@this` self-reference:
```
RecexpGrammar grammar = RecexpGrammar.compile(
    "0(@this)1(@this)|1(@this)0(@this)|@eps");
```  

### Arithmetic expressions over variables X and Y
```
E → E±T | T           (expressions)
T → T×F | F           (terms)
F → (E) | X | Y       (factors)
```
```
RecexpGrammar grammar = RecexpGrammar.builder()
    .rule("E", "@E±@T|@T")
    .rule("T", "@T×@F|@F")
    .rule("F", "\\(@E\\)|X|Y")
    .build();

grammar.matches("X±Y");            // true
grammar.matches("X×Y");            // true
grammar.matches("(X±X)×Y");        // true
grammar.matches("(X±X)×(Y×X)");    // true

grammar.matches("(X×X)(Y×X)");     // false
```

### More examples

For more examples see [unit tests](http://github.com/ttulka/recursive-expressions/blob/master/src/test/java/cz/net21/ttulka/recexp/test/RecexpTest.java) for more examples.

## Release Changes

Not released yet.

## License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)
