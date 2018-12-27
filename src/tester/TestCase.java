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

import java.io.File;
import java.util.ArrayList;

/** A single test case. */
abstract class TestCase extends Test {

  /** The command line string to execute. */
  protected String cmd;

  /** Default constructor. */
  TestCase(String name, String[] context, String cmd) {
    super(name, context);
    this.cmd = cmd;
  }

  /**
   * Calculate the size of this test (i.e., the total number of individual TestCase objects that it
   * contains).
   */
  public int size() {
    return 1;
  }

  /** Flag to indicate whether this test passed or failed last time it was executed. */
  protected boolean passed;

  /** Calculate the total number of test cases that have passed in this test. */
  public int numPassed() {
    return passed ? 1 : 0;
  }

  /** Attempt to run a test that executes a command and captures output in the specified files. */
  protected boolean execTest(
      ArrayList<String> cmds, File expected, File actual, String path, int nesting, int flags)
      throws Exception {
    String nameOut = name + ".out";
    File actualOut = new File(actual, nameOut);
    String nameErr = name + ".err";
    File actualErr = new File(actual, nameErr);

    if ((flags & RUNTESTS) != 0) {
      // Check that we can write to the files for capturing output:
      if (!checkFile(actualOut) || !checkFile(actualErr)) {
        failed(flags, nesting, path, "Cannot access files for capturing output");
        return false;
      }

      // Run the command:
      ProcessBuilder pb = new ProcessBuilder(cmds);
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
        if (actualOut.canRead() && actualErr.canRead()) {
          System.out.println(
              "ISSUE for " + path + ": expected outputs for " + path + " are missing.");
          showContext(flags);
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
        } else {
          System.out.println(
              "ISSUE for "
                  + path
                  + ": Expected and actual outputs are missing; use -r to run tests?");
        }
      }
      failed(flags, nesting, path, "Missing expected outputs");
      return false;
    }
    boolean outSame = sameContent(actualOut, expectedOut);
    boolean errSame = sameContent(actualErr, expectedErr);
    if (!outSame || !errSame) {
      if ((flags & INTERACT) != 0) {
        boolean contextShown = false;
        System.out.println("ISSUE for " + path + ": test did not produce expected outputs.");
        if (!outSame) {
          showContext(flags);
          contextShown = true;
          diff("standard output", expectedOut, actualOut);
          if (ask("Use new output as the expected result for " + path, "yn") == 'y') {
            copy(actualOut, expectedOut);
            outSame = true;
          }
        }
        if (!errSame) {
          if (!contextShown) {
            showContext(flags);
          }
          diff("standard error", expectedErr, actualErr);
          if (ask("Use new error output as expected results for " + path, "yn") == 'y') {
            copy(actualErr, expectedErr);
            errSame = true;
          }
        }
      }
    }
    if (outSame && errSame) {
      progress(flags, nesting, "PASSED " + path);
      return true;
    } else {
      failed(flags, nesting, path, "Test did not produce expected outputs");
      return false;
    }
  }
}
