(ns xicotrader.system
  (:require
    [com.stuartsierra.component :as component]
    [xicotrader
     [engine :as engine]
     [events :as events]
     [strategy :as strategy]]
    [xicotrader.service
     [simulator-service :as simulator-service]]
    [xicotrader.strategy
     [arbitrage :as arbitrage]]))

(defn make-system-component [{:keys [engine events simulator kraken strategy arbitrage]}]
  (component/system-map
    :engine    (engine/new engine)
    :events    (events/new events)
    :simulator (simulator-service/new simulator)
    :strategy  (strategy/new strategy)
    :arbitrage (arbitrage/new arbitrage)))

(defn make-system-dependencies []
  {:engine    {:events   :events
               :strategy :strategy}
   :strategy  {:strategy :arbitrage}
   :events    {:service :simulator}
   :simulator {}})

(defn start-system [system]
  (component/start system))

(defn stop-system [system]
  (component/stop system))
