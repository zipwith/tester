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

/** A set of test cases. */
class TestSet extends Test {

  /** The collection of test cases. */
  private Test[] tests;

  /** Default constructor. */
  TestSet(String name, String[] context, Test[] tests) {
    super(name, context);
    this.tests = tests;
  }

  /**
   * Records the number of individual tests in this TestSet (the sum of the number of tests in the
   * children). A negative value indicates that the size has not yet been computed.
   */
  private int size = (-1);

  /**
   * Calculate the size of this test (i.e., the total number of individual TestCase objects that it
   * contains).
   */
  public int size() {
    if (size < 0) {
      size = 0;
      for (int i = 0; i < tests.length; i++) {
        size += tests[i].size();
      }
    }
    return size;
  }

  /**
   * Records the total number of test cases that have passed (or -1 if that calculation has not been
   * performed).
   */
  int numPassed = (-1);

  /** Calculate the total number of test cases that have passed in this test. */
  public int numPassed() {
    if (numPassed < 0) {
      numPassed = 0;
      for (int i = 0; i < tests.length; i++) {
        numPassed += tests[i].numPassed();
      }
    }
    return numPassed;
  }

  /**
   * Display a tree with summary statistics for each section of a test set. (Poor complexity in
   * principle, but probably good enough in practice.)
   */
  public void displayTestTree(int nesting) {
    for (int i = 0; i < nesting; i++) {
      System.out.print("| ");
    }
    int n = numPassed();
    int s = size();
    String alert = (n != s) ? "  <<<<<<<" : "";
    System.out.println(name + ": passed " + n + " of " + s + " tests" + alert);
    for (int i = 0; i < tests.length; i++) {
      tests[i].displayTestTree(nesting + 1);
    }
  }

  /** Name of root file where tests are stored. */
  public static final String root = "tests";

  /**
   * Run this test using the specified parameters.
   *
   * @param expected is the folder where expected output files are stored.
   * @param actual is the folder where actual output files are stored.
   * @param path is the path name for this test case (for user display).
   * @param nesting specifies the current nesting level (to determine indentation).
   * @param flags specifies operating flags (RUNTESTS|INTERACT).
   */
  void run(File expected, File actual, String path, int nesting, int flags) throws Exception {
    path = extendPath(path);

    // Print message to indicate start of test:
    progress(flags, nesting, "Test set " + path + " contains " + tests.length + " tests:");
    numPassed = (-1); // reset counts from a previous run

    // Check that we can access expected and actual folders:
    File expectedDir = new File(expected, name);
    File actualDir = new File(actual, name);
    if (!checkDirectory(expectedDir)) {
      failed(flags, nesting, path, expectedDir.toString(), "Error with expected output directory");
      return;
    } else if (!checkDirectory(actualDir)) {
      failed(flags, nesting, path, actualDir.toString(), "Error with actual output directory");
      return;
    }

    // Check for duplicate test names
    for (int i = 0; i < tests.length; i++) {
      if (tests[i].name.equals(TestSet.root)) {
        failed(flags, nesting, path, "", "Test name \"" + TestSet.root + "\" is reserved");
        return;
      }
      for (int j = i + 1; j < tests.length; j++) {
        if (tests[i].name.equals(tests[j].name)) {
          failed(flags, nesting, path, "", "Multiple subtests called \"" + tests[i].name + "\"");
          return;
        }
      }
    }

    // Run individual tests:
    for (int i = 0; i < tests.length; i++) {
      tests[i].run(expectedDir, actualDir, path, nesting + 1, flags);
      progress(flags, 0, "");
    }
    summary(flags, nesting, path + ": passed " + numPassed() + " of " + size() + " tests");
  }
}
