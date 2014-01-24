(ns cemerick.cljs.test.plugin
  (:require-macros [cemerick.cljs.test :refer (is deftest)])
  (:require [cemerick.cljs.test :as t]))

;; TODO how do you export a simple value def, e.g. (def foo nil)?
;; export binds the initial value, but then inlines all other references
;; http://stackoverflow.com/questions/19860984
;(def ^:export file-was-evaluated nil)

(deftest literal-js-string-is-evaluated
  ; odd, the `true?` is needed here to avoid a gnarly js `this` error?
  (is (true? (this-as this (aget this "literal_js_was_evaluated")))))

(deftest extra-file-in-test-command-is-evaluated
  (is (= 42 (this-as this (aget this "file_was_evaluated")))))
