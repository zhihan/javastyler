package me.zhihan.javastyler

import groovy.transform.CompileStatic

/** Rules that applies to multiple lines. */
interface MultiLineRule {
    /** Analyze a file and provide diagnostics */
    Diagnostics analyze(List<String> lines)

    Boolean canFix(List<String> lines)

    List<String> fix(List<String> lines)

    void setCanSkip(Closure isComment)
}

/**
 * Multiple empty lines rules
 *
 * There should not be two or more consecutive empty lines
 */
@CompileStatic
class EmptyLinesRule implements MultiLineRule {
    private Closure canSkip = { int col, int row -> false }

    // Returns true if the line contains only whitespaces
    private Boolean isEmpty(String line) {
        line.trim().empty
    }

    // Find regions of empty lines
    private List<PairInt> findRegions(List<String> lines) {
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

    /** Analyze multiple lines. */
    Diagnostics analyze(List<String> lines) {
        List<PairInt> regions = findRegions(lines)
        if (regions.empty) {
            return new Pass()
        } else {
            List<Integer> problems = regions.collect{ it.start }
            return new FailWithLineNumber(rule: this.class.name, lines: problems)
        }
    }

    Boolean canFix(List<String> lines) {
        true
    }

    /** Fix multiple lines. */
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

    void setCanSkip(Closure isComment) {
        canSkip = isComment
    }
}