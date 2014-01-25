(ns clojurescript.test.plugin
  (:require [clojure.java.io :refer [copy file input-stream resource]]
            [clojure.walk :refer (postwalk-replace)])
  (:import java.io.File))

(def ^:private version
  (let [[_ coords version]
        (-> (or (resource "META-INF/leiningen/com.cemerick/clojurescript.test/project.clj")
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

(defn runner-path! [[runner resource-file]]
  "Creates a temp file for the given runner resource file"
  (let [runner-path (.getAbsolutePath
                     (doto (File/createTempFile (name runner) ".js")
                       (.deleteOnExit)
                       (#(copy (slurp (resource resource-file)) %))))]

    [runner runner-path]))

(defn runner-paths! [runners]
  "Creates temp files for the given runners, returning a hash map
associating the runner keyword with the corresponding temp file"
  (into {} (map runner-path! runners)))

(defn middleware
  "Does two things:

1. Modify all :cljsbuild :test-command vectors, swapping :runner, :node-runner,
  and :nodejs-runner keywords for the string path to the corresponding packaged
  script.
2. Add [com.cemerick/clojurescript-test \"CURRENT_VERSION\"] as a project
  dependency."
  [project]
  (let [runners [[:runner "cemerick/cljs/test/runner.js"]
                 [:node-runner "cemerick/cljs/test/node_runner.js"]
                 [:nodejs-runner "cemerick/cljs/test/node_runner.js"]]
        runner-paths (runner-paths! runners)]
    (-> project
        (update-in [:dependencies]
                   (fnil into [])
                   [['com.cemerick/clojurescript.test version]])
        (update-in [:cljsbuild :test-commands]
                   #(postwalk-replace runner-paths %)))))
