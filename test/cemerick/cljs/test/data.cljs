(ns cemerick.cljs.test.data
  (:require-macros [cemerick.cljs.test :refer (is deftest)])
  (:require [cemerick.cljs.test :as t]))

(deftest loads-single-test-data-file
  (is (= "42" (t/test-data "test/cemerick/cljs/test/single_data_file.txt"))))

(deftest loads-test-data-dir
  (let [o (JSON/parse (t/test-data "test/cemerick/cljs/test/data_files/some.json"))
        h (t/test-data "test/cemerick/cljs/test/data_files/other.txt")]
    (is (= 42 (aget o "everything")))
    (is (= 3.14 (aget o "pi")))
    (is (= "hello!" (aget o "string")))
    (is (= "Hello ClojureScript" h))))
