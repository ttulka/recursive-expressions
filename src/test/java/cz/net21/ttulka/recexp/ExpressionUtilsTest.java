package cz.net21.ttulka.recexp;

import org.junit.Test;

import static cz.net21.ttulka.recexp.ExpressionUtils.containsEpsilon;
import static cz.net21.ttulka.recexp.ExpressionUtils.getQuantifier;
import static cz.net21.ttulka.recexp.ExpressionUtils.hydrateExpression;
import static cz.net21.ttulka.recexp.ExpressionUtils.isClosedInBrackets;
import static cz.net21.ttulka.recexp.ExpressionUtils.isQuantified;
import static cz.net21.ttulka.recexp.ExpressionUtils.isReference;
import static cz.net21.ttulka.recexp.ExpressionUtils.removeClosingBrackets;
import static cz.net21.ttulka.recexp.ExpressionUtils.removeReferencePrefix;
import static cz.net21.ttulka.recexp.ExpressionUtils.split;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;

/**
 * @ttulka
 */
public class ExpressionUtilsTest {

    @Test
    public void hydrateExpressionTest() {
        assertThat(hydrateExpression("\\@this"), is("\\@this"));
        assertThat(hydrateExpression("ab\\@this"), is("ab\\@this"));
        assertThat(hydrateExpression("ab\\@this@"), is("ab\\@this@"));
        assertThat(hydrateExpression("@this"), is("(.*)"));
        assertThat(hydrateExpression("ab@this"), is("ab(.*)"));
        assertThat(hydrateExpression("ab@this@"), is("ab(.*)@"));
        assertThat(hydrateExpression("ab(@this)"), is("ab((.*))"));
    }

    @Test
    public void isClosedInBracketsTest() {
        assertThat(isClosedInBrackets("", true), is(false));
        assertThat(isClosedInBrackets("(", true), is(false));
        assertThat(isClosedInBrackets(")", true), is(false));
        assertThat(isClosedInBrackets(")(", true), is(false));
        assertThat(isClosedInBrackets("()()", true), is(false));
        assertThat(isClosedInBrackets("()(", true), is(false));
        assertThat(isClosedInBrackets(")()", true), is(false));
        assertThat(isClosedInBrackets("()(())", true), is(false));
        assertThat(isClosedInBrackets("())", true), is(false));
        assertThat(isClosedInBrackets("()))", true), is(false));
        assertThat(isClosedInBrackets("())()", true), is(false));
        assertThat(isClosedInBrackets("(()", true), is(false));
        assertThat(isClosedInBrackets("((()", true), is(false));

        assertThat(isClosedInBrackets("a", true), is(false));
        assertThat(isClosedInBrackets("(a", true), is(false));
        assertThat(isClosedInBrackets("a)", true), is(false));
        assertThat(isClosedInBrackets(")a(", true), is(false));
        assertThat(isClosedInBrackets("(a)a(a)", true), is(false));
        assertThat(isClosedInBrackets("(a)(", true), is(false));
        assertThat(isClosedInBrackets(")(a)", true), is(false));
        assertThat(isClosedInBrackets("(a)((a))", true), is(false));
        assertThat(isClosedInBrackets("(a))", true), is(false));
        assertThat(isClosedInBrackets("(a))a)", true), is(false));
        assertThat(isClosedInBrackets("(a))(a)", true), is(false));
        assertThat(isClosedInBrackets("((a)", true), is(false));
        assertThat(isClosedInBrackets("(a(a(a)", true), is(false));

        assertThat(isClosedInBrackets("()", true), is(true));
        assertThat(isClosedInBrackets("(())", true), is(true));
        assertThat(isClosedInBrackets("(()())", true), is(true));
        assertThat(isClosedInBrackets("(()()())", true), is(true));
        assertThat(isClosedInBrackets("((()()))", true), is(true));

        assertThat(isClosedInBrackets("(\\)", true), is(false));
        assertThat(isClosedInBrackets("(()\\)", true), is(false));
        assertThat(isClosedInBrackets("(()()\\)", true), is(false));
        assertThat(isClosedInBrackets("(()()()\\)", true), is(false));
        assertThat(isClosedInBrackets("((()())\\)", true), is(false));

        assertThat(isClosedInBrackets("\\()", true), is(false));
        assertThat(isClosedInBrackets("\\(())", true), is(false));
        assertThat(isClosedInBrackets("\\(()())", true), is(false));
        assertThat(isClosedInBrackets("\\(()()())", true), is(false));
        assertThat(isClosedInBrackets("\\((()()))", true), is(false));

        assertThat(isClosedInBrackets("(a)", true), is(true));
        assertThat(isClosedInBrackets("((a))", true), is(true));
        assertThat(isClosedInBrackets("((a)(a))", true), is(true));
        assertThat(isClosedInBrackets("(()(a)())", true), is(true));
        assertThat(isClosedInBrackets("(a(a(a)a(a)a)a)", true), is(true));

        assertThat(isClosedInBrackets("(a)?", true), is(true));
        assertThat(isClosedInBrackets("(a)+", true), is(true));
        assertThat(isClosedInBrackets("(a)*", true), is(true));
        assertThat(isClosedInBrackets("(a){1,2}", true), is(true));

        assertThat(isClosedInBrackets("(a)?", false), is(false));
        assertThat(isClosedInBrackets("(a)+", false), is(false));
        assertThat(isClosedInBrackets("(a)*", false), is(false));
        assertThat(isClosedInBrackets("(a){1,2}", false), is(false));
    }

    @Test
    public void removeClosingBracketsTest() {
        assertThat(removeClosingBrackets(""), is(""));
        assertThat(removeClosingBrackets("()"), is(""));
        assertThat(removeClosingBrackets("(a)"), is("a"));
        assertThat(removeClosingBrackets("(ab)"), is("ab"));
        assertThat(removeClosingBrackets("x(ab)y"), is("x(ab)y"));
        assertThat(removeClosingBrackets("(ab)(xy)"), is("(ab)(xy)"));
        assertThat(removeClosingBrackets("((ab)(xy))"), is("(ab)(xy)"));
    }

    @Test
    public void splitTest() {
        assertThat(split("").size(), is(1));
        assertThat(split("a").toString(), is("[a]"));
        assertThat(split("ab").toString(), is("[ab]"));
        assertThat(split("a(b)").toString(), is("[a, (b)]"));
        assertThat(split("a(b)@this").toString(), is("[a, (b), @this]"));
        assertThat(split("a(b)(@this)").toString(), is("[a, (b), (@this)]"));
        assertThat(split("a(b)(\\@this)").toString(), is("[a, (b), (\\@this)]"));
        assertThat(split("a(b)(\\@this)@this").toString(), is("[a, (b), (\\@this), @this]"));
        assertThat(split("a(b)(@this)@this").toString(), is("[a, (b), (@this), @this]"));
        assertThat(split("a(b)(@AB)@CD").toString(), is("[a, (b), (@AB), @CD]"));

        assertThat(split("(a)(@A)?").toString(), is("[(a), (@A)?]"));

        assertThat(split("@A@this").toString(), is("[@A, @this]"));
        assertThat(split("@A@this?").toString(), is("[@A, @this?]"));
        assertThat(split("@A(@this)?").toString(), is("[@A, (@this)?]"));
        assertThat(split("(@A(@this)?)").toString(), is("[(@A(@this)?)]"));

        assertThat(split("()").toString(), is("[()]"));
        assertThat(split("(())").toString(), is("[(())]"));
        assertThat(split("()()").toString(), is("[(), ()]"));
        assertThat(split("(()())").toString(), is("[(()())]"));

        assertThat(split("a@this?b").toString(), is("[a, @this?, b]"));
        assertThat(split("a(@this?)b").toString(), is("[a, (@this?), b]"));
        assertThat(split("a(@this?)b").toString(), is("[a, (@this?), b]"));
        assertThat(split("(a)(@this?)(b)").toString(), is("[(a), (@this?), (b)]"));
        assertThat(split("((a))((@this?))((b))").toString(), is("[((a)), ((@this?)), ((b))]"));
        assertThat(split("a(@this?)b\\)\\)").toString(), is("[a, (@this?), b\\)\\)]"));

        assertThat(split("a@this?").toString(), is("[a, @this?]"));
        assertThat(split("a@this*").toString(), is("[a, @this*]"));
        assertThat(split("a@this+").toString(), is("[a, @this+]"));
        assertThat(split("a@this{1}").toString(), is("[a, @this{1}]"));
        assertThat(split("a@this{123}").toString(), is("[a, @this{123}]"));
        assertThat(split("a@this{1,}").toString(), is("[a, @this{1,}]"));
        assertThat(split("a@this{123,}").toString(), is("[a, @this{123,}]"));
        assertThat(split("a@this{1,2}").toString(), is("[a, @this{1,2}]"));
        assertThat(split("a@this{1,23}").toString(), is("[a, @this{1,23}]"));
        assertThat(split("a@this??").toString(), is("[a, @this??]"));
        assertThat(split("a@this*?").toString(), is("[a, @this*?]"));
        assertThat(split("a@this+?").toString(), is("[a, @this+?]"));
        assertThat(split("a@this{1}?").toString(), is("[a, @this{1}?]"));
        assertThat(split("a@this{123}?").toString(), is("[a, @this{123}?]"));
        assertThat(split("a@this{1,}?").toString(), is("[a, @this{1,}?]"));
        assertThat(split("a@this{123,}?").toString(), is("[a, @this{123,}?]"));
        assertThat(split("a@this{1,2}?").toString(), is("[a, @this{1,2}?]"));
        assertThat(split("a@this{1,23}?").toString(), is("[a, @this{1,23}?]"));
        assertThat(split("a@this?+").toString(), is("[a, @this?+]"));
        assertThat(split("a@this*+").toString(), is("[a, @this*+]"));
        assertThat(split("a@this++").toString(), is("[a, @this++]"));
        assertThat(split("a@this{1}+").toString(), is("[a, @this{1}+]"));
        assertThat(split("a@this{123}+").toString(), is("[a, @this{123}+]"));
        assertThat(split("a@this{1,}+").toString(), is("[a, @this{1,}+]"));
        assertThat(split("a@this{123,}+").toString(), is("[a, @this{123,}+]"));
        assertThat(split("a@this{1,2}+").toString(), is("[a, @this{1,2}+]"));
        assertThat(split("a@this{1,23}+").toString(), is("[a, @this{1,23}+]"));
        assertThat(split("a@this{,}++").toString(), is("[a, @this, {,}++]"));
        assertThat(split("a@this{}++").toString(), is("[a, @this, {}++]"));
        assertThat(split("a@this{,1}++").toString(), is("[a, @this, {,1}++]"));

        try {
            split("@A(@this)?)");
            fail("RecexpSyntaxException expected");

        } catch (RecexpSyntaxException expected) {
        }

        try {
            split("(@A(@this)?");
            fail("RecexpSyntaxException expected");

        } catch (RecexpSyntaxException expected) {
        }

        try {
            split("(");
            fail("RecexpSyntaxException expected");

        } catch (RecexpSyntaxException expected) {
        }

        try {
            split(")");
            fail("RecexpSyntaxException expected");

        } catch (RecexpSyntaxException expected) {
        }

        try {
            split("())");
            fail("RecexpSyntaxException expected");

        } catch (RecexpSyntaxException expected) {
        }

        try {
            split("(()");
            fail("RecexpSyntaxException expected");

        } catch (RecexpSyntaxException expected) {
        }
    }

    @Test
    public void isReferenceTest() {
        assertThat(isReference(""), is(false));
        assertThat(isReference("a"), is(false));
        assertThat(isReference("ab"), is(false));
        assertThat(isReference("@A"), is(true));
        assertThat(isReference("@AB"), is(true));
        assertThat(isReference("@AB?"), is(true));
        assertThat(isReference("@AB{1,2}"), is(true));
        assertThat(isReference("(ab)"), is(false));
        assertThat(isReference("(@AB)"), is(false));
        assertThat(isReference("@AB(a)"), is(false));
        assertThat(isReference("@this"), is(true));
    }

    @Test
    public void removeReferencePrefixTest() {
        assertThat(removeReferencePrefix(""), is(""));
        assertThat(removeReferencePrefix("a"), is("a"));
        assertThat(removeReferencePrefix("ab"), is("ab"));
        assertThat(removeReferencePrefix("@A"), is("A"));
        assertThat(removeReferencePrefix("@AB"), is("AB"));
        assertThat(removeReferencePrefix("@this"), is("this"));
    }

    @Test
    public void isQuantifiedTest() {
        assertThat(isQuantified(""), is(false));
        assertThat(isQuantified("a"), is(false));
        assertThat(isQuantified("ab"), is(false));
        assertThat(isQuantified("@A"), is(false));
        assertThat(isQuantified("@AB"), is(false));
        assertThat(isQuantified("@this"), is(false));
        assertThat(isQuantified("a@this"), is(false));
        assertThat(isQuantified("a@this?"), is(true));
        assertThat(isQuantified("a@this*"), is(true));
        assertThat(isQuantified("a@this+"), is(true));
        assertThat(isQuantified("a@this{1}"), is(true));
        assertThat(isQuantified("a@this{123}"), is(true));
        assertThat(isQuantified("a@this{1,}"), is(true));
        assertThat(isQuantified("a@this{123,}"), is(true));
        assertThat(isQuantified("a@this{1,2}"), is(true));
        assertThat(isQuantified("a@this{1,23}"), is(true));
        assertThat(isQuantified("a@this??"), is(true));
        assertThat(isQuantified("a@this*?"), is(true));
        assertThat(isQuantified("a@this+?"), is(true));
        assertThat(isQuantified("a@this{1}?"), is(true));
        assertThat(isQuantified("a@this{123}?"), is(true));
        assertThat(isQuantified("a@this{1,}?"), is(true));
        assertThat(isQuantified("a@this{123,}?"), is(true));
        assertThat(isQuantified("a@this{1,2}?"), is(true));
        assertThat(isQuantified("a@this{1,23}?"), is(true));
        assertThat(isQuantified("a@this?+"), is(true));
        assertThat(isQuantified("a@this*+"), is(true));
        assertThat(isQuantified("a@this++"), is(true));
        assertThat(isQuantified("a@this{1}+"), is(true));
        assertThat(isQuantified("a@this{123}+"), is(true));
        assertThat(isQuantified("a@this{1,}+"), is(true));
        assertThat(isQuantified("a@this{123,}+"), is(true));
        assertThat(isQuantified("a@this{1,2}+"), is(true));
        assertThat(isQuantified("a@this{1,23}+"), is(true));
        assertThat(isQuantified("a@this{,}++"), is(true));
        assertThat(isQuantified("a@this{}++"), is(true));
        assertThat(isQuantified("a@this{,1}++"), is(true));
    }

    @Test
    public void getQuantifierTest() {
        assertThat(getQuantifier(""), is(nullValue()));
        assertThat(getQuantifier("a"), is(nullValue()));
        assertThat(getQuantifier("ab"), is(nullValue()));
        assertThat(getQuantifier("@A"), is(nullValue()));
        assertThat(getQuantifier("@AB"), is(nullValue()));
        assertThat(getQuantifier("@this"), is(nullValue()));
        assertThat(getQuantifier("a@this"), is(nullValue()));
        assertThat(getQuantifier("a@this?"), is("?"));
        assertThat(getQuantifier("a@this*"), is("*"));
        assertThat(getQuantifier("a@this+"), is("+"));
        assertThat(getQuantifier("a@this{1}"), is("{1}"));
        assertThat(getQuantifier("a@this{123}"), is("{123}"));
        assertThat(getQuantifier("a@this{1,}"), is("{1,}"));
        assertThat(getQuantifier("a@this{123,}"), is("{123,}"));
        assertThat(getQuantifier("a@this{1,2}"), is("{1,2}"));
        assertThat(getQuantifier("a@this{1,23}"), is("{1,23}"));
        assertThat(getQuantifier("a@this??"), is("??"));
        assertThat(getQuantifier("a@this*?"), is("*?"));
        assertThat(getQuantifier("a@this+?"), is("+?"));
        assertThat(getQuantifier("a@this{1}?"), is("{1}?"));
        assertThat(getQuantifier("a@this{123}?"), is("{123}?"));
        assertThat(getQuantifier("a@this{1,}?"), is("{1,}?"));
        assertThat(getQuantifier("a@this{123,}?"), is("{123,}?"));
        assertThat(getQuantifier("a@this{1,2}?"), is("{1,2}?"));
        assertThat(getQuantifier("a@this{1,23}?"), is("{1,23}?"));
        assertThat(getQuantifier("a@this?+"), is("?+"));
        assertThat(getQuantifier("a@this*+"), is("*+"));
        assertThat(getQuantifier("a@this++"), is("++"));
        assertThat(getQuantifier("a@this{1}+"), is("{1}+"));
        assertThat(getQuantifier("a@this{123}+"), is("{123}+"));
        assertThat(getQuantifier("a@this{1,}+"), is("{1,}+"));
        assertThat(getQuantifier("a@this{123,}+"), is("{123,}+"));
        assertThat(getQuantifier("a@this{1,2}+"), is("{1,2}+"));
        assertThat(getQuantifier("a@this{1,23}+"), is("{1,23}+"));
        assertThat(getQuantifier("a@this{,}++"), is("++"));
        assertThat(getQuantifier("a@this{}++"), is("++"));
        assertThat(getQuantifier("a@this{,1}++"), is("++"));
    }

    @Test
    public void containsEpsilonTest() {
        assertThat(containsEpsilon(""), is(true));
        assertThat(containsEpsilon("a"), is(false));
        assertThat(containsEpsilon("ab"), is(false));
        assertThat(containsEpsilon("@A"), is(false));
        assertThat(containsEpsilon("@AB"), is(false));
        assertThat(containsEpsilon("@this"), is(false));
        assertThat(containsEpsilon("a@A"), is(false));
        assertThat(containsEpsilon("a?"), is(true));
        assertThat(containsEpsilon("(a)?"), is(true));
        assertThat(containsEpsilon("(a?)"), is(true));
        assertThat(containsEpsilon("@A?"), is(true));
        assertThat(containsEpsilon("@AB?"), is(true));
        assertThat(containsEpsilon("(a)*"), is(true));
        assertThat(containsEpsilon("(a*)"), is(true));
        assertThat(containsEpsilon("@A*"), is(true));
        assertThat(containsEpsilon("@AB*"), is(true));
        assertThat(containsEpsilon("(a)+"), is(false));
        assertThat(containsEpsilon("(a+)"), is(false));
        assertThat(containsEpsilon("@A+"), is(false));
        assertThat(containsEpsilon("@AB+"), is(false));
    }
}
