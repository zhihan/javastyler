package me.zhihan.javastyler

import org.junit.Test

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.* 

class CommentTest {
    @Test
    void testLineColumn() {
        def a = new LineColumn(0, 2)
        def b = new LineColumn(0, 2)

        assertThat(a, is(b))

        def c = new LineColumn(0, 3)
        assertThat(a, is(not(equalTo(c))))

        def e = new LineColumn(0, -2)
        def f = LineColumn.endOfLine(0)
        assertThat(e, is(equalTo(f)))
    }

    @Test
    void testLineColumnLeq() {
        def a = new LineColumn(1, 1)
        def b = new LineColumn(0, 1)
        def c = LineColumn.endOfLine(1)

        assertThat(b.leq(a), is(true))
        assertThat(c.leq(a), is(false))
        assertThat(a.leq(c), is(true))
        assertThat(b.leq(c), is(true))
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

    @Test
    void testScanComments3() {
        def x = ["aaa //", "bb /* c */", "cc /* // */"] 
        def scanner = new CommentScanner()
        def comments = scanner.scan(x)
        assertThat(comments.size(), is(3))
        assertThat(comments.get(0), is(new Comment(0, 4, 0, -2)))
        assertThat(comments.get(2), is(new Comment(2, 3, 2, 10)))
    }

    @Test
    void testCommentContains() {
        def x = ["aaa //", "bb /* c */", "cc /* // */"] 
        def scanner = new CommentScanner()
        def comments = scanner.scan(x)
        
        assertThat(Comment.inComment(comments, 0, 4), is(true))
        assertThat(Comment.inComment(comments, 1, 3), is(true))
        assertThat(Comment.inComment(comments, 1, 2), is(false))

    }
}