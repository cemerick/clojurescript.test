(ns cemerick.cljs.test.fixtures-2
  (:require [cemerick.cljs.test :as t])
  (:require-macros [cemerick.cljs.test :refer (use-fixtures deftest is run-tests)]))

(def ^:dynamic *n* 0)

(defn inc-n-fixture [f]
  (binding [*n* (inc *n*)] (f)))

(use-fixtures :once inc-n-fixture)

(use-fixtures :each inc-n-fixture)

(deftest fixtures-stack-in-dashed-namespaces
  (is (= *n* 2)))
