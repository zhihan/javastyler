package me.zhihan.javastyler

interface MultiLineRule {
    /** Analyze a file and provide diagnostics */
    Diagnostics analyze(List<String> lines)

    Boolean canFix(List<String> lines)

    List<String> fix(List<String> lines)

    void setSkip(Closure isComment)
}

/**
 * Multiple empty lines rules
 *
 * There should not be two or more consecutive empty lines
 */
class EmptyLinesRule implements MultiLineRule {
    Closure canSkip = { col, row -> false }

    private Boolean isEmpty(String line) {
        line.trim().empty
    }

    List<PairInt> findRegions(List<String> lines) {
        List<Boolean> empty = lines.collect { isEmpty(it) }
        List<PairInt> regions = []

        Boolean inEmptyRegion = false
        Integer start = 0
        for (int i = 0; i < empty.size(); i++) {
            if (!canSkip(i, 0)) { // Not in the comment
                if (inEmptyRegion && !empty[i]) {
                    if (i - start > 1) {
                        regions.add(new PairInt(start: start, end: i))
                    } 
                    inEmptyRegion = false
                } else if (!inEmptyRegion && empty[i]) {
                    inEmptyRegion = true
                    start = i
                }
            }
        }
        regions
    }

    Diagnostics analyze(List<String> lines) {
        List<PairInt> regions = findRegions(lines)
        if (regions.empty) {
            return new Pass()
        } else {
            List<Integer> problems = regions.collect{ it.start }
            return new FailWithLineNumber(rule:this.class.name, lines: problems)
        }
    }

    Boolean canFix(List<String> lines) {
        true
    }

    List<String> fix(List<String> lines) {
        List<Boolean> empty = lines.collect { isEmpty(it) }
        List<String> result = []
        Boolean inEmptyRegion = false
        Integer start = 0

        for (int i = 0; i < empty.size(); i++) {
            if (!canSkip(i, 0)) { // Not in the comment
                if (inEmptyRegion && !empty[i]) {
                    inEmptyRegion = false
                } else if (!inEmptyRegion && empty[i]) {
                    inEmptyRegion = true
                    result.add("")
                }

                if (!empty[i]) {
                    result.add(lines[i])
                }
            } else {
                // commented out line
                result.add(lines[i])
            }
        }
        result
    }

    void setSkip(Closure isComment) {
        canSkip = isComment
    }
}