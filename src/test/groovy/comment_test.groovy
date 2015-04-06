package me.zhihan.javastyler

import org.junit.Test

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.* 

class LineColumnTest {
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
}

class CommentTest {
    @Test
    void testAlwaysFalse() {
        def pred = { Comment.alwaysFalse(it) }
        assertThat(pred(1), is(false))
        assertThat(pred(0), is(false))
    }

    @Test
    void testToString() {
        def a = new Comment(0, 0, 1, 80)
        assertThat(a.toString().size(), greaterThan(10))
    }

    @Test
    void testEquals() {
        def a = new Comment(0, 0, 1, 1)
        def c = new Comment(0, 0, 1, 1)

        assertThat(a.equals(c), is(true))
        assertThat(a.equals(a), is(true))
        assertThat(a.equals(null), is(false))
        assertThat(a.equals("String"), is(false))
        assertThat(a.equals(new Comment(1, 0, 1, 1)), is(false))
        
        def start = new LineColumn(0, 0)
        def end = new LineColumn(1, 1)
        def x = new Comment(start, end)
        assertThat(x.equals(new Comment(start, end)), is(true))
        assertThat(x.equals(new Comment(start, new LineColumn(1, 1))), is(true))
        assertThat(x.equals(new Comment(start, new LineColumn(1, 2))), is(false))

        def b = new Comment(2, 0, 1, 1)
        assertThat(a.hashCode(), equalTo(c.hashCode()))
        assertThat(a.hashCode(), not(b.hashCode()))
    }
}

class CommentScannerTest {
    @Test
    void testScanComments() {
        def x = ["/* aa ", "bb */"]
        def scanner = new CommentScanner()

        def comments = scanner.scan(x)
        assertThat(comments.size(), is(1))
        assertThat(comments.get(0), is(new Comment(0, 0, 1, 4)))
    }

    @Test
    void testScanCommentsByPassSlash() {
        def x = ["/ aa ", "cc / ", "* d", "/*bb */"]
        def scanner = new CommentScanner()

        def comments = scanner.scan(x)
        assertThat(comments.size(), is(1))
        assertThat(comments.get(0), is(new Comment(3, 0, 3, 6)))

        def x2 = ["/* /* aa */"]
        def comments2 = scanner.scan(x2)
        assertThat(comments.size(), is(1))
        assertThat(comments.get(0), is(new Comment(0, 0, 0, 10)))
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

    @Test
    void testCommentShouldIgoreSlashInQuotes() {
        def x = ["String x = \" // \";"]
        def scanner = new CommentScanner()
        def comments = scanner.scan(x)

        assertThat(comments.isEmpty(), is(true))
    }
}