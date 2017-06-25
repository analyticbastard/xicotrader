(defproject xicotrader "0.1.0-SNAPSHOT"
  :description "Automatically trade cryoptocurrency"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.3.442"]
                 [medley "1.0.0"]
                 [prismatic/schema "1.0.4"]
                 [com.stuartsierra/component "0.3.0"]
                 [cheshire "5.7.1"]
                 [im.chit/cronj "1.4.4"]
                 [clj-http "3.6.1"]]
  :target-path "target/%s"
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.10"]
                                  [org.clojure/tools.nrepl "0.2.12"]]
                   :source-paths ["dev"]}

             :prod {:aot :all}})
