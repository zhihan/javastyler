package me.zhihan.javastyler

import org.junit.Test

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.* 

class UtilTest {
    @Test
    void testLastToken() {
        String a = "abc cde f"
        assertThat(StringUtil.lastToken(a, 7), is("cde"))
        assertThat(StringUtil.lastToken(a, 8), is(""))
        assertThat(StringUtil.lastToken(a, 9), is("f"))
    }
}