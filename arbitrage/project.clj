(defproject xicotrader/arbitrage "0.1.0-SNAPSHOT"
  :description "Arbitrage strategy example to run with xicotrader's simulator"
  :url "https://github.com/analyticbastard/xicotrader"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :profiles {:provided {:dependencies [[org.clojure/clojure "1.8.0"]
                                       [com.stuartsierra/component "0.3.0" :exclusions [org.clojure/clojure]]
                                       [xicotrader "0.1.0-SNAPSHOT" :exclusions [org.clojure/clojure]]]}
             :dev {:dependencies [[org.clojure/tools.namespace "0.2.10" :exclusions [org.clojure/clojure]]
                                  [org.clojure/tools.nrepl "0.2.12" :exclusions [org.clojure/clojure]]]}
             :uberjar {:aot :all}}
  :target-path "target/%s")
