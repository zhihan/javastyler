package me.zhihan.javastyler

import groovy.transform.CompileStatic

/** 
  * Left parenthesis rule
  *
  * Left parenthesis should not follow a whitespace and should not be followed
  * by a whitespace.
  */
@CompileStatic
class LeftParenthesisRule implements SingleLineRule {
    // By default do not ignore anything
    Closure canSkip = { col -> false };

    private int lastNonWhiteSpace(String line, int offset) {
        int pos = offset - 1
        while(pos >= 0 && Character.isWhitespace(line.charAt(pos))) {
            pos = pos - 1
        }
        pos
    }

     /** Analyzes a single line and look for left parenthesis problems. */
    Diagnostics analyze(String line) {
        int offset = 0
        QuoteMask mask = QuoteMask.doubleQuote(line)

        while (offset < line.size()) {
            offset = line.indexOf("(", offset)
            if (offset < 0) {
                return new Pass()
            }
            if (mask.masked(offset) ||
                canSkip(offset)) {
                offset = offset + 1
                continue
            }

            if (offset > 0 && 
                Character.isWhitespace(line.charAt(offset - 1))) {
                int r = lastNonWhiteSpace(line, offset)
                if (r == -1) { // continued line
                    offset = offset + 1
                    continue
                }
                
                String lastToken = StringUtil.lastToken(line, r + 1)
                if (StringUtil.isControlKeyword(lastToken)) {
                    offset = offset + 1
                    continue
                } 
                if (Character.isLetter(line.charAt(r)) ||
                    Character.isDigit(line.charAt(r))) {
                    return new Fail(msg: "Extra space before '(' at $offset")
                } else {
                    offset = offset + 1
                    continue
                }
            }
            if (offset < line.size() - 1 && 
                Character.isWhitespace(line.charAt(offset + 1)) ) {
                return new Fail(msg: "Extra space after '(' at $offset")
            }
            offset = offset + 1
        }
        return new Pass()
    }

    /** A fix can be provided based on the diagnostics */
    Boolean canFix(String line) {
        return true
    }

    private void deleteWhitespaceBackward(StringBuilder sb, int startIdx, int endIdx) {
        int deleteIdx = endIdx - 1
        while (deleteIdx > startIdx) {
            sb.deleteCharAt(deleteIdx)
            deleteIdx = deleteIdx - 1
        }
    }

    private void deleteWhitespaceForward(StringBuilder sb, int pos) {
        while (pos < sb.size() && 
            Character.isWhitespace(sb.charAt(pos))) {
            sb.deleteCharAt(pos)
        }
    }

    /** Return a fixed line */
    String fix(String line) {
        StringBuilder sb = new StringBuilder(line)
        
        int offset = 0
        QuoteMask mask = QuoteMask.doubleQuote(line)
        while (offset < sb.size()) {
            offset = sb.indexOf("(", offset)
            if (offset < 0) {
                break
            }
            if (mask.masked(offset) || 
                canSkip(offset)) {
                offset = offset + 1
                continue
            }
            if (offset > 0 && Character.isWhitespace(sb.charAt(offset - 1))) {
                int r = lastNonWhiteSpace(line, offset)
                if (r == -1) { // continued line
                    offset = offset + 1
                    continue
                }

                String lastToken = StringUtil.lastToken(line, r + 1)
                if (StringUtil.isControlKeyword(lastToken)) {
                    offset = offset + 1
                    continue
                } 
                
                if (Character.isLetter(line.charAt(r)) ||
                    Character.isDigit(line.charAt(r))) {
                    deleteWhitespaceBackward(sb, r, offset)
                    offset = r + 2
                    continue
                } else {
                    offset = offset + 1
                    continue
                }
            }
            if (offset < sb.size() - 1 && 
                Character.isWhitespace(sb.charAt(offset + 1)) ) {
                deleteWhitespaceForward(sb, offset + 1)
            }
            offset = offset + 1
        }
        sb.toString();
    }

    void setSkip(Closure isComment) {
        canSkip = isComment
    }

}

/**
 * No leading spaces
 * 
 * A few tokens should not follow a space, such as ')' or ';'
 */
@CompileStatic
class NoLeadingSpaceRule implements SingleLineRule {
    // By default do not ignore anything
    Closure canSkip = { col -> false };
    void setSkip(Closure isComment) {
        canSkip = isComment
    }

    String token

    /** A fix can be provided based on the diagnostics */
    Boolean canFix(String line) {
        return true
    }

    Diagnostics analyze(String line) {
        int offset = 0
        QuoteMask mask = QuoteMask.doubleQuote(line)

        while (offset >= 0 && offset < line.size()) {
            offset = line.indexOf(token, offset)
            if (offset < 0) {
                break
            }
            if (mask.masked(offset)) {
                offset = offset + 1
                continue
            }
            if (canSkip(offset)) {
                offset = offset + 1
                continue
            }

            if (offset > 0 && Character.isWhitespace(line.charAt(offset - 1))) {
                int r = offset - 1
                while(r >= 0 && Character.isWhitespace(line.charAt(r))) {
                    r = r - 1
                }
                if (r == -1) { // closing line
                    offset = offset + 1
                    continue
                } else {
                    return new Fail(msg: "Leading space before $token at $offset")
                }
            } else {
                offset = offset + 1
            }
        }
        return new Pass()
    }

    String fix(String line) {
        int offset = 0
        QuoteMask mask = QuoteMask.doubleQuote(line)
        StringBuffer sb = new StringBuffer(line)

        while (offset >=0) {
            offset = sb.indexOf(token, offset)
            if (offset <0) {
                break
            }
            if (mask.masked(offset)) {
                offset = offset + 1
                continue
            }
            if (canSkip(offset)) {
                offset = offset + 1
                continue
            }

            if (offset > 0 && Character.isWhitespace(sb.charAt(offset - 1))) {
                int r = offset - 1
                while(r >= 0 && Character.isWhitespace(sb.charAt(r))) {
                    r = r - 1
                }
                if (r == -1) { // closing line
                    offset = offset + 1
                    continue
                } else {
                    int numDeleted
                    while (numDeleted < offset - r - 1) {
                        sb.deleteCharAt(r + 1)
                        numDeleted = numDeleted + 1
                    }
                    offset = r + 2
                    continue
                }
            }
            offset = offset + 1
        }
        return sb.toString()
        return new Pass()
    }

    static SingleLineRule semiColonRule() {
        new NoLeadingSpaceRule(token: ';')
    }

    static SingleLineRule rightParenthesisRule() {
        new NoLeadingSpaceRule(token: ")")
    }
}

class RequireLeadingSpaceRule implements SingleLineRule {
    String token
    // By default do not ignore anything
    Closure canSkip = { col -> false };
    void setSkip(Closure isComment) {
        canSkip = isComment
    }

    Diagnostics analyze(String line) {
        int offset = 0
        QuoteMask mask = QuoteMask.doubleQuote(line)

        while (offset >= 0 && offset < line.size()) {
            offset = line.indexOf(token, offset)
            if (offset < 0) {
                return new Pass()
            }
            if (mask.masked(offset)) {
                offset = offset + 1
                continue
            }
            if (canSkip(offset)) {
                offset += 1
                continue
            }

            if (offset > 0 && !Character.isWhitespace(line.charAt(offset - 1))) {
                return new Fail(msg: "Does not have space before {")
            } 

            if (offset > 1 && 
                Character.isWhitespace(line.charAt(offset - 1)) &&
                Character.isWhitespace(line.charAt(offset - 2))) {
                return new Fail(msg: "Has more than one space before {")
            } else {
                offset = offset + 1
            }
        }
        new Pass()
    }

    Boolean canFix(String line) {
        true
    }

    String fix(String line) {
        StringBuilder sb = new StringBuilder(line)
        int offset = 0
        QuoteMask mask = QuoteMask.doubleQuote(line)

        while (offset >= 0 && offset < sb.size()) {
            offset = sb.indexOf(token, offset)
            if (offset < 0) {
                break
            }
            if (mask.masked(offset)) {
                offset = offset + 1
                continue
            }
           if (canSkip(offset)) {
                offset += 1
                continue
            }


            if (offset > 0 && !Character.isWhitespace(sb.charAt(offset - 1))) {
                sb.insert(offset, ' ')
                offset = offset + 2
                continue
            } 

            if (offset > 1 && 
                Character.isWhitespace(sb.charAt(offset - 1)) &&
                Character.isWhitespace(sb.charAt(offset - 2))) {
                if (sb.substring(0, offset).trim().size() == 0 ) {
                    // All whitespace before {
                    offset = offset + 1
                    continue
                }

                int r = offset - 2
                while (Character.isWhitespace(sb.charAt(r))) {
                    sb.deleteCharAt(r)
                    r = r - 1
                }
                offset = r + 3
                continue
            } else {
                offset = offset + 1
            }
        }
        sb.toString()
    }

    static SingleLineRule openBracketRule() {
        new RequireLeadingSpaceRule(token: "{")
    }
}