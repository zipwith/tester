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

/** A test case that is described by executing command line. */
class Exec extends TestCase {

  /** Default constructor. */
  Exec(String name, String[] context, String cmd) {
    super(name, context, cmd);
  }

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
    progress(flags, nesting, "Exec " + path + ": " + cmd);

    // Create and run test command:
    ArrayList<String> cmds = new ArrayList();
    addArgs(cmds, cmd);
    passed = execTest(cmds, cmd, expected, actual, path, nesting, flags);
  }
}
