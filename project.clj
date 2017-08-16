(defproject xicotrader "0.1.0-SNAPSHOT"
  :description "Automatically trade cryoptocurrency"
  :url "https://github.com/analyticbastard/xicotrader"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.3.442"]
                 [medley "1.0.0"]
                 [prismatic/schema "1.0.4"]
                 [com.stuartsierra/component "0.3.0"]
                 [cheshire "5.7.1"]
                 [im.chit/cronj "1.4.4"]
                 [clj-http "3.6.1"]

                 ;; Web stuff
                 [metosin/compojure-api "1.1.6" :exclusions [org.clojure/clojure]]
                 [juxt.modular/ring "0.5.3" :exclusions [org.clojure/clojure]]
                 [juxt.modular/http-kit "0.5.4" :exclusions [org.clojure/clojure]]
                 [metosin/ring-http-response "0.6.5" :exclusions [ring/ring-core]]
                 [ring/ring-json "0.4.0" :exclusions [org.clojure/clojure]]]
  :source-paths ["src/clj/common"]
  :resource-paths ["resources/client" "resources/simulator"]
  :target-path "target/%s"
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.10" :exclusions [org.clojure/clojure]]
                                  [org.clojure/tools.nrepl "0.2.12" :exclusions [org.clojure/clojure]]]
                   :source-paths ["dev"]}
             :client {:source-paths ["src/clj/client" "src/clj/common"]
                      :resource-paths ["resources/client"]}
             :simulator {:source-paths ["src/clj/simulator" "src/clj/common"]
                         :resource-paths ["resources/simulator" "resources/marketdata"]}
             :prod {:aot :all}})
