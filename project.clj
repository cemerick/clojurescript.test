(defproject com.cemerick/clojurescript.test "0.0.1-SNAPSHOT"
  :description "Port of clojure.test targeting ClojureScript."
  :url "http://github.com/cemerick/clojurescript.test"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/clojurescript "0.0-1586"]]
  
  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
  
  :profiles {:1.5 {:dependencies [[org.clojure/clojure "1.5.0-RC17"]]}
             :dev {:dependencies [[com.cemerick/piggieback "0.0.4-SNAPSHOT"]]}}
  
  :deploy-repositories {"releases" {:url "https://oss.sonatype.org/service/local/staging/deploy/maven2/" :creds :gpg}
                        "snapshots" {:url "https://oss.sonatype.org/content/repositories/snapshots/" :creds :gpg}}
  
  :aliases  {"all" ["with-profile" "dev:dev,1.5"]}
  
  ;;maven central requirements
  :scm {:url "git@github.com:cemerick/clojurescript.test.git"}
  :pom-addition [:developers [:developer
                              [:name "Chas Emerick"]
                              [:url "http://cemerick.com"]
                              [:email "chas@cemerick.com"]
                              [:timezone "-5"]]])
