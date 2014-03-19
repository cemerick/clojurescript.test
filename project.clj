(defproject com.cemerick/clojurescript.test "0.3.0-SNAPSHOT"
  :description "Port of clojure.test targeting ClojureScript."
  :url "http://github.com/cemerick/clojurescript.test"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :test-paths ["target/generated/clj"]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2138"]]

  :cljsbuild {:builds [{:source-paths ["src" "test" "target/generated/cljs"]
                        :compiler {:output-to "target/cljs/whitespace.js"
                                   :optimizations :whitespace
                                   :pretty-print true}}
                       {:source-paths ["src" "test" "target/generated/cljs"]
                        :compiler {:output-to "target/cljs/simple.js"
                                   :optimizations :simple
                                   :pretty-print true}}
                       {:source-paths ["src" "test" "target/generated/cljs"]
                        :compiler {:output-to "target/cljs/advanced.js"
                                   :optimizations :advanced
                                   :pretty-print true}}]
              :test-commands {; PhantomJS tests
                              "phantom-whitespace" ["phantomjs" :runner
                                                    "window.literal_js_was_evaluated=true"
                                                    "target/cljs/whitespace.js"
                                                    "test/cemerick/cljs/test/extra_test_command_file.js"]
                              "phantom-simple" ["phantomjs" :runner
                                                "window.literal_js_was_evaluated=true"
                                                "target/cljs/simple.js"
                                                "test/cemerick/cljs/test/extra_test_command_file.js"]
                              "phantom-advanced" ["phantomjs" :runner
                                                  "window.literal_js_was_evaluated=true"
                                                  "target/cljs/advanced.js"
                                                  "test/cemerick/cljs/test/extra_test_command_file.js"]

                              ; Rhino tests
                              "rhino-whitespace" ["rhino" "-opt" "-1" :rhino-runner
                                                  "this.literal_js_was_evaluated=true "
                                                  "target/cljs/whitespace.js"
                                                  "test/cemerick/cljs/test/extra_test_command_file.js"]
                              "rhino-simple" ["rhino" "-opt" "-1" :rhino-runner
                                                  "this.literal_js_was_evaluated=true"
                                                  "target/cljs/simple.js"
                                                  "test/cemerick/cljs/test/extra_test_command_file.js"]
                              "rhino-advanced" ["rhino" "-opt" "-1" :rhino-runner
                                                  "this.literal_js_was_evaluated=true"
                                                  "target/cljs/advanced.js"
                                                  "test/cemerick/cljs/test/extra_test_command_file.js"]

                              ; node tests
                              "node-simple" ["node" :node-runner
                                             "this.literal_js_was_evaluated=true"
                                             "target/cljs/simple.js"
                                             "test/cemerick/cljs/test/extra_test_command_file.js"]
                              "node-advanced" ["node" :node-runner
                                               "this.literal_js_was_evaluated=true"
                                               "target/cljs/advanced.js"
                                               "test/cemerick/cljs/test/extra_test_command_file.js"]}}

  :cljx {:builds [{:source-paths ["test"]
                   :output-path "target/generated/clj"
                   :rules :clj}
                  {:source-paths ["test"]
                   :output-path "target/generated/cljs"
                   :rules :cljs}]}
  
  :profiles {:latest {:dependencies [[org.clojure/clojure "1.6.0-alpha3"]
                                     [org.clojure/clojurescript "0.0-2138"]]}
             :dev {:dependencies [[org.clojure/core.async "0.1.267.0-0d7780-alpha"]]
                   :plugins [[lein-cljsbuild "1.0.1"]
                             [com.keminglabs/cljx "0.3.2"]
                             [com.cemerick/austin "0.1.4-SNAPSHOT"]]}
             ; self-reference and chained `lein install; lein test` invocation
             ; needed to use the project as its own plugin. Leiningen :-(
             :self-plugin [:default {:plugins [[com.cemerick/clojurescript.test "0.3.0-SNAPSHOT"]]}]}

  :aliases  {"cleantest" ["with-profile" "self-plugin:self-plugin,latest"
                          "do" "clean," "cljx" "once," "test," "cljsbuild" "test"]
             "release" ["do" "clean," "deploy" "clojars," "deploy" "releases"]}

  :deploy-repositories {"releases" {:url "https://oss.sonatype.org/service/local/staging/deploy/maven2/" :creds :gpg}
                        "snapshots" {:url "https://oss.sonatype.org/content/repositories/snapshots/" :creds :gpg}}

  ;;maven central requirements
  :scm {:url "git@github.com:cemerick/clojurescript.test.git"}
  :pom-addition [:developers [:developer
                              [:name "Chas Emerick"]
                              [:url "http://cemerick.com"]
                              [:email "chas@cemerick.com"]
                              [:timezone "-5"]]])
