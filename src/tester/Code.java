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
class Code extends Test {

  /** File extension, including dot. */
  private String ext;

  /** Command to run with this file. */
  private String cmd;

  /** Lines of code. */
  private String[] code;

  /** Default constructor. */
  Code(String name, String ext, String cmd, String[] code) {
    super(name);
    this.ext = ext;
    this.cmd = cmd;
    this.code = code;
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
    message(nesting, "CodeTest: " + cmd + " " + path + ext);

    // Create the test file:
    File src = new File(actual, name + ext);
    writeFile(src, code);

    // Create and run test command:
    ArrayList<String> cmds = new ArrayList();
    addArgs(cmds, cmd);
    cmds.add(src.getAbsolutePath());
    return execTest(working, cmds, expected, actual, path, nesting, flags);
  }
}
