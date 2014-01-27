(ns cemerick.cljs.test.async
  (:require-macros [cemerick.cljs.test :as m
                    :refer (is test-var deftest run-tests done with-test-ctx)])
  (:require [cemerick.cljs.test :as t]))

(deftest synchronous-test
  (is true))

; because I find interleaving callback / timeouts really difficult to read
(defn- do-later
  [timeout fn]
  (js/setTimeout fn timeout))

(deftest ^:async timeout
  (let [now #(.getTime (js/Date.))
        t (now)]
    (do-later 2000
              (fn []
                (is (>= (now) (+ t 2000)))
                (do-later 100 #(do
                                 (is (>= (now) (+ t 2000 100)))
                                 (done)))))))

(defn- some-other-fn
  [-test-ctx]
  (do-later 500 (fn []
                  (is -test-ctx (> 6 5) "msg")
                  (with-test-ctx -test-ctx
                    (do-later 500 (fn []
                                    (is (= 5 (+ 2 3)))
                                    (done)))))))

(deftest ^:async testing-ctx
  (some-other-fn -test-ctx))
