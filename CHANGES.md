## [clojurescript.test](http://github.com/cemerick/clojurescript.test) changelog

### `0.2.0`

**This release contains breaking changes.**

* `test-var` is now a macro, which allows you to write code like `(test-var
  #'name-of-test)`, even though ClojureScript doesn't support `#'` or the
  `(var ...)` special form.  The function that was previously named `test-var`
  retains its prior implementation, and is now (more appropriately) named
  `test-function`.
* Warnings regarding `set-print-fn!` have been squelched.  As a side effect,
  clojurescript.test now requires ClojureScript >= `0.0-1798`.

### `0.1.0`

* clojurescript.test now ships with its test runner script, and includes a
  Leiningen plugin that automatically unpacks it and updates your
  suitably-arranged lein-cljsbuild `:test-commands` vectors to refer to that
  script. See the `README.md` for details. (gh-11)
* Fixed a bug where fixtures defined in namespaces containing dashes were never
  applied (gh-17)

### `0.0.4`

* Fixed test output when run using ClojureScript `0.0-1803` (which dropped
  previously-default `.toString()` implementations on data structure types)
  (gh-7)
* Fixed extraneous (and missing!) newlines in test runner output (gh-6)

