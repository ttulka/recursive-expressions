package cz.net21.ttulka.recexp;

import org.junit.Test;

import static cz.net21.ttulka.recexp.ExpressionUtils.getQuantifier;
import static cz.net21.ttulka.recexp.ExpressionUtils.hydrateExpression;
import static cz.net21.ttulka.recexp.ExpressionUtils.isClosedInBrackets;
import static cz.net21.ttulka.recexp.ExpressionUtils.isQuantified;
import static cz.net21.ttulka.recexp.ExpressionUtils.isReference;
import static cz.net21.ttulka.recexp.ExpressionUtils.matchesEpsilon;
import static cz.net21.ttulka.recexp.ExpressionUtils.removeClosingBrackets;
import static cz.net21.ttulka.recexp.ExpressionUtils.removeReferencePrefix;
import static cz.net21.ttulka.recexp.ExpressionUtils.splitANDs;
import static cz.net21.ttulka.recexp.ExpressionUtils.splitORs;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;

/**
 * @author ttulka
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
    public void splitANDsTest() {
        assertThat(splitANDs("").size(), is(1));
        assertThat(splitANDs("a").toString(), is("[a]"));
        assertThat(splitANDs("ab").toString(), is("[ab]"));
        assertThat(splitANDs("a(b)").toString(), is("[a, (b)]"));
        assertThat(splitANDs("a(b)@this").toString(), is("[a, (b), @this]"));
        assertThat(splitANDs("a(b)(@this)").toString(), is("[a, (b), (@this)]"));
        assertThat(splitANDs("a(b)(\\@this)").toString(), is("[a, (b), (\\@this)]"));
        assertThat(splitANDs("a(b)(\\@this)@this").toString(), is("[a, (b), (\\@this), @this]"));
        assertThat(splitANDs("a(b)(@this)@this").toString(), is("[a, (b), (@this), @this]"));
        assertThat(splitANDs("a(b)(@AB)@CD").toString(), is("[a, (b), (@AB), @CD]"));

        assertThat(splitANDs("(a)(@A)?").toString(), is("[(a), (@A)?]"));

        assertThat(splitANDs("@A@this").toString(), is("[@A, @this]"));
        assertThat(splitANDs("@A@this?").toString(), is("[@A, @this?]"));
        assertThat(splitANDs("@A(@this)?").toString(), is("[@A, (@this)?]"));
        assertThat(splitANDs("(@A(@this)?)").toString(), is("[(@A(@this)?)]"));

        assertThat(splitANDs("()").toString(), is("[()]"));
        assertThat(splitANDs("(())").toString(), is("[(())]"));
        assertThat(splitANDs("()()").toString(), is("[(), ()]"));
        assertThat(splitANDs("(()())").toString(), is("[(()())]"));

        assertThat(splitANDs("a@this?b").toString(), is("[a, @this?, b]"));
        assertThat(splitANDs("a(@this?)b").toString(), is("[a, (@this?), b]"));
        assertThat(splitANDs("a(@this?)b").toString(), is("[a, (@this?), b]"));
        assertThat(splitANDs("(a)(@this?)(b)").toString(), is("[(a), (@this?), (b)]"));
        assertThat(splitANDs("((a))((@this?))((b))").toString(), is("[((a)), ((@this?)), ((b))]"));
        assertThat(splitANDs("a(@this?)b\\)\\)").toString(), is("[a, (@this?), b\\)\\)]"));

        assertThat(splitANDs("a@this?").toString(), is("[a, @this?]"));
        assertThat(splitANDs("a@this*").toString(), is("[a, @this*]"));
        assertThat(splitANDs("a@this+").toString(), is("[a, @this+]"));
        assertThat(splitANDs("a@this{1}").toString(), is("[a, @this{1}]"));
        assertThat(splitANDs("a@this{123}").toString(), is("[a, @this{123}]"));
        assertThat(splitANDs("a@this{1,}").toString(), is("[a, @this{1,}]"));
        assertThat(splitANDs("a@this{123,}").toString(), is("[a, @this{123,}]"));
        assertThat(splitANDs("a@this{1,2}").toString(), is("[a, @this{1,2}]"));
        assertThat(splitANDs("a@this{1,23}").toString(), is("[a, @this{1,23}]"));
        assertThat(splitANDs("a@this??").toString(), is("[a, @this??]"));
        assertThat(splitANDs("a@this*?").toString(), is("[a, @this*?]"));
        assertThat(splitANDs("a@this+?").toString(), is("[a, @this+?]"));
        assertThat(splitANDs("a@this{1}?").toString(), is("[a, @this{1}?]"));
        assertThat(splitANDs("a@this{123}?").toString(), is("[a, @this{123}?]"));
        assertThat(splitANDs("a@this{1,}?").toString(), is("[a, @this{1,}?]"));
        assertThat(splitANDs("a@this{123,}?").toString(), is("[a, @this{123,}?]"));
        assertThat(splitANDs("a@this{1,2}?").toString(), is("[a, @this{1,2}?]"));
        assertThat(splitANDs("a@this{1,23}?").toString(), is("[a, @this{1,23}?]"));
        assertThat(splitANDs("a@this?+").toString(), is("[a, @this?+]"));
        assertThat(splitANDs("a@this*+").toString(), is("[a, @this*+]"));
        assertThat(splitANDs("a@this++").toString(), is("[a, @this++]"));
        assertThat(splitANDs("a@this{1}+").toString(), is("[a, @this{1}+]"));
        assertThat(splitANDs("a@this{123}+").toString(), is("[a, @this{123}+]"));
        assertThat(splitANDs("a@this{1,}+").toString(), is("[a, @this{1,}+]"));
        assertThat(splitANDs("a@this{123,}+").toString(), is("[a, @this{123,}+]"));
        assertThat(splitANDs("a@this{1,2}+").toString(), is("[a, @this{1,2}+]"));
        assertThat(splitANDs("a@this{1,23}+").toString(), is("[a, @this{1,23}+]"));

        try {
            splitANDs("@A(@this)?)");
            fail("RecexpSyntaxException expected");

        } catch (RecexpSyntaxException expected) {
        }

        try {
            splitANDs("(@A(@this)?");
            fail("RecexpSyntaxException expected");

        } catch (RecexpSyntaxException expected) {
        }

        try {
            splitANDs("(");
            fail("RecexpSyntaxException expected");

        } catch (RecexpSyntaxException expected) {
        }

        try {
            splitANDs(")");
            fail("RecexpSyntaxException expected");

        } catch (RecexpSyntaxException expected) {
        }

        try {
            splitANDs("())");
            fail("RecexpSyntaxException expected");

        } catch (RecexpSyntaxException expected) {
        }

        try {
            splitANDs("(()");
            fail("RecexpSyntaxException expected");

        } catch (RecexpSyntaxException expected) {
        }
    }

    @Test
    public void splitORsTest() {
        assertThat(splitORs("a|b").toString(), is("[a, b]"));
        assertThat(splitORs("(a|b)").toString(), is("[(a|b)]"));
        assertThat(splitORs("a|b|c").toString(), is("[a, b, c]"));
        assertThat(splitORs("abc|123").toString(), is("[abc, 123]"));
        assertThat(splitORs("a(b)c|123").toString(), is("[a(b)c, 123]"));
        assertThat(splitORs("a(b)(c|1)23").toString(), is("[a(b)(c|1)23]"));
        assertThat(splitORs("a(b)(c|1)23@this").toString(), is("[a(b)(c|1)23@this]"));
        assertThat(splitORs("a|@this").toString(), is("[a, @this]"));
        assertThat(splitORs("a|b@this").toString(), is("[a, b@this]"));
        assertThat(splitORs("a|b(@this)").toString(), is("[a, b(@this)]"));
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
        assertThat(matchesEpsilon(""), is(true));
        assertThat(matchesEpsilon("a"), is(false));
        assertThat(matchesEpsilon("ab"), is(false));
        assertThat(matchesEpsilon("@A"), is(false));
        assertThat(matchesEpsilon("@AB"), is(false));
        assertThat(matchesEpsilon("@this"), is(false));
        assertThat(matchesEpsilon("a@A"), is(false));
        assertThat(matchesEpsilon("a?"), is(true));
        assertThat(matchesEpsilon("(a)?"), is(true));
        assertThat(matchesEpsilon("(a?)"), is(true));
        assertThat(matchesEpsilon("@A?"), is(true));
        assertThat(matchesEpsilon("@AB?"), is(true));
        assertThat(matchesEpsilon("(a)*"), is(true));
        assertThat(matchesEpsilon("(a*)"), is(true));
        assertThat(matchesEpsilon("@A*"), is(true));
        assertThat(matchesEpsilon("@AB*"), is(true));
        assertThat(matchesEpsilon("(a)+"), is(false));
        assertThat(matchesEpsilon("(a+)"), is(false));
        assertThat(matchesEpsilon("@A+"), is(false));
        assertThat(matchesEpsilon("@AB+"), is(false));
    }
}
