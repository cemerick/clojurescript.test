# clojurescript.test [![Build Status](https://travis-ci.org/cemerick/clojurescript.test.png?branch=develop/0.3.x)](https://travis-ci.org/cemerick/clojurescript.test)

A maximal port of `clojure.test` to ClojureScript.

## Why?

I want to be able to write portable tests to go along with my portable
Clojure[Script], and `clojure.test`'s model is Good Enough™ (it's better than
that, actually).  Combine with something like
[cljx](https://github.com/lynaghk/cljx) to make your ClojureScripting a whole
lot more pleasant.

## Installation

clojurescript.test is available in Maven Central. Add it to your **`:plugins`**
in your Leiningen `project.clj`:

```clojure
[com.cemerick/clojurescript.test "0.3.0"]
```

(clojurescript.test is actually a project dependency _and_ a Leiningen plugin;
adding it as the latter just helps simplify test configuration, as you see below.)

Or, add this to your Maven project's `pom.xml`:

```xml
<dependency>
  <groupId>com.cemerick</groupId>
  <artifactId>clojurescript.test</artifactId>
  <version>0.3.0</version>
</dependency>
```

## Usage

clojurescript.test provides roughly the same API as `clojure.test`, thus making
writing portable tests possible.

(Note that `clojurescript.test` doesn't take any responsibility for any hosty or
otherwise-unportable things you do in your tests, e.g. `js/...` or naming JVM
types or Clojure- or ClojureScript-only functions; either don't do that, or use
something like cljx to include both Clojure and ClojureScript code in the same
file.)

Here's a simple ClojureScript namespace that uses clojurescript.test:

```clojure
(ns cemerick.cljs.test.example
  (:require-macros [cemerick.cljs.test
                    :refer (is deftest with-test run-tests testing test-var)])
  (:require [cemerick.cljs.test :as t]))

(deftest somewhat-less-wat
  (is (= "{}[]" (+ {} []))))

(deftest javascript-allows-div0
  (is (= js/Infinity (/ 1 0) (/ (int 1) (int 0)))))

(with-test
  (defn pennies->dollar-string
    [pennies]
    {:pre [(integer? pennies)]}
    (str "$" (int (/ pennies 100)) "." (mod pennies 100)))
  (testing "assertions are nice"
    (is (thrown-with-msg? js/Error #"integer?" (pennies->dollar-string 564.2)))))
```

**Note**: each test namespace in your project must `(:require
cemerick.cljs.test)` even if you only use macros. Otherwise, the ClojureScript
compilation process won't include clojurescript.test in its output, resulting
in an error similar to "`ReferenceError: Can't find variable: cemerick`".

You can load this into a ClojureScript REPL, and run its tests using familiar functions:

```clojure
=> (t/test-ns 'cemerick.cljs.test.example)

Testing cemerick.cljs.test.example
{:fail 0, :pass 3, :test 3, :error 0}

=> (test-var #'cemerick.cljs.test.example/somewhat-less-wat)
{:fail 0, :pass 1, :test 1, :error 0}
```

All of the test-definition macros (`deftest` and `with-test`, as well as the
`set-test` utility) add to a global registry of available tests (necessary given
ClojureScript's lack of namespaces), so you can also define, redefine, and run
tests interactively:

```clojure
=> (deftest dumb-test
     (is (empty? (filter even? (range 20)))))
#<[object Object]>
nil
=> (t/test-ns 'cemerick.cljs.test.example)

Testing cemerick.cljs.test.example

FAIL in (dumb-test) (:0)
expected: (empty? (filter even? (range 20)))
  actual: (not (empty? (0 2 4 6 8 10 12 14 16 18)))
{:fail 1, :pass 3, :test 4, :error 0}
```

### Writing portable tests

Because clojurescript.test has (approximately) the same API as `clojure.test`,
writing portable tests with it is easy.  For example, the test namespace above
can be made portable using cljx like so:

```clojure
(ns cemerick.cljs.test.example
  #+clj (:require [clojure.test :as t
                   :refer (is deftest with-test run-tests testing)])
  #+cljs (:require-macros [cemerick.cljs.test
                           :refer (is deftest with-test run-tests testing test-var)])
  #+cljs (:require [cemerick.cljs.test :as t]))

#+cljs
(deftest somewhat-less-wat
  (is (= "{}[]" (+ {} []))))

#+cljs
(deftest javascript-allows-div0
  (is (= js/Infinity (/ 1 0) (/ (int 1) (int 0)))))

(with-test
  (defn pennies->dollar-string
    [pennies]
    {:pre [(integer? pennies)]}
    (str "$" (int (/ pennies 100)) "." (mod pennies 100)))
  (testing "assertions are nice"
    (is (thrown-with-msg? #+cljs js/Error #+clj Error #"integer?"
          (pennies->dollar-string 564.2)))))
```

Note that `test-var` is a macro in clojurescript.test; this allows you to
portably write code like `(test-var #'name-of-test)`, even though ClojureScript
doesn't support `#'` or the `(var ...)` special form.  `test-var` forms
macroexpand to calls to `cemerick.cljs.test/test-function`, which is the
corollary to clojure.test's `test-var`.

### Using with lein-cljsbuild

Most people use [lein-cljsbuild](https://github.com/emezeske/lein-cljsbuild) to
automate their ClojureScript builds.  It also provides a test runner, originally
intended for use with e.g. [phantomjs](http://phantomjs.org/) (though there are
[rumors](https://github.com/cemerick/clojurescript.test/issues/10) that it works
nicely with `slimerjs` as well) to run tests that use existing JavaScript test
frameworks.  However, you can easily use the same facility to run
clojurescript.test tests.

This is an excerpt of the lein-cljsbuild configuration that this project uses to
run its own clojurescript.test tests (look in the `project.clj` file for the full
monty):

```clojure
:plugins [[lein-cljsbuild "1.0.0"]
          [com.cemerick/clojurescript.test "0.2.3"]]
:cljsbuild {:builds [{:source-paths ["src" "test"]
                      :compiler {:output-to "target/cljs/testable.js"
                                 :optimizations :whitespace
                                 :pretty-print true}}]
            :test-commands {"unit-tests" ["phantomjs" :runner
                                          "this.literal_js_was_evaluated=true"
                                          "target/cljs/testable.js"
                                          "test/cemerick/cljs/test/extra_test_command_file.js"]}}
```

Everything here is fairly basic, except for the `:test-commands` entries, which
describes the shell command that will be executed when lein-cljsbuild's test
phase is invoked (either via `lein cljsbuild test`, or just `lein test` because
its hook is registered).  In this case, it's going to run `phantomjs`, passing
as arguments:

1. The path to the clojurescript.test test runner script (denoted by
`:runner`, which I'll explain momentarily…), and
2. Either paths to ClojureScript compiler output (a lein-cljsbuild `:output-to`
value defined elsewhere in the `project.clj`), _or_ paths to other arbitrary
JavaScript files (useful for injecting external libraries, polyfills, etc), _or_
arbitrary JavaScript expressions (useful for e.g. configuring runtime test
properties...see the subsection below on using this capability, especially in
conjunction with advanced compilation).

clojurescript.test bundles test runner scripts for various environments
(currently, phantomjs, node.js and rhino).  As long as you add
clojurescript.test to your `project.clj` as a `:plugin`, then it will replace
any occurrences of `:runner`, `:node-runner` and `:rhino-runner` in your
`:test-commands` vectors with the path to the corresponding test runner script.

_Outside_ of the `:test-commands` vector in your `:cljsbuild` configuration,
clojurescript.test will replace _namespaced_ corollaries to these test runner
keywords (`:cljs.test/runner`, `:cljs.test/node-runner`, and
`:cljs.test/rhino-runner`). This allows you to have paths to clojurescript.test
runner scripts injected anywhere into your `project.clj` you like.

##### Node.js

To run your tests with [node.js](http://nodejs.org/) instead of phantomjs, just
change the executable name and the `:runner` keyword in your `:test-commands`
vectors like so:

```
:test-commands {"unit-tests" ["node" :node-runner
                              ; extra code/files here...
                             ]}
```

**Note that you must compile your ClojureScript code with `:advanced` or
  `:simple` `:optimizations to run it on node.js.**

##### Rhino

To run your tests with [rhino](https://developer.mozilla.org/en/docs/Rhino),
change the executable name and the `:runner` keyword in your `:test-commands`
vectors like so:

```
:test-commands {"unit-tests" ["rhino" "-opt" "-1" :rhino-runner
                              ; extra code/files here...
                             ]}
```

Note that rhino doesn't support any HTML or DOM related functions and objects so
it can be used mainly for business-only logic or you have to mock all DOM
functions by yourself.

All test runner scripts load the output of the ClojureScript compilation, run
all of the tests found therein, reports on them, and fails the build if
necessary.

clojurescript.test supports all of Google Closure's compilation modes, including
`:advanced`.

#### Configuring tests via JavaScript files/expressions in `:test-commands`

As noted above, you can have arbitrary JavaScript files and/or expressions
loaded before or after your compiled ClojureScript.  One of the most useful
aspects of this is that you can configure properties of your tests; for example,
when using [double-check](https://github.com/cemerick/double-check), you can
control the number of iterations checked by each `defspec` test by setting a
Java system property.  While JavaScript doesn't have a corollary of system
properties, you can add a JavaScript expression to your `:test-commands`
vector(s) that sets a property on some globally-accessible object, e.g.:

```clojure
:test-commands {"rigorous" ["phantomjs" :runner
                            "this.defspec_iters=10000000"
                            "target/cljs/testable.js"]}
```

Then, in your ClojureScript test file(s), you can look up this dynamically-set
value, using a default if it's not set:

```clojure
(def iteration-count (or (this-as this (aget this "defspec_iters")) 1000))
```

The use of `aget` and a string property lookup is necessary to
ensure that the property name will not be renamed/obfuscated by Google Closure
when run with `:advanced` optimizations.  Prior examples of this practice
touched `window`, but that name is undefined in node.js; using `this` when
setting and looking up the test configuration value makes it so that the same
code (and configuration) can be used in any test environment.  

### Asynchronous testing

Problem: various operations in JavaScript are necessarily asynchronous, from
things as simple as DOM event callbacks to more involved activity like querying
or modifying IndexedDB databases or interacting with core.async channels.   This
means that the testing "context" may have moved on (and your
JavaScript environment's execution may have completed entirely) before your
callbacks/`go` blocks/etc have fired/completed…a big problem if those
asynchronous constructs contained assertions.

Starting with version `0.3.0`, clojurescript.test provides ways to explicitly
control when each test is complete.

First, an example of a test that will not perform the intended (asynchronous)
assertion:

```clojure
(ns async-example
  (:require-macros [cemerick.cljs.test :refer (is deftest)])
  (:require [cemerick.cljs.test :as t]))
  
(deftest timeout
  (let [now #(.getTime (js/Date.))
        t (now)]
    (js/setTimeout
	  (fn [] (is (>= (now) (+ t 2000))))
      2000)))
```

In the best case, the `is` assertion's results will be attributed to some other
test; in the worst case, the JavaScript environment will have exited before the
`setTimeout` callback is scheduled to be invoked, and the asynchronous assertion
will never be run at all.

Modifying this example as follows will yield useful/correct behaviour:

1. Add `^:async` metadata to the `deftest` name.
2. You must call `(done)` using the asynchronous `deftest`'s testing context in
   order for that test to finish, and cause the next test in the current run to
   start.

```clojure
(ns async-example
  (:require-macros [cemerick.cljs.test :refer (is deftest done)])
  (:require [cemerick.cljs.test :as t]))

(deftest ^:async timeout
  (let [now #(.getTime (js/Date.))
        t (now)]
    (js/setTimeout
	  (fn []
	    (is (>= (now) (+ t 2000)))
		(done))
      2000)))
```

Compared to the first example:

1. This test will be run after any synchronous tests in the same namespace that are
   included in the current test run.
2. Control exiting the lexical scope of `deftest` will have no effect upon the
   wider test run (compared to synchronous tests, the "completion" of which
   cause the next test in a run to be started).
3. When the `setTimeout` callback is invoked, the assertion therein will be run,
   and properly attributed to the `timeout` test.
4. The `(done)` call will close the `timeout` test context, and start the next
   test in the run.

Note that you have complete control over when a test is done; the `setTimeout`
callback above could just as well spin off another `setTimeout` call (or use any
other callback-based API), or send or
block on a core.async channel, etc.

If you _don't_ explicitly close a test's context via `(done)`, the
clojurescript.test test runner **will never move on to the next test**, and your
test run will be permanently stalled.  You can unwedge yourself from this
situation at the REPL in a couple of different ways, see
["Canceling asynchronous tests"](#canceling-asynchronous-tests).

The rest of this section will dig into the finer details of using the
asynchronous testing facilities.

#### Test contexts

Each test defined by clojurescript.test carries its own _test context_.  This is
defined implicitly by `deftest` and other test-creation macros.  The body of
each test is also wrapped within a `cemerick.cljs.test/with-test-ctx` form.
This macro does a couple of things:

* It implicitly binds the test context provided to it to `-test-ctx` within its
  scope.
* If the test context is asynchronous (i.e. the corresponding `deftest` was
  marked with `^:async` metadata), then `with-test-ctx` will wrap any containing
  body of code with a `try/catch` form that will call `(done)` with any error thrown in the
  course of the body's execution.  This ensures that the test context associated
  with an asynchronous test that fails with an exception is automatically
  closed, starting the next test.

The `is` assertion macro will pick up the anaphoric `-test-ctx` binding
automatically when provided with one or two arguments (the form to
evaluate/test, and an optional message).  Alternatively, you can explicitly pass
a test context to `is`.

Putting this all together allows you to define asynchronous tests that use
common functions that contain asynchronous processing and/or assertions, passing
the test context around explicitly in order to properly tie test results to the
"source" tests.  For example, here's the example from before, refactored to put
the asynchronous call and assertion in a helper function:

```clojure
(ns async-example
  (:require-macros [cemerick.cljs.test :refer (is deftest done with-test-ctx)])
  (:require [cemerick.cljs.test :as t]))

(defn- timeout-helper
  [test-context delay]
  (with-test-ctx test-context
    (let [now #(.getTime (js/Date.))
        t (now)]
      (js/setTimeout
	    (fn []
	      (is (>= (now) (+ t delay)))
	  	  (done))
        delay))))

(deftest ^:async timeout
  (timeout-helper 2000))
```

Because `is` and `done` are within `with-test-ctx`'s lexical scope, they'll pick
up the implicit test context binding automatically.

Alternatively, you could write `timeout-helper` like so, always passing the test
context explicitly:

```clojure
(defn- timeout-helper
  [test-context delay]
  (let [now #(.getTime (js/Date.))
        t (now)]
    (js/setTimeout
      (fn []
        (is test-context (>= (now) (+ t delay))
		  "an assertion message is required when explicitly passing test context to `is`")
        (done test-context))
      delay)))
```

A final variation is to establish the `-test-ctx` binding that `is` and `done`
look for yourself:

```clojure
(defn- timeout-helper
  [-test-ctx delay]
  (let [now #(.getTime (js/Date.))
        t (now)]
    (js/setTimeout
      (fn []
        (is (>= (now) (+ t delay)))
        (done))
      delay)))
```

This is somewhat less verbose than other options, but necessitates very careful
naming of `-test-ctx` (if you call `done` or `done*` without a test context,
you'll have a bad time), and does not provide the asynchronous error-handling
benefits of `with-test-ctx`.

#### Reporting errors

`with-test-ctx` will automatically catch and report errors that occur in
asynchronous tests.  However, if you're not using `with-test-ctx`, or want/need
to catch certain errors manually, you can report them via the `done` macro
(e.g. `(done error)`) if you are nevertheless within a `with-test-ctx` body, or
via the `done*` function (e.g. `(done* test-context error)`).  As with any other
`done` invocation, this will close the test context and start the next test in
the run.

#### core.async

You can use all of the facilities described here to test core.async code just as
you would test callback-based APIs of all sorts. Under the covers, the
asynchrony provided by core.async in ClojureScript is also mediated by
callbacks, so all the same semantics apply: declare your tests to be
asynchronous via the `^:async` metadata, and be sure to call `(done)` one way or
the other when each test's context should be closed.

Here's an example of core.async (and a profligate use of `go` blocks) used in
conjunction with clojurescript.test, pulled from clojurescript.test's own test
suite:

```clojure
(deftest ^:async core-async-test
  (let [inputs (repeatedly 10000 #(go 1))]
    (go (is (= 10000 (<! (reduce
                           (fn [sum in]
                             (go (+ (<! sum) (<! in))))
                           inputs))))
      (done))))
```

##### Portably testing core.async code with clojurescript.test and clojure.test

Clojure's clojure.test does not provide any control over test lifecycle to accommodate
assertions being performed in asynchronously-executed code paths, i.e. there is
no `done` to call when we want a test to be considered complete.  To work around
this, clojurescript.test includes `cemerick.cljs.test/block-or-done` macro, which enables one to test code that uses the only
Clojure/ClojureScript portable asynchrony option, core.async.  In Clojure, `block-or-done`
will block the completion of the enclosing clojure.test `deftest` until the
provided channel is yields a value; in ClojureScript, `block-or-done` will call
`(done)` when the provided channel yields a value.

This allows us to write the above core.async-using test in a portable way, that
will work on either Clojure or ClojureScript:

```clojure
(deftest ^:async pointless-counting
  (let [inputs (repeatedly 10000 #(go 1))
        complete (async/chan)]
    (go (is (= 10000 (<! (reduce
                           (fn [sum in]
                             (go (+ (<! sum) (<! in))))
                           inputs))))
        (>! complete true)) 
    (block-or-done complete)))
```

#### Canceling asynchronous tests

Every function or macro that starts a clojurescript.test test run
(e.g. `run-tests`, `test-ns`, etc) will return a map of the test environment
that summarizes the results of the _synchronous_ tests included in that run.
Within that environment's map is an `:async` entry, the value of which is an
atom containing another test environment, dedicated to the asynchronous portion
of the test run.

##### In ClojureScript

If you run a set of tests which appear to have wedged on the asynchronous
portion (perhaps because one of your tests failed to close its testing context
via `(done)` or `(done* ...)`, or maybe a bug is causing a test run to carry on
longer than desired), you can cancel the further processing of the test
run by calling `(cemerick.cljs.test/stop ...)`, passing the value of the
`:async` slot of the top-level test environment described above.  This will not
cancel any outstanding asynchronous processing your tests have provoked in the
JavaScript environment (e.g. callbacks, pending core.async puts or takes, etc),
but it will stop the test run corresponding to the test environment from
continuing if and when the wedged asynchronous test _does_ close its testing
context.

##### In Clojure

Assuming your asynchronous tests are using `block-or-done` (discussed above),
test running functions and macros will block until all tests are complete.  If
you suspect those tests will not complete, the only solution to this is to
interrupt the blocked REPL evaluation, supported by various nREPL clients and
tools.

## Limitations

* Bug: filenames and line numbers are not currently reported properly.

## Differences from `clojure.test`

TODO the differences noted here are out of date, and do not account for the
additional differences (esp. w.r.t. the test runtime maintenance bits)
introduced by supporting asynchronous testing starting in `0.3.0`.

* docstrings bear little to no semblence to the library's actual operation
* Namespace test hooks must be defined using the `deftesthook` macro

### Runtime

* `*report-counters*` is now bound to an atom, not a ref
* `*testing-vars*` now holds symbols naming the top-levels under test, not vars
* `*test-out*` is replaced by `*test-print-fn*`, which defaults to `nil`, and
  is only bound to `cljs.core/*print-fn*` if it is bound to a non-nil value.
* `run-tests` is now a macro; `run-tests*` does the same, but does not offer a
  no-arg arity
* `use-fixtures` is now a macro, and there is no underlying multimethod to
  extend as in `clojure.test`.

### Errors

* Stack traces from caught exceptions are obtained via
  [`Error.stack`](https://developer.mozilla.org/en-US/docs/JavaScript/Reference/Global_Objects/Error/Stack),
which appears to only be supported in Chrome, FF, Safari, and IE 10+. The value
of `Error.stack` in Rhino (at least, the version specified for use by
ClojureScript) is always an empty string; other JavaScript environments may be
similar.
* File and line numbers of reported exception failures may be missing in
  JavaScript environments that do not support the `lineNumber` or `fileName`
properties of `Error`.

### Removed

* `*load-tests*` is now private, and will probably be removed.  The use case
  for Clojure (which is rarely taken advantage of AFAICT) seems irrelevant for
ClojureScript; if you do or don't want tests in production, you just change
your cljsc/lein-cljsbuild configuration.
* `file-position` was already deprecated and unused
* Not applicable
 * `get-possibly-unbound-var`
 * `function?`
 * `*stack-trace-depth*`

## Need Help?

Send a message to the
[ClojureScript](http://groups.google.com/group/clojurescript) mailing list, or
ping `cemerick` on freenode irc or [twitter](http://twitter.com/cemerick) if you
have questions or would like to contribute patches.

## License

Copyright © 2013 Chas Emerick and other contributors.  Known contributors to
`clojure.test` (which was the initial raw ingredient for this project) at the
time of this project's inception were:

* Stuart Sierra
* Rich Hickey
* Stuart Halloway
* Phil Hagelberg
* Tassilo Horn
* Mike Hinchey

Distributed under the Eclipse Public License, the same as Clojure.
