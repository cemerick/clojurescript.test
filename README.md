# clojurescript.test [![Travis CI status](https://secure.travis-ci.org/cemerick/clojurescript.test.png)](http://travis-ci.org/#!/cemerick/clojurescript.test/builds)

A maximal port of `clojure.test` to ClojureScript.

## Why?

## Installation

clojurescript.test is available in Maven Central. Add this `:dependency` to your Leiningen
`project.clj`:

```clojure
[com.cemerick/clojurescript.test "0.0.1-SNAPSHOT"]
```

Or, add this to your Maven project's `pom.xml`:

```xml
<dependency>
  <groupId>com.cemerick</groupId>
  <artifactId>clojurescript.test</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Usage

clojurescript.test provides roughly the same API as clojure.test, thus making writing portable tests 

## Differences from `clojure.test`

* docstrings bear little to no semblence to the library's actual operation
* Namespace test hooks must be defined using the `deftesthook` macro

### Runtime
* `*report-counters*` is now bound to an atom, not a ref
* `*testing-vars*` now holds symbols naming the top-levels under test, not vars
* `*test-out*` is replaced by `*test-print-fn*`, which defaults to `nil`, and is only bound to `cljs.core/*print-fn*` if it is bound to a non-nil value.
* `run-tests` is now a macro; `run-tests*` does the same, but does not offer a no-arg arity

### Errors
* Stack traces from caught exceptions are obtained via [`Error.stack`](https://developer.mozilla.org/en-US/docs/JavaScript/Reference/Global_Objects/Error/Stack), which appears to only be supported in Chrome, FF, Safari, and IE 10+. The value of `Error.stack` in Rhino (at least, the version specified for use by ClojureScript) is always an empty string; other JavaScript environments may be similar.
* File and line numbers of reported exception failures may be missing in JavaScript environments that do not support the `lineNumber` or `fileName` properties of `Error`.

### Removed
* All fixture facilities; perhaps to be reintroduced
* `*load-tests*` is now private, and will probably be removed.  The use case for Clojure (which is rarely taken advantage of AFAICT) seems irrelevant for ClojureScript; if you do or don't want tests in production, you just change your cljsc/lein-cljsbuild configuration.
* `file-position` was already deprecated and unused
* Not applicable
 * `get-possibly-unbound-var`
 * `function?`
 * `*stack-trace-depth*`


