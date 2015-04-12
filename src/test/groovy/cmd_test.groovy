package me.zhihan.javastyler

import org.junit.Test

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.* 

class CommandTest {
    @Test
    void testSingleLineRuleAnalyze() {
        URL url = ClassLoader.getSystemClassLoader().getResource("test_data1.java")
        List<String> lines = url.readLines()
        Diagnostics diag = Tool.analyze(lines, new TrailingSpaceRule())

        assertThat(diag.passed(), is(false))
        assertThat(diag.lines, is([0, 2]))

        List<String> results = Tool.fix(lines, new TrailingSpaceRule())
        assertThat(results, is([
            "Integer a = 1;",
            "Integer b = 2;", 
            "Integer c = 3;"]))
    }

    @Test
    void testSingleLineRuleAnalyzePassOnEmpty() {
        List<String> lines = [""]
        List<Diagnostics> diag = Tool.analyzeSingle(lines, Tool.singleLineRules())

        assertThat(diag, is(empty()))
    }
    
    @Test
    void testMultiLineRuleAnalyzePassOnEmpty() {
        List<String> lines = [""]
        List<Diagnostics> diag = Tool.analyzeMulti(lines, Tool.multiLineRules())

        assertThat(diag, is(empty()))
    }

    @Test
    void testMultiRuleAnalyze() {
        URL url = ClassLoader.getSystemClassLoader().getResource("test_data1.java")
        List<String> lines = url.readLines()
        List<Diagnostics> diag = Tool.analyzeSingle(lines, [new TrailingSpaceRule()])

        assertThat(diag.get(0).passed(), is(false))
        assertThat(diag.get(0).lines, is([0, 2]))
        List<String> results = Tool.fixSingle(lines, [new TrailingSpaceRule()])
        assertThat(results, is([
            "Integer a = 1;",
            "Integer b = 2;", 
            "Integer c = 3;"]))

    }

    @Test
    void testSingleLineWithComment1() {
        URL url = ClassLoader.getSystemClassLoader().getResource("test_data2.java")
        List<String> lines = url.readLines()
        Diagnostics diag = Tool.analyze(lines, new LeftParenthesisRule())

        assertThat(diag.passed(), is(false))
        assertThat(diag.lines, is([5]))
        List<String> results = Tool.fix(lines, new LeftParenthesisRule())
        assertThat(results, is([
            " /* ",
            " Commented out area should not count",
            " String a = f  ();",
            " */",
            "",
            "String b = f();"
            ]))
    }

    @Test
    void testSingleLineWithComment2() {
        URL url = ClassLoader.getSystemClassLoader().getResource("test_data3.java")
        List<String> lines = url.readLines()
        Diagnostics diag = Tool.analyze(lines, NoLeadingSpaceRule.semiColonRule())

        assertThat(diag.passed(), is(false))
        assertThat(diag.lines, is([5]))

        List<String> results = Tool.fix(lines, NoLeadingSpaceRule.semiColonRule())
        assertThat(results, is([
            "/*",
            "String a = f()  ;",
            "*/",
            "String b // = f()  ;",
            "",
            "String b = f ();"
            ]))  
    }

    @Test
    void testSingleLineWithComment3() {
        URL url = ClassLoader.getSystemClassLoader().getResource("test_data4.java")
        List<String> lines = url.readLines()
        Diagnostics diag = Tool.analyze(lines, RequireLeadingSpaceRule.openBracketRule())

        assertThat(diag.passed(), is(false))
        assertThat(diag.lines, is([5]))

        List<String> results = Tool.fix(lines, RequireLeadingSpaceRule.openBracketRule())
        assertThat(results, is([
            "/*",
            "String a = f(){}  ;",
            "*/",
            "String b // = f(){}  ;",
            "",
            "String b = f () {} ;"
            ]))       
    }
} 
