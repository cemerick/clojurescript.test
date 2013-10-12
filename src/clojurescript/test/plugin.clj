(ns clojurescript.test.plugin
  (:require [clojure.java.io :refer [copy file input-stream resource]]
            [clojure.walk :refer (postwalk-replace)])
  (:import java.io.File))

(def ^:private version
  (let [[_ coords version] (-> (or (resource "META-INF/leiningen/com.cemerick/clojurescript.test/project.clj")
                             ; this should only ever come into play when testing clojurescript.test itself
                             "project.clj")
                         slurp
                         read-string)]
    (assert (= coords 'com.cemerick/clojurescript.test)
            (str "Something very wrong, could not find clojurescript.test's project.clj, actually found: "
                 coords))
    (assert (string? version)
            (str "Something went wrong, version of clojurescript.test is not a string: "
                 version))
    version))

(defn middleware
  "Does two things:

1. Modify all :cljsbuild :test-command vectors, swapping :runner keyword
for string path to the packaged runner.js.
2. Add [com.cemerick/clojurescript-test \"CURRENT_VERSION\"] as a project dependency."
  [project]
  (let [runner (File/createTempFile "test-runner" ".js")
        runner-path (.getAbsolutePath runner)]
    (.deleteOnExit runner)
    ; if we end up packaging multiple runner scripts, there's a (weak)
    ; correspondence set up between the keywords being replaced and the resource
    ; path...
    (copy (slurp (resource "cemerick/cljs/test/runner.js")) runner)
    (-> project
        (update-in [:dependencies]
                   (fnil into [])
                   [['com.cemerick/clojurescript.test version]])
        (update-in [:cljsbuild :test-commands]
                   #(postwalk-replace {:runner runner-path} %)))))
