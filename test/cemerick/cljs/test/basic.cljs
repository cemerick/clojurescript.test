;   Copyright (c) Rich Hickey. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

;;; test_clojure/test.clj: unit tests for test.clj

;; by Stuart Sierra
;; January 16, 2009

;; Thanks to Chas Emerick, Allen Rohner, and Stuart Halloway for
;; contributions and suggestions.

(ns cemerick.cljs.test.basic
  (:require-macros [cemerick.cljs.test :as m :refer (is deftest run-tests deftesthook)])
  (:require [cemerick.cljs.test :as t]))

(deftest can-test-symbol
  (let [x true]
    (is x "Should pass"))
  (let [x false]
    (is x "Should fail")))

(deftest can-test-boolean
  (is true "Should pass")
  (is false "Should fail"))

(deftest can-test-nil
  (is nil "Should fail"))

(deftest can-test-=
  (is (= 2 (+ 1 1)) "Should pass")
  (is (= 3 (+ 2 2)) "Should fail"))

(deftype A [])

(deftest can-test-instance
  (is (number? (+ 2 2)) "Should pass")
  (is (string? (+ 1 1)) "Should fail")
  (is (instance? A (A.)) "Should pass")
  (is (instance? A (+ 1 1)) "Should fail"))

(deftest can-test-thrown
  (is (thrown? js/Error (js/eval "a + 2")) "Should pass")
  ;; No exception is thrown:
  (is (thrown? js/Error (+ 1 1)) "Should fail")
  ;; Wrong class of exception is thrown:
  (is (thrown? js/SyntaxError (throw (js/Error.))) "Should error"))

(deftest can-test-thrown-with-msg
  ;; coming up with error messages that are standard across js runtime isn't easy...
  (is (thrown-with-msg? js/SyntaxError #"[Uu]nterminated parenthetical|Invalid regular expression"
        ;; Use eval to prevent advanced mode from transforming it into a literal.
        ((js/eval "RegExp") "f(")) "Should pass")
  ;; Wrong message string:
  (is (thrown-with-msg? js/SyntaxError #"foo" (/ 1 0)) "Should fail")
  ;; No exception is thrown:
  (is (thrown? js/Error (+ 1 1)) "Should fail")
  ;; Wrong class of exception is thrown:
  (is (thrown-with-msg? js/SyntaxError #"Divide by zero" (js/eval "a + 2")) "Should error"))

(deftest can-catch-unexpected-exceptions
  (is (= 1 (throw (js/Error.))) "Should error"))

(deftest can-test-method-call
  (is (.match "abc" #"bc") "Should pass")
  (is (.match "abc" #"d") "Should fail"))

(deftest can-test-anonymous-fn
  (is (#(.match % #"bc") "abc") "Should pass")
  (is (#(.match % #"d") "abc") "Should fail"))

(deftest can-test-regexps
  (is (re-matches #"^ab.*$" "abbabba") "Should pass")
  (is (re-matches #"^cd.*$" "abbabba") "Should fail")
  (is (re-find #"ab" "abbabba") "Should pass")
  (is (re-find #"cd" "abbabba") "Should fail"))

;; Here, we create an alternate version of test/report, that
;; compares the event with the message, then calls the original
;; 'report' with modified arguments.

(declare ^:dynamic original-report)

(defn custom-report [data]
  (let [event (:type data)
        msg (:message data)
        expected (:expected data)
        actual (:actual data)
        passed (cond
                 (= event :fail) (= msg "Should fail")
                 (= event :pass) (= msg "Should pass")
                 (= event :error) (= msg "Should error")
                 :else true)]
    (if passed
      (original-report {:type :pass, :message msg,
                        :expected expected, :actual actual})
      (original-report {:type :fail, :message (str msg " but got " event)
                        :expected expected, :actual actual}))))

;; test-ns-hook will be used by test/test-ns to run tests in this
;; namespace.
(deftesthook test-ns-hook []
  (binding [original-report t/report
            t/report custom-report]
    (t/test-all-vars 'cemerick.cljs.test.basic)))
