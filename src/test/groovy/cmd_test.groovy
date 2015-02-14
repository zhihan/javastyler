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
    }

    @Test
    void testMultiRuleAnalyze() {
        URL url = ClassLoader.getSystemClassLoader().getResource("test_data1.java")
        List<String> lines = url.readLines()
        List<Diagnostics> diag = Tool.analyze(lines, [new TrailingSpaceRule()])

        assertThat(diag.get(0).passed(), is(false))
        assertThat(diag.get(0).lines, is([0, 2]))
    }
} 
