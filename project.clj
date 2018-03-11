(defproject xicotrader "0.1.0-SNAPSHOT"
  :description "Automatically trade cryoptocurrency"
  :url "https://github.com/analyticbastard/xicotrader"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.3.442"]
                 [org.clojure/tools.logging "0.3.1"]
                 [clj-time/clj-time "0.12.0"]
                 [medley "1.0.0"]
                 [prismatic/schema "1.0.4"]
                 [com.stuartsierra/component "0.3.0"]
                 [com.cemerick/pomegranate "0.3.1"]]
  :source-paths ["src/clj"]
  :test-paths ["test/clj"]
  :resource-paths ["resources/common" "resources/client"]
  :target-path "target/%s"
  :plugins [[lein-cloverage "1.0.7-SNAPSHOT"]]
  :aot [clojure.tools.logging.impl com.stuartsierra.component
        xicotrader.strategy xicotrader.schema]
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.10" :exclusions [org.clojure/clojure]]
                                  [org.clojure/tools.nrepl "0.2.12" :exclusions [org.clojure/clojure]]]
                   :source-paths ["dev"]}
             :uberjar {:aot :all}})
