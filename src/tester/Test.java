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
import java.io.PrintWriter;
import java.util.ArrayList;

/** Represents a test (either a single test case or a set of tests). */
public abstract class Test {

  /** A label to identify this test. */
  protected String name;

  /** Default constructor. */
  public Test(String name) {
    this.name = name;
  }

  public abstract int size();

  /** Compute a new path name for this test given the enclosing path. */
  protected String extendPath(String path) {
    return (path == null || path.length() == 0) ? name : (path + File.separator + name);
  }

  /**
   * Check that the specified folder exists, creating it if neccessary, returning true on success.
   */
  protected static boolean checkDirectory(File dir) {
    if (dir.exists() ? dir.isDirectory() : dir.mkdir()) {
      return true;
    }
    System.out.println("Cannot access directory \"" + dir.getPath() + "\"");
    return false;
  }

  /** Check that the specified file does not exist or can be written, returning true on success. */
  protected static boolean checkFile(File file) {
    if (!file.exists() || (file.isFile() && file.canWrite())) {
      return true;
    }
    System.out.println("Cannot write to file \"" + file.getPath() + "\"");
    return false;
  }

  /** Write strings to file. */
  protected static void writeFile(File file, String[] lines) throws FileNotFoundException {
    PrintWriter out = new PrintWriter(file);
    for (int i = 0; i < lines.length; i++) {
      out.println(lines[i]);
    }
    out.close();
  }

  /** Parse a string, adding each token as a separate command line argument to the given array. */
  protected static void addArgs(ArrayList<String> args, String str) {
    int l = str.length();
    int i = 0;
    for (; ; ) {
      // Skip whitespace
      for (; i < l && Character.isWhitespace(str.charAt(i)); i++) {
        // skip
      }

      // Reached the end of the line?
      if (i >= l) {
        return;
      }

      // Read argument:
      int s = i; // start of argument string
      for (i++; i < l && !Character.isWhitespace(str.charAt(i)); i++) {
        // skip
      }
      args.add(str.substring(s, i));
    }
  }

  /** Compare the content of two (text) files for equality. */
  public static boolean sameContent(File left, File right) throws Exception {
    BufferedReader lreader = new BufferedReader(new FileReader(left));
    BufferedReader rreader = new BufferedReader(new FileReader(right));
    String lstr, rstr;
    do {
      lstr = lreader.readLine();
      rstr = rreader.readLine();
    } while (lstr != null && rstr != null && lstr.equals(rstr));
    lreader.close();
    rreader.close();
    return (lstr == null) && (rstr == null);
  }

  /** Copy the contents of one (text) file in to another. */
  public void copy(File from, File to) throws Exception {
    BufferedReader fromreader = new BufferedReader(new FileReader(from));
    PrintWriter towriter = new PrintWriter(to);
    String str;
    while ((str = fromreader.readLine()) != null) {
      towriter.println(str);
    }
    fromreader.close();
    towriter.close();
  }

  /** Display a header message on the console with a ruler extending to a fixed width. */
  protected void header(String msg) {
    System.out.print(msg);
    for (int i = msg.length(); i < 72; i++) {
      System.out.print("=");
    }
    System.out.println();
  }

  /** Display the contents of a file on standard output. */
  protected void display(File file) throws Exception {
    BufferedReader reader = new BufferedReader(new FileReader(file));
    String str;
    while ((str = reader.readLine()) != null) {
      System.out.println(str);
    }
    reader.close();
  }

  /** Display differences between two files. */
  protected void diff(String msg, File expected, File actual) throws Exception {
    header(msg);
    ProcessBuilder pb = new ProcessBuilder("diff", "-c", actual.getPath(), expected.getPath());
    pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
    pb.start().waitFor();
    header("");
  }

  /** Display a message at a specified nesting level. */
  protected void message(int nesting, String msg) {
    for (int i = 0; i < nesting; i++) {
      System.out.print("....");
    }
    System.out.println(msg);
  }

  /** Ask the user a question, and only return when they enter a character listed in the options. */
  protected char ask(String question, String options) throws Exception {
    char c;
    do {
      System.out.print(question + " [" + options + "]? ");
      do {
        c = Character.toLowerCase((char) System.in.read());
      } while (c == '\n' || c == '\r');
    } while (options.indexOf(c) < 0);

    return c;
  }

  /** RUNTESTS: 1=>run the tests; 0=>use previously computed results. */
  public static final int RUNTESTS = 1;

  /** INTERACT: 1=>query user when differences are found; 0=>report differences but do not stop. */
  public static final int INTERACT = 2;

  /** Attempt to run a test that executes a command and captures output in the specified files. */
  protected boolean execTest(
      File working,
      ArrayList<String> cmds,
      File expected,
      File actual,
      String path,
      int nesting,
      int flags)
      throws Exception {
    String nameOut = name + ".out";
    File actualOut = new File(actual, nameOut);
    String nameErr = name + ".err";
    File actualErr = new File(actual, nameErr);

    if ((flags & RUNTESTS) != 0) {
      // Check that we can write to the files for capturing output:
      if (!checkFile(actualOut) || !checkFile(actualErr)) {
        message(nesting, "Cannot access files for capturing output");
        return false;
      }

      // Run the command:
      ProcessBuilder pb = new ProcessBuilder(cmds);
      pb.directory(working);
      pb.redirectOutput(actualOut);
      pb.redirectError(actualErr);
      pb.start().waitFor();
    }

    File expectedOut = new File(expected, nameOut);
    File expectedErr = new File(expected, nameErr);
    if (!expectedOut.exists()
        || !expectedOut.isFile()
        || !expectedErr.exists()
        || !expectedErr.isFile()) {
      if ((flags & INTERACT) != 0) {
        System.out.println("ISSUE: expected outputs for " + path + " are missing.");
        header("standard output");
        display(actualOut);
        header("standard error");
        display(actualErr);
        header("");
        if (ask("Use these outputs as the expected results", "yn") == 'y') {
          copy(actualOut, expectedOut);
          copy(actualErr, expectedErr);
          return true;
        }
      }
      message(nesting, "FAILED " + path + ": missing expected outputs");
      return false;
    }
    boolean outSame = sameContent(actualOut, expectedOut);
    boolean errSame = sameContent(actualErr, expectedErr);
    if (!outSame || !errSame) {
      if ((flags & INTERACT) != 0) {
        System.out.println("ISSUE: test did not produce expected outputs.");
        if (!outSame) {
          diff("standard output", expectedOut, actualOut);
          if (ask("Use new output as the expected result", "yn") == 'y') {
            copy(actualOut, expectedOut);
            outSame = true;
          }
        }
        if (!errSame) {
          diff("standard error", expectedErr, actualErr);
          if (ask("Use new outputs as the expected results", "yn") == 'y') {
            copy(actualErr, expectedErr);
            errSame = true;
          }
        }
      }
    }
    if (outSame && errSame) {
      message(nesting, "PASSED " + path);
      return true;
    } else {
      message(nesting, "FAILED " + path + ": test did not produce expected outputs");
      return false;
    }
  }

  /**
   * Run this test using the specified parameters.
   *
   * @param working is the working directory where the test will be executed.
   * @param expected is the folder where expected output files are stored.
   * @param actual is the folder where actual output files are stored.
   * @param path is the path name for this test case (for user display).
   * @param nesting specifies the current nesting level (to determine indentation).
   * @param flags specifies operating flags (RUNTESTS|INTERACT).
   */
  abstract boolean run(
      File working, File expected, File actual, String path, int nesting, int flags)
      throws Exception;
}
