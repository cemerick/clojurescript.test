## [clojurescript.test](http://github.com/cemerick/clojurescript.test) changelog

### `0.0.3`

* `function?` predicate fixed to properly identify symbols that name macros
  within the current ClojureScript compilation environment.  This fixes `are`
(which didn't work at all previously), and usages of `is` that use a macro in
fn position of the expression being asserted (e.g. `(is (and ...))`.

### `0.0.2`

* Advanced compilation now supported (gh-2); thanks to
  [r0man](https://github.com/r0man) for making it happen

### `0.0.1`

Initial release.
