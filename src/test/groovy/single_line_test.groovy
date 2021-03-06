package me.zhihan.javastyler

import org.junit.Test

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.* 

class SingleLineTest {
    @Test
    void testSingleLineWidthPass() {
        String line = "Integer x = 1;"
        SingleLineRule rule = new LineWidthRule(width: 80)
        
        assertThat(rule.analyze(line).passed(), is(true))

    }

    @Test
    void testSingleLineWidthFail() {
        String line = "VeryLongTypeName a = VeryLongPackageName.VeryLongPackageName.VeryLongPackageName." +
            "VeryLongPackageName.VeryLongPackageName.VeryLongTypeName(1);"
        SingleLineRule rule = new LineWidthRule(width: 100)

        Diagnostics d = rule.analyze(line)
        assertThat(d.passed(), is(false))
        assertThat(d.message().indexOf("100"), greaterThan(0))
        assertThat(rule.canFix(line), is(false))
        assertThat(rule.fix(line), is(line))
    }

    @Test
    void testTrailingSpacePass() {
        String line = "Integer x = 1;"
        SingleLineRule rule = new TrailingSpaceRule()
        
        assertThat(rule.analyze(line).passed(), is(true))

        line = ""
        assertThat(rule.analyze(line).passed(), is(true))
        assertThat(rule.fix(line), is(""))
    }

    @Test
    void testTrailingSpaceFail() {
        String line = "Integer x = 1;  "
        SingleLineRule rule = new TrailingSpaceRule()
        
        assertThat(rule.analyze(line).passed(), is(false))
        assertThat(rule.canFix(line), is(true))
        assertThat(rule.fix(line), is("Integer x = 1;"))
    }

    @Test
    void testDoubleQuoteMaskEmpty() {
        String line = "Integer i = 1;"
        QuoteMask mask = QuoteMask.doubleQuote(line)
        List<Integer> raw = mask.rawMasks()

        assertThat(raw.size(), is(0))
    }

    @Test
    void testDoubleQuoteMaskNonEmpty() {
        String line = 'String a = "\'a\'";'
        QuoteMask mask = QuoteMask.doubleQuote(line)
        List<Integer> raw = mask.rawMasks()

        assertThat(raw.size(), is(2))
        String matched = line.substring(raw.get(0), raw.get(1))
        assertThat(matched, is('"\'a\'"'))
    }     

    @Test
    void testDoubleQuoteMaskWithSlash() {
        String line = 'String a = "a \\"";'
        QuoteMask mask = QuoteMask.doubleQuote(line)
        List<Integer> raw = mask.rawMasks()

        assertThat(raw.size(), is(2))
        String matched = line.substring(raw.get(0), raw.get(1))
        assertThat(matched, is('"a \\""'))

        assertThat(mask.masked(0), is(false))
        assertThat(mask.masked(12), is(true))
    } 

    @Test
    void testLeadingTabPass() {
        String line = '  (b + c);' // continued line
        SingleLineRule rule = new LeadingTabRule()

        assertThat(rule.analyze(line).passed(), is(true))
        assertThat(rule.fix(line), is(line))
    } 

    @Test
    void testLeadingTabFail() {
        String line = '\t  \tInteger i;' // continued line
        SingleLineRule rule = new LeadingTabRule()

        assertThat(rule.analyze(line).passed(), is(false))
        assertThat(rule.canFix(line), is(true))
        assertThat(rule.fix(line), is("      Integer i;"))
    }  
}

class ImportStatementTest {
    @Test
    void testPass() {
        String line = "import a;"
        SingleLineRule rule = new ImportNoWildcardRule()

        assertThat(rule.canFix(line), is(false))
        assertThat(rule.analyze(line).passed(), is(true))
    }

    @Test
    void testPassIfNoImports() {
        String line = "a = 1;"
        SingleLineRule rule = new ImportNoWildcardRule()

        assertThat(rule.analyze(line).passed(), is(true))
    }

    @Test
    void testFail() {
        String line = "import a.*;"
        SingleLineRule rule = new ImportNoWildcardRule()

        assertThat(rule.analyze(line).passed(), is(false))
        assertThat(rule.analyze(line).msg.contains("wildcard"), is(true))
    }
}