package me.zhihan.javastyler

import groovy.transform.CompileStatic
import org.apache.commons.cli.Options
import org.apache.commons.cli.CommandLineParser
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.GnuParser
import org.apache.commons.cli.HelpFormatter

import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

abstract class Diagnostics {
    abstract Boolean passed()
    abstract String message()
}

/**
 * Diagnostics class
 * Either pass or fail.
 */
@CompileStatic
class Pass extends Diagnostics {
    /** Whether it is a pass. Used for tests. */
    Boolean passed() {
        return true
    }

    String message() {
        "Passed"
    }
}

/** Failed diagnostic */
@CompileStatic
class Fail extends Diagnostics {
    String msg
    Boolean passed() {
        return false
    }

    String message() {
        msg
    }
}

/** A diagnostic with line numbers attached */
@CompileStatic
class FailWithLineNumber extends Diagnostics {
    String rule
    List<Integer> lines
    String msg

    Boolean passed() {
        return false
    }

    String message() {
        msg
    }
}

/**
 * Main entry of the analysis 
 */
class Tool {
    /** Print the help message of the tool. */
    static void printHelp(Options options) {
      HelpFormatter fmt = new HelpFormatter()
      fmt.printHelp("Tool", options)  
    }

    private static List<SingleLineRule> singleLineRules() {
        [ 
            new TrailingSpaceRule(), 
            new LineWidthRule(),
            new LeadingTabRule(),
            new LeftParenthesisRule(),
            NoLeadingSpaceRule.semiColonRule(),
            NoLeadingSpaceRule.rightParenthesisRule()
        ]
    }

    private static List<MultiLineRule> multiLineRules() {
        [
            new EmptyLinesRule()
        ]
    }

    static void main(String[] args) {
        Options options = new Options()
        options.addOption("f", "file", true, "Enter file name")
        options.addOption("h", "help", false, "Display help")
        options.addOption("c", "change", false, "Change the file (a backup will be saved)" )

        CommandLineParser parser = new GnuParser();
        CommandLine cmd = parser.parse( options, args);

        if (cmd.hasOption("h")) {
            printHelp(options)
            return
        } 

        // Analyze a file
        Boolean hasFailed = false
        if (cmd.hasOption("f")) {
            String fileName = cmd.getOptionValue("f")
            List<String> lines = new File(fileName).readLines()

            List<Diagnostics> result = analyzeSingle(lines, singleLineRules())
            hasFailed = !result.isEmpty()
            report(result)

            result = analyzeMulti(lines, multiLineRules())
            report(result)

            if (cmd.hasOption("c") && hasFailed) {
                Path theFile = Paths.get(fileName)
                List<String> fixed = fixSingle(lines, singleLineRules())

                Files.move(theFile, theFile.resolveSibling(fileName + ".bak"))
                Files.write(theFile, fixed, Charset.forName("US-ASCII"))
            }
        }
    }

    @CompileStatic
    static void report(List<Diagnostics> results) {
        for (Diagnostics diag in results) {
            if (!diag.passed()) {
                FailWithLineNumber d = diag as FailWithLineNumber
                String lineNos = d.lines
                    .collect{int i -> i+1}  // Convert from 0-based to 1-based indexing
                    .join(",")
                println("Problem found $d.rule at $lineNos")
            }
        }
    }
    
    /**
     * Analyze the whole file using a single line rule.
     */
    @CompileStatic
    static Diagnostics analyze(List<String> lines, SingleLineRule rule) {
        List<Integer> problems = []
        List<Diagnostics> diags = []
        CommentScanner scanner = new CommentScanner()
        final List<Comment> comments = scanner.scan(lines)

        for (int i = 0; i < lines.size(); i++) {
            // Set a filter on whether to skip in the current line.
            rule.setSkip{ int col -> Comment.inComment(comments, i, col) }
            Diagnostics diag = rule.analyze(lines.get(i))
            if (!diag.passed()) {
                problems.add(i)
                diags.add(diag)
            }
        }
        if (!problems.isEmpty()) {
            String ruleName = rule.class.name
            new FailWithLineNumber(rule: ruleName, lines: problems)
        } else {
            new Pass()
        }
    }

    /**
     * Analyze the whole file using a single line rule.
     */
    @CompileStatic
    static List<String> fix(List<String> lines, SingleLineRule rule) {
        CommentScanner scanner = new CommentScanner()
        final List<Comment> comments = scanner.scan(lines)

        List<String> result = new ArrayList<String>()
        lines.eachWithIndex{ String line, int i -> 
            // Set a filter on whether to skip in the current line.
            rule.setSkip{ int col -> Comment.inComment(comments, i, col) }
            String r = rule.fix(line)
            result.add(r)
        }
        result
    }

    @CompileStatic
    static List<String> fixSingle(List<String> lines, List<SingleLineRule> rules) {
        List<String> results = lines
        for (SingleLineRule rule in rules) {
            results = fix(results, rule)
        }
        results
    }

    /**
     * Analyze the whole file using multiple rules.
     */
    @CompileStatic
    static List<Diagnostics> analyzeSingle(List<String> lines, List<SingleLineRule> rules) {
        rules
            .collect{SingleLineRule rule -> analyze(lines, rule)}
            .findAll{Diagnostics diag -> !diag.passed()}
    }

    @CompileStatic
    static Diagnostics analyze(List<String> lines, MultiLineRule rule) {
        CommentScanner scanner = new CommentScanner()
        final List<Comment> comments = scanner.scan(lines)

        rule.setCanSkip{ int row, int col -> Comment.inComment(comments, row, col) }
        return rule.analyze(lines)
    }

    @CompileStatic
    static List<Diagnostics> analyzeMulti(List<String> lines, List<MultiLineRule> rules) {
        rules
            .collect{MultiLineRule rule -> analyze(lines, rule)}
            .findAll{Diagnostics diag -> !diag.passed()}
    }

    /** Fix the source file using a multi-line rule.*/
    @CompileStatic
    static List<String> fix(List<String> lines, MultiLineRule rule) {
        CommentScanner scanner = new CommentScanner()
        final List<Comment> comments = scanner.scan(lines)

        rule.setCanSkip{ int row, int col -> Comment.inComment(comments, row, col) }
        if (rule.canFix(lines)) { 
            rule.fix(lines)
        } else {
            lines
        }
    }
} 

