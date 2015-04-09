package me.zhihan.javastyler
import org.junit.Test

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.* 

class ParenthesisTest {
   @Test
    void testLeftParenthesisPass() {
        String line = 'String a = f(b, c);'
        SingleLineRule rule = new LeftParenthesisRule()

        assertThat(rule.analyze(line).passed(), is(true))
    } 

    @Test
    void testLeftParenthesisPassOnEmpty() {
        String line = ''
        SingleLineRule rule = new LeftParenthesisRule()

        assertThat(rule.analyze(line).passed(), is(true))
        assertThat(rule.canFix(line), is(true))
    } 

    @Test
    void testLeftParenthesisPass2() {
        String line = 'if (a > 1) {'
        SingleLineRule rule = new LeftParenthesisRule()

        assertThat(rule.analyze(line).passed(), is(true))
        assertThat(rule.fix(line), is(line))
    } 

    @Test
    void testLeftParenthesis1() {
        String line = 'String a = f  ();'
        SingleLineRule rule = new LeftParenthesisRule()

        assertThat(rule.analyze(line).passed(), is(false))
        assertThat(rule.fix(line), is("String a = f();"))
    } 

    @Test
    void testLeftParenthesis2() {
        String line = 'String a = f(  b);'
        SingleLineRule rule = new LeftParenthesisRule()

        assertThat(rule.analyze(line).passed(), is(false))
        assertThat(rule.fix(line), is("String a = f(b);"))
    } 

    @Test
    void testLeftParenthesis3() {
        String line = 'String a = f  (b) + f(   c);'
        SingleLineRule rule = new LeftParenthesisRule()

        assertThat(rule.analyze(line).passed(), is(false))
        assertThat(rule.fix(line), is("String a = f(b) + f(c);"))
    } 

    @Test
    void testLeftParenthesisInQuote() {
        String line = 'String a = "f(  b)";'
        SingleLineRule rule = new LeftParenthesisRule()

        assertThat(rule.analyze(line).passed(), is(true))
        assertThat(rule.fix(line), is(line))
    } 

    @Test
    void testLeftParenthesisWithOperator() {
        String line = 'Integer a = (a + b) * (b + c);'
        SingleLineRule rule = new LeftParenthesisRule()

        assertThat(rule.analyze(line).passed(), is(true))
        assertThat(rule.fix(line), is(line))
    } 

    @Test
    void testLeftParenthesisContinuedLine() {
        String line = '  (b + c);' // continued line
        SingleLineRule rule = new LeftParenthesisRule()

        assertThat(rule.analyze(line).passed(), is(true))
        assertThat(rule.fix(line), is(line))
    }

    @Test
    void testLeftParenthesisTrailingSpaceAfterOpen() {
        String line = '  int x = f(   ' // trailing space
        SingleLineRule rule = new LeftParenthesisRule()

        assertThat(rule.analyze(line).passed(), is(false))
        assertThat(rule.fix(line), is('  int x = f('))
    }

    @Test
    void testSemicolonPass() {
        String line = '  Integer i = 0;' // continued line
        SingleLineRule rule = NoLeadingSpaceRule.semiColonRule()

        assertThat(rule.analyze(line).passed(), is(true))
        assertThat(rule.fix(line), is(line))
    }

    @Test
    void testSemicolonPassIfNone() {
        String line = '  if (x)' // continued line
        SingleLineRule rule = NoLeadingSpaceRule.semiColonRule()

        assertThat(rule.analyze(line).passed(), is(true))
        assertThat(rule.fix(line), is(line))
    } 
    
    @Test
    void testSemicolonPassIfMasked() {
        String line = '  if (x == ";")' // continued line
        SingleLineRule rule = NoLeadingSpaceRule.semiColonRule()

        assertThat(rule.analyze(line).passed(), is(true))
        assertThat(rule.fix(line), is(line))
    } 

    @Test
    void testSemicolonPassIfSkip() {
        String line = ' // if (x == 0) ;' // continued line
        SingleLineRule rule = NoLeadingSpaceRule.semiColonRule()

        rule.setSkip({ offset -> offset > 1})
        assertThat(rule.analyze(line).passed(), is(true))
        assertThat(rule.fix(line), is(line))
    } 

    @Test
    void testSemicolonPassIfEmptyLine() {
        String line = '    ;' // empty line
        SingleLineRule rule = NoLeadingSpaceRule.semiColonRule()

        assertThat(rule.analyze(line).passed(), is(true))
        assertThat(rule.fix(line), is(line))
    }

    @Test
    void testSemicolonFail() {
        String line = '  Integer i = 0  ;' // continued line
        SingleLineRule rule = NoLeadingSpaceRule.semiColonRule()

        assertThat(rule.analyze(line).passed(), is(false))
        assertThat(rule.fix(line), is('  Integer i = 0;'))
    } 

    @Test
    void testRightParenthesisPass() {
        String line = '  Integer i = f();' // continued line
        SingleLineRule rule = NoLeadingSpaceRule.rightParenthesisRule()

        assertThat(rule.analyze(line).passed(), is(true))
        assertThat(rule.fix(line), is(line))
    } 
    
    @Test
    void testRightParenthesisFail() {
        String line = '  Integer i = f(   );' // continued line
        SingleLineRule rule = NoLeadingSpaceRule.rightParenthesisRule()

        assertThat(rule.analyze(line).passed(), is(false))
        assertThat(rule.fix(line), is('  Integer i = f();'))
    }

    @Test
    void testOpenBracketPass() {
        String line = "  void fun() {"
        SingleLineRule rule = RequireLeadingSpaceRule.openBracketRule()

        assertThat(rule.analyze(line).passed(), is(true))
        assertThat(rule.fix(line), is(line))
    }

    @Test
    void testOpenBracketFail() {
        String line = "  void fun(){"
        SingleLineRule rule = RequireLeadingSpaceRule.openBracketRule()

        assertThat(rule.analyze(line).passed(), is(false))
        assertThat(rule.fix(line), is("  void fun() {"))
    }

    @Test
    void testOpenBracketFail2() {
        String line = "  void fun()    {"
        SingleLineRule rule = RequireLeadingSpaceRule.openBracketRule()

        assertThat(rule.analyze(line).passed(), is(false))
        assertThat(rule.fix(line), is("  void fun() {"))
    }
}