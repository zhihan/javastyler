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
        assertThat(StringUtil.lastToken(a, 3), is("abc"))
    }

    @Test
    void testLastNonWhitespace() {
        String a = "abc cd "
        assertThat(StringUtil.lastNonWhitespace(a, 6), is(5))
        assertThat(StringUtil.lastNonWhitespace(a, 2), is(1))
    }

    @Test
    void testFirstToken() {
        String a = " abc abc"
        assertThat(StringUtil.findToken(a), is("abc"))
        assertThat(StringUtil.findToken("   "), is(""))
    }

    @Test
    void testPairInt() {
        def x = new PairInt(start:0, end:0) 
        assertThat(x.isEmpty(), is(true))
        assertThat(x.between(0), is(false))
        assertThat(x.between(1), is(false))

        def y = new PairInt(start:0, end:1)
        assertThat(y.isEmpty(), is(false))
        assertThat(y.between(0), is(true))
        assertThat(y.between(1), is(false))
        assertThat(y.between(-1), is(false))
    }
}