package me.zhihan.javastyler

import groovy.transform.CompileStatic

/**
 * File-based rules, each rule is applied to a file once.
 */
interface FileRule {
    /** Analyzes a file and provide diagnostics */
    Diagnostics analyze(List<String> lines)

    /** Returns true if the file can be fixed */
    Boolean canFix(List<String> lines)

    /** Fixes the problems of the file. */
    List<String> fix(List<String> lines)
}