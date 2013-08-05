(ns clojurescript.test.plugin
  (:require [clojure.java.io :refer [copy file input-stream resource]]
            [clojure.walk :refer (postwalk-replace)])
  (:import java.io.File))

(defn middleware
  "Modify command vector annotated with ^:cljs-test so that they receive the
location of a runner.js copy as second argument."
  [project]
  (let [target-folder (file (or (:target-path project) "target/"))]
    (.mkdirs target-folder)
    (let [runner (File/createTempFile "test-runner" ".js" target-folder)
          runner-path (.getAbsolutePath runner)]
      (.deleteOnExit runner)
      (copy (slurp (resource "cemerick/cljs/test/runner.js")) runner)
      (update-in project [:cljsbuild :test-commands]
                 #(postwalk-replace {:cljs.testrunner runner-path} %)))))
