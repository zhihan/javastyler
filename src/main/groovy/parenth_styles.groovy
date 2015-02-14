package me.zhihan.javastyler

/** 
  * Left parenthesis rule
  *
  * Left parenthesis should not follow a whitespace and should not be followed
  * by a whitespace.
  */
class LeftParenthesisRule implements SingleLineRule {
    Diagnostics analyze(String line) {
        int offset = 0
        QuoteMask mask = QuoteMask.doubleQuote(line)

        while (offset >= 0 && offset < line.size()) {
            offset = line.indexOf("(", offset)
            if (offset <0) {
                return new Pass()
            }
            if (mask.masked(offset)) {
                offset = offset + 1
                continue
            }

            if (offset > 0 && Character.isWhitespace(line.charAt(offset - 1))) {
                int r = offset - 1
                while(r >= 0 && Character.isWhitespace(line.charAt(r))) {
                    r = r - 1
                }
                if (r == -1) { // continued line
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
            if (offset < line.size() - 2 && Character.isWhitespace(line.charAt(offset + 1)) ) {
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

    /** Return a fixed line */
    String fix(String line) {
        StringBuilder sb = new StringBuilder(line)
        
        int offset = 0
        QuoteMask mask = QuoteMask.doubleQuote(line)
        while (offset >= 0 && offset < sb.size()) {
            offset = sb.indexOf("(", offset)
            if (offset < 0) {
                break
            }
            if (mask.masked(offset)) {
                offset = offset + 1
                continue
            }
            if (offset > 0 && Character.isWhitespace(sb.charAt(offset - 1))) {
                int r = offset - 1
                while(r >= 0 && Character.isWhitespace(sb.charAt(r))) {
                    r = r - 1
                }
               if (r == -1) { // continued line
                    offset = offset + 1
                    continue
                }

                if (Character.isLetter(line.charAt(r)) ||
                    Character.isDigit(line.charAt(r))) {
                    int deleteIdx = offset - 1
                    while (deleteIdx > r) {
                        sb.deleteCharAt(deleteIdx)
                        deleteIdx = deleteIdx - 1
                    }
                    offset = r + 2
                    continue
                } else {
                    offset = offset + 1
                    continue
                }
            }
            if (offset < sb.size() - 2 && Character.isWhitespace(sb.charAt(offset + 1)) ) {
                int r2 = offset + 1
                while (r2 < sb.size() - 1 && Character.isWhitespace(sb.charAt(r2))) {
                    sb.deleteCharAt(r2)
                }
                offset = r2
                continue
            }
            offset = offset + 1
        }
        return sb.toString();
    }    
}

/**
 * No leading spaces
 * 
 * A few tokens should not follow a space, such as ')' or ';'
 */
class NoLeadingSpaceRule implements SingleLineRule {
    String token;

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
                return new Pass()
            }
            if (mask.masked(offset)) {
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
            offset = line.indexOf(token, offset)
            if (offset <0) {
                break
            }
            if (mask.masked(offset)) {
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
                    int numDeleted
                    while (numDeleted < offset - r - 1) {
                        sb.deleteCharAt(r + 1)
                        numDeleted = numDeleted + 1
                    }
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