(ns cemerick.cljs.test.portable-core-async
  #+clj (:require [clojure.test :refer (is deftest run-tests)]
                  [clojure.core.async :as async :refer (go go-loop >! <! <!!)]
                  [cemerick.cljs.test :refer (done with-test-ctx block-or-done)])
  #+cljs (:require cemerick.cljs.test
                   [cljs.core.async :as async :refer (>! <!)])
  #+cljs (:require-macros [cemerick.cljs.test :refer (is deftest run-tests
                                                         block-or-done with-test-ctx)]
                          [cljs.core.async.macros :refer (go go-loop alts!)]))

(deftest synchronous-test
  (is true))

(deftest ^:async pointless-counting
  (let [inputs (repeatedly 10000 #(go 1))
        complete (async/chan)]
    (go (is (= 10000 (<! (reduce
                           (fn [sum in]
                             (go (+ (<! sum) (<! in))))
                           inputs))))
        (>! complete true)) 
    (block-or-done complete)))

(defn- verify-content
  [-test-ctx content substring]
  (is #+cljs -test-ctx (pos? (.indexOf content substring))
      "verifying explicit use of -test-ctx")
  (with-test-ctx -test-ctx
    (is (pos? (.indexOf content substring)))))

#+cljs
(defn- resolve
  [sym]
  (try
    (js/eval (str sym))
    (catch js/Error e nil)))

(defn- http-get
  [url]
  #+clj (go (let [url (java.net.URL. url)]
              (slurp url)))
  #+cljs (if-let [require (resolve 'require)]
           (let [chunks (async/chan 20)
                 http (require "http")]
             (if-not (.-request http)
               (go :skip-test)
               ; I couldn't figure out the lifecycle of the nodejs HTTP
               ; request to save my life; was trying to close the channel on
               ; "end" or "close", but they kept firing *before* the data
               ; event. Thus the setTimeout closing the channel below.
               (do (doto (.request http
                                   (clj->js {:host "google.com"})
                                   (fn [resp]
                                     (.on resp "data" #(go (>! chunks (str %))
                                                           (js/setTimeout
                                                            (fn [] (async/close! chunks))
                                                            1000)))))
                     .end)
                   (async/reduce str "" chunks))))
           (go :skip-test)))

(deftest ^:async check-webpage
  (let [content (http-get "http://google.com")]
    (block-or-done (go (let [content (<! content)]
                         (if (= :skip-test content)
                           (println "Skipping test, the `check-webpage` test requires node + :simple CLJS compilation")
                           (verify-content
                            #+cljs -test-ctx #+clj nil
                            content "google")))))))
