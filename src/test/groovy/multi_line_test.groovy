package me.zhihan.javastyler

import org.junit.Test

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.* 

class MultiLineTest {
    @Test
    void testMultiLineRuleAnalyzePass() {
        List<String> lines = ["a = 1;", " ", "b = 2;", "", "c = 3;"]
        Diagnostics diag = Tool.analyze(lines, new EmptyLinesRule())

        assertThat(diag.passed(), is(true))
    }

    @Test
    void testMultiLineRuleAnalyzeFail() {
        List<String> lines = ["a = 1;", " ", "", "b = 2;", "", "c = 3;"]
        Diagnostics diag = Tool.analyze(lines, new EmptyLinesRule())

        assertThat(diag.passed(), is(false))
        assertThat(diag.lines, is([1]))
    }

    @Test
    void testMultiLineRuleAnalyzeFix() {
        List<String> lines = ["a = 1;", " ", "", "b = 2;", "", "c = 3;"]
        List<String> result = Tool.fix(lines, new EmptyLinesRule())

        assertThat(result, is(["a = 1;", "", "b = 2;", "", "c = 3;"]))
    }


    @Test
    void testMultiLineRuleAnalyzeIgnoreComment() {
        List<String> lines = ["a = 1;", " ", "//", "b = 2;", "", "c = 3;"]
        Diagnostics diag = Tool.analyze(lines, new EmptyLinesRule())

        assertThat(diag.passed(), is(true))
    }

    @Test
    void testMultiLineRuleAnalyzeIgnoreComment2() {
        List<String> lines = ["a = 1;/*", " ", "", "*/b = 2;", "", "c = 3;"]
        Diagnostics diag = Tool.analyze(lines, new EmptyLinesRule())

        assertThat(diag.passed(), is(true))
    }
} 
