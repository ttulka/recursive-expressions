package cz.net21.ttulka.recexp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cz.net21.ttulka.recexp.Expression.EPSILON;
import static cz.net21.ttulka.recexp.Expression.REFERENCE_PREFIX;

/**
 * @ttulka
 */
class ExpressionUtils {

    private static final String REGEXP_REFERENCE = REFERENCE_PREFIX + "((\\w)+)";
    private static final String REGEXP_QUANTIFIER = "(([?*+]|\\{\\d+,?\\d*})[?+]?)";

    private ExpressionUtils() {
        throw new IllegalStateException("Cannot create an instance of this class.");
    }

    /**
     * Replaces references with (.*).
     */
    public static String hydrateExpression(String expression) {
        return resetEscapedReference(
                replaceEscapedReference(expression).replaceAll(REGEXP_REFERENCE, "(.*)")
        );
    }

    public static String replaceEscapedReference(String expression) {
        return expression.replaceAll("\\\\" + REGEXP_REFERENCE, "\\\\__RecexpRefPrefix__$1");
    }

    public static String resetEscapedReference(String expression) {
        return expression.replaceAll("\\\\__RecexpRefPrefix__", "\\\\" + REFERENCE_PREFIX);
    }

    public static List<String> split(String expression) {
        List<String> parts = new ArrayList<String>();

        for (String bracketGroup : ExpressionUtils.getExpressionPartsCutByBrackets(expression)) {
            for (String referenceGroup : ExpressionUtils.getExpressionPartsCutByReferences(bracketGroup)) {
                parts.add(referenceGroup);
            }
        }
        return parts;
    }

    private static List<String> getExpressionPartsCutByBrackets(String expression) {
        List<String> parts = new ArrayList<String>();

        StringBuilder sb = new StringBuilder(expression.length());
        char previous = '\0';
        int bracketsLevel = 0;
        int lastOpeningBracketIndex = -1;

        int index = 0;
        while (index < expression.length()) {
            char ch = expression.charAt(index);
            sb.append(ch);

            if (ch == '(' && previous != '\\') {
                if (sb.length() > 1 && bracketsLevel == 0) {
                    parts.add(sb.toString().substring(0, sb.length() - 1));
                    sb = new StringBuilder(expression.length() - index).append('(');
                }

                bracketsLevel++;
                lastOpeningBracketIndex = index;

            } else if (ch == ')' && previous != '\\') {
                if (bracketsLevel > 0) {
                    bracketsLevel--;

                    if (bracketsLevel == 0) {
                        String exp = sb.toString();

                        // has this part a quantifier?
                        String rest = expression.substring(index + 1);
                        Matcher matcher = Pattern.compile(REGEXP_QUANTIFIER + "(.*)").matcher(rest);
                        if (matcher.matches()) {
                            exp += matcher.group(1);
                            index += matcher.group(1).length();
                        }

                        parts.add(exp);
                        sb = new StringBuilder(expression.length() - index);
                    }

                } else {
                    throw new RecexpSyntaxException("Unmatched closing ')' near index " + index + "\n" + expression);
                }
            }

            previous = ch;
            index++;
        }

        if (bracketsLevel > 0) {
            throw new RecexpSyntaxException("Unmatched opening '(' near index " + lastOpeningBracketIndex + "\n" + expression);
        }

        if (sb.length() > 0 || index == 0) {
            parts.add(sb.toString());
        }
        return parts;
    }

    private static List<String> getExpressionPartsCutByReferences(String expression) {
        if (isClosedInBrackets(expression, true)) {
            return Collections.singletonList(expression);
        }

        Matcher matcher = Pattern.compile(REGEXP_REFERENCE + REGEXP_QUANTIFIER + "?").matcher(expression);

        List<String> parts = new ArrayList<String>();

        expression = replaceEscapedReference(expression);

        int restStarts = 0;

        while (matcher.find()) {
            if (matcher.start() > restStarts) {
                parts.add(resetEscapedReference(
                        expression.substring(restStarts, matcher.start())
                ));
            }
            restStarts = matcher.end();
            parts.add(resetEscapedReference(
                    expression.substring(matcher.start(), matcher.end())
            ));
        }
        if (restStarts < expression.length() || expression.isEmpty()) {
            parts.add(resetEscapedReference(
                    expression.substring(restStarts)
            ));
        }
        return parts;
    }

    public static boolean isClosedInBrackets(String expression, boolean acceptQuantified) {
        if (acceptQuantified && isQuantified(expression)) {
            expression = expression.substring(0, expression.length() - ExpressionUtils.getQuantifier(expression).length());
        }
        if (!expression.startsWith("(") || !expression.endsWith(")")) {
            return false;
        }
        int openBrackets = 0;
        char previous = '\0';

        for (int i = 0; i < expression.length(); i++) {
            char ch = expression.charAt(i);

            if (ch == '(' && previous != '\\' && i < expression.length()) {
                openBrackets++;

            } else if (ch == ')' && previous != '\\' && i > 0) {
                openBrackets--;

                if (openBrackets == 0 && i < expression.length() - 1) {
                    return false;
                }
            }
            previous = ch;
        }
        return openBrackets == 0;
    }

    public static String removeClosingBrackets(String expression) {
        if (isClosedInBrackets(expression, false)) {
            return expression.substring(1, expression.length() - 1);
        }
        return expression;
    }

    public static boolean isReference(String expression) {
        return Pattern.matches(REGEXP_REFERENCE, expression);
    }

    public static String removeReference(String expression) {
        if (isReference(expression)) {
            return expression.substring(1);
        }
        return expression;
    }

    public static boolean isQuantified(String expression) {
        return Pattern.matches("(.+)" + REGEXP_QUANTIFIER, expression);
    }

    public static String getQuantifier(String expression) {
        Matcher matcher = Pattern.compile("(.+)" + REGEXP_QUANTIFIER).matcher(expression);
        if (matcher.matches()) {
            return matcher.group(2);
        }
        return null;
    }

    public static boolean containsEpsilon(String expression) {
        return Pattern.matches(expression, EPSILON);
    }
}
