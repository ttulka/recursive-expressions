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
Matcher matcher = Pattern.compile("(a)((b))").matcher("ab");
matcher.groupCount();   // 3
matcher.group(1)        // a
matcher.group(2)        // b
matcher.group(3)        // b
```

With Recursive Expressions groups are created hierarchically:

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

### Recursive References

Rule expression can reference another rule and/or itself. 

References have syntax `@RefName` where `RefName` can contain only word characters (letters, digits and underscore `_`).

```
RecexpGrammar grammar = RecexpGrammar.builder()
    .rule("RULE", "@A@RULE?@B")
    .rule("A", "a")
    .rule("B", "b")
    .build();
    
RecexpMatcher matcher = grammar.matcher("aabb");
    
matcher.matches();      // true
matcher.groupCount();   // 3    

matcher.group(1).name();    // @A
matcher.group(1).value();   // a   

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

## Release Changes

Not released yet.

## License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)
