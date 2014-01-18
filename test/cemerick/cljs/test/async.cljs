(ns cemerick.cljs.test.async
  (:require-macros [cemerick.cljs.test :as m :refer (is deftest testing)])
  (:require [cemerick.cljs.test :as t]
            [cljs.core.async :refer [put! <! chan]])
  (:use-macros [cljs.core.async.macros :only [go go-loop]]))

(deftest test-timeout
  (let [lock (t/acquire-lock)]
    (.setTimeout js/window
    (fn []
      (is (= 2 2))
      (t/release-lock lock))
    500)))

(deftest test-core-async
  (let [ch (chan)
        lock (t/acquire-lock)
        ; For some reason 'is' didn't work for me in go block.
        ; I wrapped it in separate function.
        assert-eq #(is (= %1 %2))]
    (go (assert-eq :core.async (<! ch))
        (t/release-lock lock))
    (put! ch :core.async)))


