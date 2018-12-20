# tester - a simple testing tool

Tester is a tool for automatically running a collection of test cases and for comparing the actual and expected outputs in each case.

-------------

### Requirements:

The following items are required to use the code in this repository:

* Java Development Kit (version >= 1.8 should be sufficient)
* Apache Ant (version >= 1.9.2 should be sufficient)

-------------

### Acknowledgements:

The development of tester was supported in part by funding from the
National Science Foundation, Award No. CNS-1422979.

-------------

## Installation

To begin, use the command `ant` to compile the code for `tester` and to create the file `tester.jar`.  The recommended method for installation is to copy the `tester` shell script into a folder on your path, and then edit the copy to point to the location of the `tester.jar` file on your system.  After these steps, you should be able to run `tester` (without arguments) in any directory to produce the following output:

    $ tester
    usage: tester options ...
    options: -r     run tests (dryrun without this option)
             -i     enable interaction to update expected results
             -c     do not print context for tests during interaction
             -q     quiet; do not print test progress messages
             -f     do not print test failed messages
             -s     do not print test set summaries
    $

-------------

## Defining test cases

A set of test cases is defined by a folder containing a file called `tests` that defines a series of individual test cases.  Each test case has an associated name (a sequence of one or more characters, each of which is a letter, digit, a hyphen, or an underscore), and every test case in each `test` file must have a distinct name.  Test case names are used in identifying individual tests to users, and also for creating files as necessary to capture test case outputs.

Every `tests` file is interpreted as a sequence of lines, and the start of a new test case is marked by one of the following:

* A line of the form `exec: name command` introduces a test called `name` that will be executed by running the specified `command`.  The lines in the `tests` file immediately following the `exec:` line can be used to provide some context for the test (e.g., a short description of why the test is included); this context information can then be displayed when tester detects a change in the expected output to help the user determine whether the results are correct or not.

* A line of the form `.ext: name command` introduces a test called `name` that will be executed by running the specified `command` with an additional argument that will be the name of a file with a `.ext` suffix.  The contents of the latter file are specified by the lines in the `tests` file that immediately follow the `.ext:` line, up to the start of the next text case or the end of the file, whichever comes first.  Note that tester will allow any file extension `ext` (with the same syntax as is used for test names).

* A line of the form `tests: name` allows the user to nest a new set of tests, called `name`, within the current test set.  This can be useful for organizing a large set of tests cases into smaller pieces.  For a test case of this form to be valid, there must be a nested folder called `name` that itself contains a new `tests` file.  For example, if the file `a/tests` includes the line `tests: b`, then there should also be a folder `a/tests/b` and a file `a/tests/b/tests` where the latter describes the test cases for `b`.

All other lines in the input are treated as plain text, either forming introductory comments before any test cases at the start of a `tests` file, or else as lines making up the context or contents portion of a `code:` or `.ext:` test case, respectively.  It is also permitted to include additional lines after a `tests:` line and before the next test case (if any), but the text in those lines will be ignored.

-------------

## Running tester

The basic operation of `tester` is to find and run each of the test cases in a given test set; to capture any output that is produced on the standard output and standard error streams; and to compare those outputs with (previously determined) expected results.  As such, `tester` will not be immediately applicable in settings where the programs being tested do not produce enough information on the standard output and error streams to be able to judge whether the programs are working correctly or not.  If you are testing a compiler, for example, then it should be possible to test that errors in input programs are being detected and reported correctly by checking these outputs, but it may be harder to confirm correct operation for valid input programs where the compiler quietly generates an executable and then exits.  Similarly, it can be hard to test a program in this way if its outputs are expected to be different every time the program is run (for example, if the output includes current time or date information or machine-specific details).  It may still be possible, however, to use tester for programs like this by using shell scripts or other programs or options specifically designed for use in testing.  For example, you might use a script that not only compiles source programs, but also runs them, or passes them through other tools to generate output and confirm that the program worked correctly.  Or you might be able to filter the output to remove unnecessary details, such as time information, that might cause the output to change unnecessarily.

If you determine that `tester` is applicable, the next step is to create a root folder for the test cases.  This folder will typically contain:

* One or more subfolders with `tests` files.  For example, you might partition your tests across multiple folders so that you can test different parts of the system without having to run all of the tests every time.  Or perhaps you will use one short set of tests for a quick sanity check during development, and a larger, more extensive set of tests (that might take much longer to run) on a less regular basis, such as prior to commiting code for a new feature.

* Two subfolders called `expected` and `actual` that contain the expected and actual outputs of the individual test cases.  The `tester` tool will automatically generate these folders, and any necessary subfolders.  In principle, you could create files in the `expected` folder by hand; in practice, however, it will probably be easier to use tester's interactive mode (`-i`) to do this.  The contents of the `actual` folder overwritten every time you rerun the tests (using the `-r` option), so you should not expect to make permanent edits to the files in this folder, but you may find that those files are useful for debugging individual test cases.  For example, the actual outputs that are produced by running a test called `t` in a set of tests represented by the folder `a` will be stored in the files `actual/a/t.out` and `actual/a/t.err`, capturing the standard output and standard error, respectively.  If you run `tester` without repeating the tests, then any results from a previous run that are cached in the `actuals` folder will be used instead.  This, for example, makes it possible to get a quick reminder of any failing tests in the previous run by using `-qs` as a command line option without the overhead of having to rerun all of the tests.

* Custom shell scripts or other programs that you need for testing.  Note that the commands that are specified in `tests` files are taken as verbatim: there is no support for environment variable substitutions, I/O redirection, pipes, shell escapes, etc.  If such features are required for the tests, then they should be packaged up in shell scripts that can, instead, be referenced in the commands used for testing.

* Data files that are needed for testing.  (If you have a lot of these, it might be better to organize them in subfolders too, of course.)

-------------

## Command line options

The `tester` command allows the user to specify a combination of options (by writing an initial `-` followed by one or more option characters) and a sequence of test folders.  As indicated previously, it is possible to obtain a summary of command line options by running `tester` without any arguments.  The following provides a slightly more detailed description for what each of the options controls.

* `-r` indicates that `tester` should attempt to run all of the specified test cases and capture new output for each one.  If this flag is not specified, then `tester` will use just any outputs captured in the `actual` folder instead of repeating the test.  This will likely be much faster, but it obviously won't account for any changes that have been made to the program being tested since the previous test run.

* `-i` indicates that `tester` should use an interactive mode to display details of any issues that it detects and give the user an option to update the expected outputs when appropriate.  It is important, of course, for the user to review the new outputs very carefully to ensure they are correct before accepting them.

* `-c` indicates that `tester` should not display the context information (explanation or code) for test cases when `tester` is running in interactive mode.  Use of this option is not encouraged unless the naming scheme that is used for individual test cases already provides enough information for users to identify any test where the outputs differ.

* `-q` indicates that `tester` should run in "quiet" mode, which means that it will not display progress messages about which test is being run or about which tests have passed; only summary information and details of failing tests will be displayed.

* `-s` indicates that `tester` should not display summary messages that indicate the fraction of passing tests in each test set.  In combination with `-q`, this option indicates that `tester` should only display information about failing tests.

* `-f` indicates that `tester` should not display messages about failing tests.  Failing tests will, however, still be included in summary lines (unless they have also been disabled using the `-s` option).

-------------

## A simple example

The following sequence of commands illustrates the use of `tester` on a simple example: testing the Unix sort utility, which takes text files as input and outputs a sorted version with the lines rearranged (by default) in increasing dictionary order.  The files that are used as the starting point for this demo are included in the `sort-test` folder of the `tester` distribution; if you have installed `tester` correctly, then you should be able to change in to that directory and then just follow along with the sequence of commands shown below.

The testing process begins by defining a set of test cases, which we will create here in a test folder called `demo`:

    $ cat demo/tests 
    This test file contains some simple tests for the Unix sort utility.
    
    .txt: animals           sort
    lion
    tiger
    panda
    racoon
    aardvark
    zebra
    bear
    wolf
    echidna
    
    exec: numbers          sort numbers
    This variant of the sort command treats the input as text strings that will
    be sorted in dictionary/lexicographic order.  So anything that starts with
    0 will come before anything that starts with a 1.
    
    exec: numbers-numeric  sort -n numbers
    By adding the -n command line option, we specify that the input should be
    treated (and hence sorted) as a list of numbers, so 007 will come before
    10, but not before 1.
    
    $

This particular test file contains three test cases.  The first, called `animals` attempts to sort the given list of animal names by placing them in a .txt file and passing that as an argument to the sort command.  (Note that there is actually a blank linke at the end of the list of animals, and this will be included with the rest of the input when the test is executed; this is not a problem, so long as we are careful to account that this is properly reflected in the output that we see when the test case is run.)

The remaining two test cases, named `numbers` and `numbers-numeric` test the behavior of `sort` for sorting text representing numbers in two different ways.  Each of the test cases includes some brief text to explain what is being tested and what we might expect from the results.  The list of numbers is actually stored separately in a file, also called `numbers`, so that the same input can be used in both tests:

    $ cat numbers
    4
    007
    12
    42
    7
    10
    100
    93
    1
    
    $

At this point, `demo/tests` and `numbers` are the only files we have created:

    $ ls
    demo/     numbers
    $

But this can be changed by running `tester` on our `demo` test cases:

    $ tester demo
    Test set demo contains 3 tests:
    ....CodeTest: sort demo/animals.txt
    ....FAILED demo/animals: Missing expected outputs
    
    ....Exec demo/numbers: sort numbers
    ....FAILED demo/numbers: Missing expected outputs
    
    ....Exec demo/numbers-numeric: sort -n numbers
    ....FAILED demo/numbers-numeric: Missing expected outputs
    
    demo: passed 0 of 3 tests
    $ ls
    actual/   demo/     expected/ numbers
    $

Notice that, although all of the tests failed (because we have not yet specified
any expected outputs for them), this initial run did at least succeed
in creating the `expected` and `actual` folders where outputs will be stored.
In fact, the `tester demo` command did not actually run any of the tests, so
although it created the `actual` folder with a subfolder for `demo`, it has
not actually captured any outputs there:

    $ ls actual/demo/
    animals.txt
    $

The only file here is `animals.txt`, which was created to contain the text from
the first test case.  To force `tester` to actually run the tests, we must specify
the `-r` command line option:

    $ tester -r demo
    Test set demo contains 3 tests:
    ....CodeTest: sort demo/animals.txt
    ....FAILED demo/animals: Missing expected outputs

    ....Exec demo/numbers: sort numbers
    ....FAILED demo/numbers: Missing expected outputs

    ....Exec demo/numbers-numeric: sort -n numbers
    ....FAILED demo/numbers-numeric: Missing expected outputs

    demo: passed 0 of 3 tests
    $

Although all the tests are failing (after all, we still have not created the
expected outputs), we can see that the outputs---for all three tests, and for
both standard output and standard error---have now been captured in the
`actual/demo` folder:

    $ ls actual/demo/
    animals.err          animals.txt          numbers-numeric.out  numbers.out
    animals.out          numbers-numeric.err  numbers.err
    $

It would be possible now to go through those files, checking (by hand)
that the contents are what we expect, and then copying each of them to
the corresponding position in the `expected` folder to build up our
initial set of expected outputs.  (Of course, it would also be possible,
but likely more work too, to create each of those expected output files
manually.)  The interactive mode of `tester`, which is accessed by
including `i` as a command line option, is designed to help in situations
like this by walking the user through any changes and allowing them to
update expected outputs as appropriate.  Every time `tester -i` finds
a difference between the actual and expected outputs for a test case,
it (a) displays the context for the test; (b) shows either the actual output
(if there is no expected output) or a diff (if both outputs exist); and
(c) asks the user if they would like to update the expected output with
the actual output.  The user should inspect the outputs carefully and
answer either `y` (yes) or `n` (no) as appropriate at each such prompt.

(In what follows, we use the `r` and `i` command line options
together, which is likely to be a common pattern in practice.  But
because we have just run all the tests using `tester -r`, it would
also be possible to use just `tester -i` here, and avoid the
overhead of repeating the previous test runs.)

    $ tester -ri demo
    Test set demo contains 3 tests:
    ....CodeTest: sort demo/animals.txt
    ISSUE for demo/animals: expected outputs for demo/animals are missing.
    context=================================================================
    lion
    tiger
    panda
    racoon
    aardvark
    zebra
    bear
    wolf
    echidna
    
    standard output=========================================================
    
    aardvark
    bear
    echidna
    lion
    panda
    racoon
    tiger
    wolf
    zebra
    standard error==========================================================
    ========================================================================
    Use these outputs as the expected results [yn]? y
    
    ....Exec demo/numbers: sort numbers
    ISSUE for demo/numbers: expected outputs for demo/numbers are missing.
    context=================================================================
    This variant of the sort command treats the input as text strings that will
    be sorted in dictionary/lexicographic order.  So anything that starts with
    0 will come before anything that starts with a 1.
    
    standard output=========================================================
    
    007
    1
    10
    100
    12
    4
    42
    7
    93
    standard error==========================================================
    ========================================================================
    Use these outputs as the expected results [yn]? y
    
    ....Exec demo/numbers-numeric: sort -n numbers
    ISSUE for demo/numbers-numeric: expected outputs for demo/numbers-numeric are missing.
    context=================================================================
    By adding the -n command line option, we specify that the input should be
    treated (and hence sorted) as a list of numbers, so 007 will come before
    10, but not before 1.
    
    standard output=========================================================
    
    1
    4
    007
    7
    10
    12
    42
    93
    100
    standard error==========================================================
    ========================================================================
    Use these outputs as the expected results [yn]? y
    
    demo: passed 3 of 3 tests
    $

In each of the three cases shown here, `tester` detects that the
expected output is missing and displays the context (code or
explanation for the test case) and the generated standard output
and standard error (the latter being empty in all of these
examples).  At each step, after carefully reviewing the details to
check that it is correct, the user hits `y` to accept the update
and the test case is marked as being passed.

If we rerun `tester`, all of the tests are now marked as `PASSED`:

    $ tester -r demo
    Test set demo contains 3 tests:
    ....CodeTest: sort demo/animals.txt
    ....PASSED demo/animals
    
    ....Exec demo/numbers: sort numbers
    ....PASSED demo/numbers
    
    ....Exec demo/numbers-numeric: sort -n numbers
    ....PASSED demo/numbers-numeric
    
    demo: passed 3 of 3 tests
    $

In situations like this, it may be useful to see only an abbreviated
version of the test report.  Using command line option `-q`, for
example, omits all of the lines for passing tests and shows only
the summary lines that describe the fraction of passing tests:

    $ tester -q demo
    demo: passed 3 of 3 tests
    $

Now suppose that we want to add another test.  For the sake of this
example, we'll use the command line to add something to the end of
the `demo/tests` file: in practice, it will usually be easier to make
changes like this in a text editor:

    $ echo "exec: sort-reverse   sort -r numbers" >> demo/tests 
    $

The purpose of this test case is to explore what happens when the `-r`
flag is used (it should cause the input to be sort in reverse order).
If we rerun `tester` with this addition, it will detect and report a
"Missing expected outputs" error for the new test case:

    $ tester -q demo
    ....FAILED demo/sort-reverse: Missing expected outputs
    demo: passed 3 of 4 tests
    $

To fill this gap, and skip output for the tests that already pass,
we can use `tester -qri`:

    $ tester -qri demo
    ISSUE for demo/sort-reverse: expected outputs for demo/sort-reverse are missing.
    standard output=========================================================
    93
    7
    42
    4
    12
    100
    10
    1
    007

    standard error==========================================================
    ========================================================================
    Use these outputs as the expected results [yn]? y
    demo: passed 4 of 4 tests
    $

Now that we have built a set of test cases, and a corresponding set
of expected outputs for each one, it is easy to run the tests again
and again:

    $ tester -qr demo
    demo: passed 4 of 4 tests
    $ 

In practice, of course, it only makes sense to repeat the tests is there
is a possibility that something may have changed to cause the programs to
fail.  To simulate this, we will add an extra number to the end of the
`numbers` file and then rerun `tester`:

    $ echo 18 >> numbers
    $ tester -qr demo
    ....FAILED demo/numbers: Test did not produce expected outputs
    ....FAILED demo/numbers-numeric: Test did not produce expected outputs
    ....FAILED demo/sort-reverse: Test did not produce expected outputs
    demo: passed 1 of 4 tests
    $

Unsurprisingly, the three tests that depend on the content of `numbers` are
now listed as having `FAILED`.  The new outputs, however, have been recorded
in the `actual` folder, so it is easy to generate a quick reminder of which
tests are failing without having to run everything from scratch:

    $ tester -q demo
    ....FAILED demo/numbers: Test did not produce expected outputs
    ....FAILED demo/numbers-numeric: Test did not produce expected outputs
    ....FAILED demo/sort-reverse: Test did not produce expected outputs
    demo: passed 1 of 4 tests
    $

More importantly, we can go back to interactive mode using the `i` command
line flag and use that to step through and update the broken test cases:

    $ tester -qi demo
    ISSUE for demo/numbers: test did not produce expected outputs.
    context=================================================================
    This variant of the sort command treats the input as text strings that will
    be sorted in dictionary/lexicographic order.  So anything that starts with
    0 will come before anything that starts with a 1.
    
    standard output=========================================================
    *** expected/demo/numbers.out	2018-09-19 16:14:45.000000000 -0700
    --- actual/demo/numbers.out	2018-09-19 16:23:52.000000000 -0700
    ***************
    *** 4,9 ****
    --- 4,10 ----
      10
      100
      12
    + 18
      4
      42
      7
    ========================================================================
    Use new output as the expected result [yn]? y
    ISSUE for demo/numbers-numeric: test did not produce expected outputs.
    context=================================================================
    By adding the -n command line option, we specify that the input should be
    treated (and hence sorted) as a list of numbers, so 007 will come before
    10, but not before 1.
    
    standard output=========================================================
    *** expected/demo/numbers-numeric.out	2018-09-19 16:14:46.000000000 -0700
    --- actual/demo/numbers-numeric.out	2018-09-19 16:23:52.000000000 -0700
    ***************
    *** 5,10 ****
    --- 5,11 ----
      7
      10
      12
    + 18
      42
      93
      100
    ========================================================================
    Use new output as the expected result [yn]? y
    ISSUE for demo/sort-reverse: test did not produce expected outputs.
    standard output=========================================================
    *** expected/demo/sort-reverse.out	2018-09-19 16:15:48.000000000 -0700
    --- actual/demo/sort-reverse.out	2018-09-19 16:23:52.000000000 -0700
    ***************
    *** 2,7 ****
    --- 2,8 ----
      7
      42
      4
    + 18
      12
      100
      10
    ========================================================================
    Use new output as the expected result [yn]? y
    demo: passed 4 of 4 tests
    $

In these examples, `tester` recognizes that expected and actual outputs both
exist, but are different, and so it displays a (context) diff for each one to
highlight the changes.  In each case, it is clear that the only change is the
result of adding 18 to the original test set, and it is also easy to see that
the new number is inserted at the correct position.  As a result, the user
can quickly answer each of the prompts with `y`, and return to the position
where all of the tests pass.

-------------

