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

    @Test
    void testFirstToken() {
        String a = " abc abc"
        assertThat(StringUtil.findToken(a), is("abc"))
        assertThat(StringUtil.findToken("   "), is(""))
    }

    @Test
    void testScanComments() {
        def x = ["/* aa ", "bb */"]
        def scanner = new CommentScanner()

        def comments = scanner.scan(x)
        assertThat(comments.size(), is(1))
        assertThat(comments.get(0), is(new Comment(0, 0, 1, 4)))
    }

    @Test 
    void testScanComments2() {
        def x = ["aaa /*/", "bb */"]
        def scanner = new CommentScanner()

        def comments = scanner.scan(x)
        assertThat(comments.size(), is(1))
    }
}