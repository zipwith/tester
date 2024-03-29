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

/** A test case that is described by a code fragment. */
class Code extends TestCase {

  /** File extension, including dot. */
  private String ext;

  /** Default constructor. */
  Code(String name, String[] context, String cmd, String ext) {
    super(name, context, cmd);
    this.ext = ext;
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
    File src = new File(actual, name + ext);
    progress(flags, nesting, "CodeTest: " + cmd + " " + src);

    // Create the test file:
    writeFile(src, context);

    // Create and run test command:
    ArrayList<String> cmds = new ArrayList();
    addArgs(cmds, cmd);
    cmds.add(src.getPath());
    passed = execTest(cmds, cmd + " " + src.getPath(), expected, actual, path, nesting, flags);
  }
}
