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

public class Main {

  /** A command line driver for the tester tool. */
  public static void main(String[] args) {
    if (args.length == 0) {
      System.out.println("usage: tester options ...");
      System.out.println("options: -wdir  set the working directory for test runs to dir");
      System.out.println("         -r    run tests (dryrun without this option)");
      System.out.println("         -i    enable interaction to update expected results");
      System.out.println("         -c    do not print context for tests during interaction");
      System.out.println("         -q    quiet; do not print test progress messages");
      System.out.println("         -f    do not print test failed messages");
      System.out.println("         -s    do not print test set summaries");
      System.exit(0);
    }
    try {
      int flags = 0;
      File working = null;
      File home = new File(".");
      for (int i = 0; i < args.length; i++) {
        if (args[i].startsWith("-")) {
          int l = args[i].length();
          if (l <= 1) {
            System.out.println("Missing option characters");
            System.exit(-1);
          }
          if (args[i].charAt(1) == 'w') {
            File nworking = new File(args[i].substring(2));
            if (!nworking.exists() || !nworking.isDirectory()) {
              System.out.println("Invalid working directory \"" + nworking.getPath() + "\"");
              System.exit(-1);
            }
            working = nworking;
          } else {
            for (int j = 1; j < l; j++) {
              switch (args[i].charAt(j)) {
                case 'r':
                  flags |= Test.RUNTESTS;
                  break;
                case 'i':
                  flags |= Test.INTERACT;
                  break;
                case 'q':
                  flags |= Test.QUIET;
                  break;
                case 's':
                  flags |= Test.SUMMARY;
                  break;
                case 'f':
                  flags |= Test.FAILED;
                  break;
                case 'c':
                  flags |= Test.CONTEXT;
                  break;
                default:
                  System.out.println("Unknown command line flag " + args[i].charAt(j));
                  System.exit(-1);
              }
            }
          }
        } else {
          File expected = new File("expected");
          if (!Test.checkDirectory(expected)) {
            System.out.println("Unable to access or create folder \"" + expected.getPath() + "\"");
            System.exit(-1);
          }
          File actual = new File("actual");
          if (!Test.checkDirectory(actual)) {
            System.out.println("Unable to access or create folder \"" + actual.getPath() + "\"");
            System.exit(-1);
          }
          TestSet tests = TestParser.readTestSet(home, args[i]);
          tests.run(working, expected, actual, "", 0, flags);
        }
      }
    } catch (Exception e) {
      System.out.println("Exception occurred: " + e);
      e.printStackTrace();
    }
  }
}
