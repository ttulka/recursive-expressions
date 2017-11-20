package cz.net21.ttulka.recexp;

import org.junit.Test;

import static cz.net21.ttulka.recexp.ExpressionUtils.hydrateExpression;
import static cz.net21.ttulka.recexp.ExpressionUtils.isClosedInBrackets;
import static cz.net21.ttulka.recexp.ExpressionUtils.split;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
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
}
