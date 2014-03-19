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

(defn runner-path! [[runner filename]]
  "Creates a temp file for the given runner resource file"
  (let [full-path (str "cemerick/cljs/test/" filename)
        runner-path (.getAbsolutePath
                     (doto (File/createTempFile (name runner) ".js")
                       (.deleteOnExit)
                       (#(copy (slurp (resource full-path)) %))))]

    [runner runner-path]))

(defn runner-paths! [runners]
  "Creates temp files for the given runners, returning a hash map
associating the runner keyword with the corresponding temp file"
  (into {} (map runner-path! runners)))

(defn middleware
  "Does two things:

1. Modify all :cljsbuild :test-command vectors, swapping :runner, :node-runner,
  :nodejs-runner and :rhino-runner keywords for the string path to the
  corresponding packaged script.
2. Add [com.cemerick/clojurescript-test \"CURRENT_VERSION\"] as a test-scoped
project dependency (equivalent to the user always having the plugin installed in
the :dev profile, and so won't affect the primary dependency graph of downstream
projects)."
  [project]
  (let [runners [[:runner "runner.js"]
                 [:node-runner "node_runner.js"]
                 [:nodejs-runner "node_runner.js"]
                 [:rhino-runner "rhino_runner.js"]]
        runner-paths (runner-paths! runners)
        namespaced-runner-paths (->> runner-paths
                                     (map (fn [[k path]]
                                            [(keyword "cljs.test" (name k)) path]))
                                     (into {}))]
    (-> project
        (update-in [:dependencies]
                   (fnil into [])
                   [['com.cemerick/clojurescript.test version :scope "test"]])
        (update-in [:cljsbuild :test-commands]
                   (partial postwalk-replace runner-paths))
        ((partial postwalk-replace namespaced-runner-paths)))))

