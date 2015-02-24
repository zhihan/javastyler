package me.zhihan.javastyler

import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.List

class PairInt {
    Integer start
    Integer end
    Boolean between(Integer i) {
        return (start <= i) && (i < end)
    }
}
/**
 * A helper class to provide maskng for quoted strings. For example, 
 * some rules such as the parenthesis rule does not apply to parenthesis
 * in a quoted string.
 */
class QuoteMask {
    List<PairInt> masks;

    static Pattern doubleQuoteString = ~/"([^"]|\")*"/
    static Pattern singleQuoteString = ~/'([^']|\')*'/

    private static QuoteMask quote(String line, Pattern pattern) {
        QuoteMask result = new QuoteMask()
        result.masks = []
        Matcher matcher = pattern.matcher(line)
        while (matcher.find()) {
            PairInt p = new PairInt(start: matcher.start(), end: matcher.end())
            result.masks.add(p)
        }
        result
    }

    static QuoteMask doubleQuote(String line) {
        quote(line, doubleQuoteString)
    }

    static QuoteMask singleQuote(String line) {
        quote(line, singleQuoteString)
    }

    // Visible for tests
    List<Integer> rawMasks() {
        List<Integer> result = []
        masks.each {
            result.add(it.start)
            result.add(it.end)
        }
        result
    }

    Boolean masked(Integer i) {
        return masks.any{ it.between(i)}
    }
}

/** Utility for stirng manipulations */
class StringUtil {
    static lastToken(String s, int offset) {
        int i = offset - 1
        while (i >= 0 && !Character.isWhitespace(s.charAt(i))) {
            i = i - 1
        }
        if (i == 0) {
            s
        } else {
            s.substring(i+1, offset)
        }
    }

    static Boolean isControlKeyword(String s) {
        return s.equals("if") || s.equals("while") || s.equals("for")
    }

    static String findToken(String s) {
        Pattern pattern = ~/[^\s]+/
        Matcher matcher = pattern.matcher(s)
        if (matcher.find()) {
            s.substring(matcher.start(), matcher.end())
        } else {
            ""
        }
    }
}

/** 
 * Simple class to represent a position in the source file by
 * line and column number 
 */
class LineColumn {
    Integer line
    Integer column

    static LineColumn endOfLine(Integer l) {
        return new LineColumn(line:l, column:-2)
    }

    boolean equals(obj) {
        if (obj instanceof LineColumn) {
            LineColumn other = obj as LineColumn
            (line == other.line) && (column == other.column)
        } else {
            false
        }
    }

    LineColumn(Integer l, Integer c) {
        line = l
        column = c
    }
}

class Comment {
    LineColumn start
    LineColumn end

    boolean equals(obj) {
        if (obj instanceof Comment) {
            Comment other = obj as Comment
            start.equals(other.start) && end.equals(other.end)
        } else {
            false
        }
    }

    Comment(Integer sl, Integer sc, Integer el, Integer ec) {
        start = new LineColumn(sl, sc)
        end = new LineColumn(el, ec)
    }

    Comment(LineColumn s, LineColumn e) {
        start = s
        end = e
    }
}

class CommentScanner {
    Boolean inComment
    LineColumn start

    List<String> buffer
    Integer lineIdx
    Integer colIdx

    void reset() {
        inComment = false
        start = null
        lineIdx = 0
        colIdx = 0
    }

    Boolean isStartOfComment() {
        (!inComment) &&
        buffer.get(lineIdx).charAt(colIdx) == '/' && 
            (colIdx < buffer.get(lineIdx).size() -1) && 
            (buffer.get(lineIdx).charAt(colIdx +1) == '*')
    }

    Boolean isEndOfComment() {
        (inComment) &&
        buffer.get(lineIdx).charAt(colIdx) == '*' && 
            (colIdx < buffer.get(lineIdx).size() -1) && 
            (buffer.get(lineIdx).charAt(colIdx +1) == '/')
    }

    Boolean isEndOfLine() {
        colIdx == buffer.get(lineIdx).size() - 1
    }

    Boolean isEndOfFile() {
        (lineIdx == buffer.size() - 1) && 
        (colIdx == buffer.get(lineIdx).size() - 1)
    }
    Boolean moveToNext() {
        if (colIdx < buffer.get(lineIdx).size() - 1 ) {
            colIdx++; 
            return true
        } else {
            if (lineIdx < buffer.size() - 1) {
                lineIdx++;
                colIdx = 0
                return true
            } else {
                return false
            }
        }
    }

    List<Comment> scan(List<String> lines) {
        buffer = lines
        reset()
        List<Comment> comments = []

        inComment = isStartOfComment()
        if (inComment) {
            start = new LineColumn(lineIdx, colIdx)
            colIdx++
        }
        while (moveToNext()) {
            if (!inComment) {
                if (isStartOfComment()) {
                    start = new LineColumn(lineIdx, colIdx)
                    inComment = true
                    colIdx++
                } 
            } else {
                if (isEndOfComment()) {
                    def e = new LineColumn(lineIdx, colIdx + 1)
                    comments.add(new Comment(start, e))
                    start = null
                    inComment = false
                    colIdx++
                }
            }
        }
        comments
    }

}

