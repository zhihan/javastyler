package me.zhihan.javastyler

import org.junit.Test

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.* 

class SingleLineTest {
    @Test
    void testSingleLineWidthPass() {
        String line = "Int x = 1;"
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
    }
}