package me.zhihan.javastyler

import groovy.transform.ToString
import groovy.transform.EqualsAndHashCode
import groovy.transform.CompileStatic

import java.util.regex.Matcher
import java.util.regex.Pattern

/** 
 * Simple class to represent a position in the source file by
 * line and column number 
 */
@ToString
@EqualsAndHashCode
@CompileStatic
class LineColumn {
    int line
    int column

    /** Returns a LineColumn object representing the end of line for the given line number. */
    static LineColumn endOfLine(int l) {
        return new LineColumn(l, -2)
    }

    /* Returns a LineColumn object representing the location. */ 
    LineColumn(int l, int c) {
        line = l
        column = c
    } 

    /** Returns true if this position preceeds the other position. */
    Boolean leq(LineColumn other) {
        if (column > 0) {
            (line < other.line) || 
                ((line == other.line) && (
                    (column <= other.column) || (other.column == -2)))
        } else {
            // end of line
            line < other.line
        }
    }
}

/** 
 * A simple comment in the source file is modeled with a start and end 
 * position. 
 */
@ToString
@EqualsAndHashCode
@CompileStatic
class Comment {
    LineColumn start
    LineColumn end

    /** Create new comment giving start and end positions (line and columns)*/
    Comment(int sl, int sc, int el, int ec) {
        start = new LineColumn(sl, sc)
        end = new LineColumn(el, ec)
    }

    /** Create new comment giving start and end positions (line and columns)*/
    Comment(LineColumn s, LineColumn e) {
        assert(s != null)
        assert(e != null)
        start = s
        end = e
    }

    /** Returns true if the position is contained in the given range. */
    Boolean contains(LineColumn pos) {
        return start.leq(pos) && pos.leq(end)
    }

    /** Test if a position in a file is in comment. */ 
    static Boolean inComment(List<Comment> cmts, int row, int col) {
        cmts.any{ it.contains(new LineColumn(row, col)) }
    }

    /** A predicate that always returns false */
    static Boolean alwaysFalse(int x) {
        return false
    } 
}

/**
 * A simple class that scans a Java source file and process the comments
 */
@CompileStatic
class CommentScanner {
    Boolean inComment = false  // Current position is in comment
    Boolean inQuote = false // Current position is in quote
    LineColumn start = null

    List<String> buffer = []
    Integer lineIdx = 0
    Integer colIdx = 0
    List<Comment> comments = []

    private void reset() {
        inComment = false
        comments.clear()
        start = null
        lineIdx = 0
        colIdx = 0
    }

    // '"' marks the start of a quote
    private Boolean isStartOfQuote() {
        (!inQuote) &&
        (colIdx < buffer.get(lineIdx).size() && 
            buffer.get(lineIdx).charAt(colIdx) == '"')
    }

    // '"' without a leading escape character marks the end of current quote.
    private Boolean isEndOfQuote() {
        inQuote &&
        (colIdx < buffer.get(lineIdx).size() &&
         buffer.get(lineIdx).charAt(colIdx) == '"') && 
        (colIdx > 0 && buffer.get(lineIdx).charAt(colIdx-1) != '\\')

    }

    // '//' marks the start of a line comment
    private Boolean isStartOfLineComment() {
        (!inComment) && 
        (colIdx < buffer.get(lineIdx).size() - 1) && 
            buffer.get(lineIdx).charAt(colIdx) == '/' &&
            (buffer.get(lineIdx).charAt(colIdx + 1) == '/')
    }

    // "/*" marks the start of a comment region
    private Boolean isStartOfComment() {
        (!inComment) &&
        (colIdx < buffer.get(lineIdx).size() - 1) && 
            (buffer.get(lineIdx).charAt(colIdx) == '/') && 
            (buffer.get(lineIdx).charAt(colIdx + 1) == '*')
    }

    // "*/" is the end of a comment region.
    private Boolean isEndOfComment() {
        (inComment) &&
        (colIdx < buffer.get(lineIdx).size() - 1) && 
            buffer.get(lineIdx).charAt(colIdx) == '*' && 
            (colIdx < buffer.get(lineIdx).size() -1) && 
            (buffer.get(lineIdx).charAt(colIdx +1) == '/')
    }

    /** 
      * Move to the next character.
      * @return whether the scanner is at the end of the buffer. 
      */
    private Boolean moveToNext() {
        if (colIdx < buffer.get(lineIdx).size() - 1 ) {
            colIdx++; 
            true
        } else {
            // End of line
            if (lineIdx < buffer.size() - 1) {
                lineIdx++;
                colIdx = 0
                true
            } else {
                // End of buffer
                false
            }
        }
    }

    // Handle the case of starting a region comment.
    private Boolean handleStartComment() {
        if (isStartOfComment()) {
            start = new LineColumn(lineIdx, colIdx)
            inComment = true
            colIdx++
            true
        } else {
            false
        }
    }

    // Handles the case of ending a region comment.
    private Boolean handleEndComment() {
        // Not in comment
        if (isEndOfComment()) {
            def e = new LineColumn(lineIdx, colIdx + 1)
            comments.add(new Comment(start, e))
            start = null
            inComment = false
            colIdx++
            true
        } else {
            false
        }

    }


    // Move to the current end of line
    private void moveToEndOfLine() {
        colIdx = buffer.get(lineIdx).size() - 1
    }

    // Handle the case of line comment and move to the end of line.
    private Boolean handleLineComment() {
        Boolean isStart = isStartOfLineComment()
        if (isStart) {
            // The remainder of the line is comment
            def s = new LineColumn(lineIdx, colIdx)
            def e = LineColumn.endOfLine(lineIdx)
            comments.add(new Comment(s, e))
            moveToEndOfLine()
        }
        isStart
    }


    /** 
     * Scan a file and return a list of comments regions found in the file. 
     *
     * @return List of Comment objects representing the commented regions.
     */
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
            // Only need to process if it is not in Quote
            if (inQuote) {
                if (isEndOfQuote()) {
                    inQuote = false
                    continue
                }
            } else {
                // Not in quote, 
                if (isStartOfQuote()) {
                    if (isStartOfQuote()) {
                        inQuote = true;
                        continue
                    }
                }

                if (!inComment) {
                    if (handleStartComment()) {
                        continue
                    }
                    if (handleLineComment()) {
                        continue
                    }
                } else {
                    if (handleEndComment()) {
                        continue
                    }
                }
            } 
        } 
        comments
    }
}

