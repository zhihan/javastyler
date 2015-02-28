package me.zhihan.javastyler

import groovy.transform.ToString
import groovy.transform.EqualsAndHashCode

import java.util.regex.Matcher
import java.util.regex.Pattern

/** 
 * Simple class to represent a position in the source file by
 * line and column number 
 */
@ToString
@EqualsAndHashCode
class LineColumn {
    Integer line
    Integer column

    static LineColumn endOfLine(Integer l) {
        return new LineColumn(l, -2)
    }

    LineColumn(Integer l, Integer c) {
        line = l
        column = c
    } 

    /** Returns true if this position preceeds the other position. */
    Boolean leq(LineColumn other) {
        if (column > 0) {
            (line < other.line) || 
                ((line == other.line) && (column <= other.column))
        } else {
            // end of line
            line < other.line
        }
    }
}

@ToString
@EqualsAndHashCode
class Comment {
    LineColumn start
    LineColumn end

    Comment(Integer sl, Integer sc, Integer el, Integer ec) {
        start = new LineColumn(sl, sc)
        end = new LineColumn(el, ec)
    }

    Comment(LineColumn s, LineColumn e) {
        start = s
        end = e
    }

    Boolean contains(LineColumn pos) {
        return start.leq(pos) && pos.leq(end)
    }
}

/**
 * A simple class that scans a Java source file and process the comments
 */
class CommentScanner {
    Boolean inComment
    Boolean inLineComment
    LineColumn start

    List<String> buffer
    Integer lineIdx
    Integer colIdx
    List<Comment> comments = []

    private void reset() {
        inComment = false
        inLineComment = false
        comments.clear()
        start = null
        lineIdx = 0
        colIdx = 0
    }

    // '//' marks the start of a line comment
    private Boolean isStartOfLineComment() {
        (!inComment) && 
        buffer.get(lineIdx).charAt(colIdx) == '/' &&
            (colIdx < buffer.get(lineIdx).size() - 1) && 
            (buffer.get(lineIdx).charAt(colIdx + 1) == '/')
    }

    // "/*" marks the start of a comment region
    private Boolean isStartOfComment() {
        (!inComment) &&
        buffer.get(lineIdx).charAt(colIdx) == '/' && 
            (colIdx < buffer.get(lineIdx).size() - 1) && 
            (buffer.get(lineIdx).charAt(colIdx + 1) == '*')
    }

    private Boolean isEndOfComment() {
        (inComment) &&
        buffer.get(lineIdx).charAt(colIdx) == '*' && 
            (colIdx < buffer.get(lineIdx).size() -1) && 
            (buffer.get(lineIdx).charAt(colIdx +1) == '/')
    }

    private Boolean isEndOfLine() {
        colIdx == buffer.get(lineIdx).size() - 1
    }

    private Boolean isEndOfFile() {
        (lineIdx == buffer.size() - 1) && 
        (colIdx == buffer.get(lineIdx).size() - 1)
    }

    private Boolean moveToNext() {
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

    private void moveToEndOfLine() {
        colIdx = buffer.get(lineIdx).size() - 1
    }

    private void handleLineComment() {
        Boolean isStart = isStartOfLineComment()
        if (isStart) {
            // The remainder of the line is comment
            def s = new LineColumn(lineIdx, colIdx)
            def e = LineColumn.endOfLine(lineIdx)
            comments.add(new Comment(s, e))
            moveToEndOfLine()
        }
    }

    /** Scan a file and return a list of comments regions found in the file. */
    List<Comment> scan(List<String> lines) {
        buffer = lines
        reset()
    
        inComment = isStartOfComment()
        if (inComment) {
            start = new LineColumn(lineIdx, colIdx)
            colIdx++
        }

        handleLineComment() // If the remainder is line comment, process it

        while (moveToNext()) {
            if (!inComment) {
                if (isStartOfComment()) {
                    start = new LineColumn(lineIdx, colIdx)
                    inComment = true
                    colIdx++
                } else {
                    handleLineComment()
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

