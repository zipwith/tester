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

  /** Introductory comments. */
  private String[] explain;

  /** The collection of test cases. */
  private Test[] tests;

  /** Default constructor. */
  TestSet(String name, String[] explain, Test[] tests) {
    super(name);
    this.explain = explain;
    this.tests = tests;
  }

  /**
   * Records the number of individual tests in this TestSet (the sum of the number of tests in the
   * children). A negative value indicates that the size has not yet been computed.
   */
  private int size = (-1);

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
   * Run this test using the specified parameters.
   *
   * @param working is the working directory where the test will be executed.
   * @param expected is the folder where expected output files are stored.
   * @param actual is the folder where actual output files are stored.
   * @param path is the path name for this test case (for user display).
   * @param nesting specifies the current nesting level (to determine indentation).
   * @param flags specifies operating flags (RUNTESTS|INTERACT).
   */
  boolean run(File working, File expected, File actual, String path, int nesting, int flags)
      throws Exception {
    path = extendPath(path);

    // Print message to indicate start of test:
    message(nesting, "TestSet " + path + " contains " + tests.length + " tests:");

    // Check that we can access expected and actual folders:
    File expectedDir = new File(expected, name);
    File actualDir = new File(actual, name);
    if (!checkDirectory(expectedDir) || !checkDirectory(actualDir)) {
      return false;
    }

    // Check for duplicate test names
    for (int i = 0; i < tests.length; i++) {
      for (int j = i + 1; j < tests.length; j++) {
        if (tests[i].name.equals(tests[j].name)) {
          System.out.println(
              "Test " + path + " failed: multiple subtests called \"" + tests[i].name + "\"");
          return false;
        }
      }
    }

    // Run individual tests:
    int count = 0;
    for (int i = 0; i < tests.length; i++) {
      if (tests[i].run(working, expectedDir, actualDir, path, nesting + 1, flags)) {
        count++;
      }
      System.out.println();
    }
    message(
        nesting, "TestSet " + path + ": " + count + " out of " + tests.length + " tests passed");
    return (count == tests.length);
  }
}
