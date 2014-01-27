(ns cemerick.cljs.test.are
  (:require-macros [cemerick.cljs.test :refer (is deftest are run-tests)])
  (:require [cemerick.cljs.test :as t]))

;; this actually is more of a test of the function? predicate in
;; test.clj, which is easy to replicate via splicing macros
(deftest sanity
  (are [x y] (and (number? x) (number? y) (< x y))
    1 2
    (inc 1) (inc 2)))
