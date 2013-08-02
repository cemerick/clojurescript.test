(ns clojurescript.test.plugin
  (:require [clojure.java.io :refer [copy file input-stream resource]])
  (:import java.io.File))

(defn- inject-runner
  "Inject runner location as second command if command vector is annotated with ^:cljs-test."
  [command runner]
  (if (contains? (meta command) :cljs-test)
    (apply conj (vector (first command)) (.getAbsolutePath runner) (rest command))
    command))

(defn middleware
  "Modify command vector annotated with ^:cljs-test so that they receive the location of a runner.js copy as second argument."
  [project]
  (let [target-folder (file (or (:target-path project) "target/"))]
    (.mkdirs target-folder)
    (let [runner (File/createTempFile "test-runner" ".js" target-folder)
          inject #(inject-runner % runner)]
      (.deleteOnExit runner)
      (copy (slurp (resource "cemerick/cljs/test/runner.js")) runner)
      (update-in project [:cljsbuild :test-commands] #(zipmap (keys %) (map inject (vals %)))))))
