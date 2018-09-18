/*
    Copyright 2018 Mark P Jones, Portland State University

    This file is part of tester.

    tester is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    tester is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with tester.  If not, see <https://www.gnu.org/licenses/>.
*/
package tester;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/** Code for parsing test cases from text files. */
public class TestParser {

  /** Source file name to use in error messages. */
  private String filename;

  /** A buffered reader for the test cases. */
  private BufferedReader reader;

  /** Nesting level for this test parser. */
  private int nesting;

  /** Default constructor. */
  public TestParser(String filename, BufferedReader reader, int nesting) {
    this.filename = filename;
    this.reader = reader;
    this.nesting = nesting;
  }

  /** Indicates the end of file. */
  private static final int EOF = (-1);

  /** Indicates a plain line of text. */
  private static final int TEXT = 0;

  /** A line beginning with "tests:" */
  private static final int TESTS = 1;

  /** A line beginning with "exec:" */
  private static final int EXEC = 2;

  /** A line beginning with ".<ext>:" */
  private static final int CODE = 3;

  /** Holds the type (TEXT/TESTS/COMMAND/CODE) of the most recently read line. */
  private int type;

  /** Holds the text of the most recently read line. */
  private String line = "";

  /** The current line number. */
  private int lineNo = 0;

  /** Display an error message and halt the program. */
  private void error(String msg) {
    System.err.println("ERROR in \"" + filename + "\", line " + lineNo + ":" + msg);
    System.exit(-1);
  }

  /** Read the next line from the input and return a code describing its type. */
  private int nextLine() {
    try {
      if (line != null) {
        line = reader.readLine();
        lineNo++;
        if (line == null) {
          reader.close();
        } else if (matches("tests:")) {
          return type = TESTS;
        } else if (matches("exec:")) {
          return type = EXEC;
        } else if (fileExt()) {
          return type = CODE;
        } else {
          return type = TEXT;
        }
      }
    } catch (IOException e) {
      error("IO exception while reading input");
    }
    return type = EOF;
  }

  /**
   * Determine whether the current line begins with the specified prefix, stripping off the prefix
   * and an test name from the line if there is a match.
   */
  private boolean matches(String prefix) {
    if (line.startsWith(prefix)) {
      extractName(prefix.length());
      return true;
    }
    return false;
  }

  /** The text of the most recently read test name. */
  private String name = "";

  /** Determine whether the specified character is value in a test name. */
  private static boolean isNameChar(char c) {
    return Character.isLetterOrDigit(c) || c == '-' || c == '_';
  }

  /** Separate an initial test name from the current line, starting at position s. */
  private void extractName(int s) {
    int l = line.length();
    while (s < l && Character.isWhitespace(line.charAt(s))) {
      s++;
    }
    if (s >= l || !isNameChar(line.charAt(s))) {
      error("Missing test name");
    }
    int n = s + 1;
    while (n < l && isNameChar(line.charAt(n))) {
      n++;
    }
    name = line.substring(s, n);
    line = line.substring(n);
  }

  /** The text of the most recently read file extension, including the leading dot. */
  private String fileExt = "";

  /**
   * Determine whether the current line begins with a .<ext>: prefix for some file extension
   * comprising letters and digits, separating out the extension, a subsequent test name, and the
   * rest of the line and then returning true if such an extension is found.
   */
  private boolean fileExt() {
    if (line.startsWith(".")) {
      int s = 1;
      int l = line.length();
      while (s < l && isNameChar(line.charAt(s))) {
        s++;
      }
      if (s < l && line.charAt(s) == ':') {
        fileExt = line.substring(0, s);
        extractName(s + 1);
        return true;
      }
    }
    return false;
  }

  /**
   * Read a sequence of text lines from the input, with parameter n specifying how many lines have
   * been read already in this text block. (n should be zero for first call.)
   */
  private String[] readStrings(int n) {
    if (nextLine() == TEXT) { // Found a line of text
      String str = line; // Read the nth text string
      String[] strs = readStrings(n + 1); // Read the rest of the strings
      strs[n] = str; // Save the nth text string
      return strs;
    } else { // At the end of the text block
      return new String[n]; // Allocate array for strings
    }
  }

  /** Specify a limit on maximum nesting of test cases within test cases. */
  public static final int MAX_NESTING = 6;

  /** Read a test, assuming that the current line is not TEXT or EOF. */
  private Test parseTest(File parent) throws Exception {
    switch (type) {
      case TESTS:
        {
          if (nesting >= MAX_NESTING) {
            error("Input exceeds maximum test file nesting (" + MAX_NESTING + " levels)");
          }
          try {
            TestSet folder = readTestSet(parent, name, nesting + 1);
            while (nextLine() == TEXT) {
              if (firstNonWhitespace() >= 0) {
                error("Text line is not part of a test case.");
              }
            }
            return folder;
          } catch (FileNotFoundException e) {
            error("Test file \"" + name + "\" not found");
            return null; /* not reached */
          }
        }

      case EXEC:
        {
          String n = name;
          String cmd = checkCommand();
          return new Exec(n, cmd, readStrings(0));
        }

      case CODE:
        {
          String n = name;
          String ext = fileExt;
          String cmd = checkCommand();
          return new Code(n, ext, cmd, readStrings(0));
        }

      default:
        error("expected start of test case");
        return null; /* not reached */
    }
  }

  /**
   * Determine if the current line is blank (or contains only whitespace), returning index of first
   * non whitespace character, or (-1) for a blank line.
   */
  private int firstNonWhitespace() {
    int l = line.length();
    int i = 0;
    while (i < l && Character.isWhitespace(line.charAt(i))) {
      i++;
    }
    return (i >= l) ? (-1) : i;
  }

  /** Check that the provided command line, ignoring leading whitespace, is nonempty. */
  private String checkCommand() {
    int i = firstNonWhitespace();
    if (i < 0) {
      error("missing command");
    }
    return line.substring(i);
  }

  /** Read the rest of this file as an array of tests, having already read n tests. */
  private Test[] parseTests(File parent, int n) throws Exception {
    if (type == EOF) {
      return new Test[n];
    } else {
      Test test = parseTest(parent);
      Test[] tests = parseTests(parent, n + 1);
      tests[n] = test;
      return tests;
    }
  }

  /** Parse the input as a TestSet: some explanatory text followed by some number of test cases. */
  private TestSet parseTestSet(File parent, String name) throws Exception {
    String[] explain = readStrings(0);
    return new TestSet(name, explain, parseTests(parent, 0));
  }

  /** Read a set of tests from the named file at the given nesting level. */
  protected static TestSet readTestSet(File parent, String name, int nesting) throws Exception {
    File folder = new File(parent, name);
    File tests = new File(folder, TestSet.root);
    BufferedReader reader = new BufferedReader(new FileReader(tests));
    TestParser parser = new TestParser(tests.getPath(), reader, nesting);
    return parser.parseTestSet(folder, name);
  }

  /** Read a set of tests from the named file at the top level (nesting level zero). */
  public static TestSet readTestSet(File parent, String name) throws Exception {
    return readTestSet(parent, name, 0);
  }
}
