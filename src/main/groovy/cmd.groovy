package me.zhihan.javastyler

import java.util.List

import org.apache.commons.cli.Options
import org.apache.commons.cli.CommandLineParser
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.GnuParser
import org.apache.commons.cli.HelpFormatter

abstract class Diagnostics {
    abstract Boolean passed()
    abstract String message()
}

/**
 * Diagnostics 
 * Either pass or fail.
 */
class Pass extends Diagnostics {
    Boolean passed() {
        return true
    }

    String message() {
        "Passed"
    }
}

class Fail extends Diagnostics {
    String msg
    Boolean passed() {
        return false
    }

    String message() {
        msg
    }
}

class FailWithLineNumber extends Diagnostics {
    String rule
    List<Integer> lines

    Boolean passed() {
        return false
    }

    String message() {
        msg
    }
}

/**
 * Main entry of the analysis 
 *
 */
class Tool {
    static void printHelp(Options options) {
      HelpFormatter fmt = new HelpFormatter()
      fmt.printHelp("Tool", options)  
    }

    static List<SingleLineRule> singleLineRules() {
        [ 
            new TrailingSpaceRule(), 
            new LineWidthRule(),
            new LeadingTabRule(),
            new LeftParenthesisRule(),
            NoLeadingSpaceRule.semiColonRule(),
            NoLeadingSpaceRule.rightParenthesisRule()
        ]
    }

    static void main(String[] args) {

        Options options = new Options()
        options.addOption("f", "file", true, "Enter file name")
        options.addOption("h", "help", false, "Display help")

        CommandLineParser parser = new GnuParser();
        CommandLine cmd = parser.parse( options, args);

        if (cmd.hasOption("h")) {
            printHelp(options)
            return
        } 

        if (cmd.hasOption("f")) {
            String fileName = cmd.getOptionValue("f")
            List<String> lines = new File(fileName).readLines()
            List<Diagnostics> result = analyze(lines, singleLineRules())
            report(result)
        }
    }


    static void report(List<Diagnostics> results) {
        for (diag in results) {
            if (!diag.passed()) {
                String lineNos = diag.lines.join(",")
                println("Problem found $diag.rule at $lineNos")
            }
        }
    }
    /**
     * Analyze the whole file using a single line rule.
     */
    static Diagnostics analyze(List<String> lines, SingleLineRule rule) {
        List<Integer> problems = []
        List<Diagnostics> diags = []
        CommentScanner scanner = new CommentScanner()
        final List<Comment> comments = scanner.scan(lines)

        for (int i = 0; i < lines.size(); i++) {
            // Set a filter on whether to skip in the current line.
            rule.setSkip{ col -> Comment.inComment(comments, i, col) }
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

    static List<Diagnostics> analyze(List<String> lines, List<SingleLineRule> rules) {
        rules.collect{rule -> analyze(lines, rule)}
    }

} 

