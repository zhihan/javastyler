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
    Integer line
    Integer column

    /** Returns a LineColumn object representing the end of line for the given line number. */
    static LineColumn endOfLine(Integer l) {
        return new LineColumn(l, -2)
    }

    /* Returns a LineColumn object representing the location. */ 
    LineColumn(Integer l, Integer c) {
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

    /** Test if a position in a file is in comment. */ 
    static Boolean inComment(List<Comment> cmts, Integer row, Integer col) {
        cmts.any{ it.contains(new LineColumn(row, col)) }
    }

    static Boolean alwaysFalse(int x) {
        return false
    } 
}

/**
 * A simple class that scans a Java source file and process the comments
 */
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
            (colIdx < buffer.get(lineIdx).size() - 1) && 
            (buffer.get(lineIdx).charAt(colIdx + 1) == '/')
    }

    // "/*" marks the start of a comment region
    private Boolean isStartOfComment() {
        (!inComment) &&
        (colIdx < buffer.get(lineIdx).size() - 1) && 
            (buffer.get(lineIdx).charAt(colIdx) == '/') && 
            (colIdx < buffer.get(lineIdx).size() - 1) && 
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

    // Move to the next character
    // Returns whether the scanner is at the end of the file.
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

    // Move to the current end of line
    private void moveToEndOfLine() {
        colIdx = buffer.get(lineIdx).size() - 1
    }

    // Handle the case of line comment and move to the end of line.
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

    /** 
     * Scan a file and return a list of comments regions found in the file. 
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
            if (!inQuote) {
                // Mark quote if 
                if (isStartOfQuote()) {
                    if (isStartOfQuote()) {
                        inQuote = true;
                        continue
                    }
                }

                if (!inComment) {
                    // Handle region comment
                    if (isStartOfComment()) {
                        start = new LineColumn(lineIdx, colIdx)
                        inComment = true
                        colIdx++
                    } else {
                        handleLineComment()
                    }
                } else {
                    // Not in comment
                    if (isEndOfComment()) {
                        def e = new LineColumn(lineIdx, colIdx + 1)
                        comments.add(new Comment(start, e))
                        start = null
                        inComment = false
                        colIdx++
                    }
                }
            } else {
                if (isEndOfQuote()) {
                    inQuote = false
                    continue
                }
            }
        } 
        comments
    }
}

