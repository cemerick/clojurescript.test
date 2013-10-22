(defproject com.cemerick/clojurescript.test "0.2.0-SNAPSHOT"
  :description "Port of clojure.test targeting ClojureScript."
  :url "http://github.com/cemerick/clojurescript.test"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1934"]]

  :plugins [[lein-cljsbuild "0.3.4"]]

  :hooks [leiningen.cljsbuild]
  :cljsbuild {:builds [{:source-paths ["src" "test"]
                        :compiler {:output-to "target/cljs/whitespace.js"
                                   :optimizations :whitespace
                                   :pretty-print true}}
                       {:source-paths ["src" "test"]
                        :compiler {:output-to "target/cljs/simple.js"
                                   :optimizations :simple
                                   :pretty-print true}}
                       {:source-paths ["src" "test"]
                        :compiler {:output-to "target/cljs/advanced.js"
                                   :optimizations :advanced
                                   :pretty-print true}}]
              :test-commands {"phantom-whitespace" ["phantomjs" :runner "target/cljs/whitespace.js"]
                              "phantom-simple" ["phantomjs" :runner "target/cljs/simple.js"]
                              "phantom-advanced" ["phantomjs" :runner "target/cljs/advanced.js"]}}

  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

  :profiles {:latest {:dependencies [[org.clojure/clojure "1.5.1"]
                                     [org.clojure/clojurescript "0.0-1934"]]}
             :dev {:dependencies [[com.cemerick/piggieback "0.0.4"]]}
             ; self-reference and chained `lein install; lein test` invocation
             ; needed to use the project as its own plugin. Leiningen :-(
             :self-plugin [:default {:plugins [[com.cemerick/clojurescript.test "0.2.0-SNAPSHOT"]]}]}

  :aliases  {"cleantest" ["with-profile" "self-plugin" ;"self-plugin:self-plugin,latest"
                          "do" "clean," "test"]
             "deploy" ["do" "clean," "deploy" "clojars," "deploy" "releases"]}

  :deploy-repositories {"releases" {:url "https://oss.sonatype.org/service/local/staging/deploy/maven2/" :creds :gpg}
                        "snapshots" {:url "https://oss.sonatype.org/content/repositories/snapshots/" :creds :gpg}}

  ;;maven central requirements
  :scm {:url "git@github.com:cemerick/clojurescript.test.git"}
  :pom-addition [:developers [:developer
                              [:name "Chas Emerick"]
                              [:url "http://cemerick.com"]
                              [:email "chas@cemerick.com"]
                              [:timezone "-5"]]])
