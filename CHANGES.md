## [clojurescript.test](http://github.com/cemerick/clojurescript.test) changelog

### `0.1.0`

* clojurescript.test now ships with its test runner script, and includes a
  Leiningen plugin that automatically unpacks it and updates your
  suitably-arranged lein-cljsbuild `:test-commands` vectors to refer to that
  script. See the `README.md` for details.

### `0.0.4`

* Fixed test output when run using ClojureScript `0.0-1803` (which dropped
  previously-default `.toString()` implementations on data structure types)
  (gh-7)
* Fixed extraneous (and missing!) newlines in test runner output (gh-6)

