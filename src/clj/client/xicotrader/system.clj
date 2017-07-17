(ns xicotrader.system
  (:require
    [com.stuartsierra.component :as component]
    [xicotrader
     [engine :as engine]
     [events :as events]
     [executor :as executor]
     [kraken :as kraken]
     [strategy :as strategy]]))

(defn make-system-component [{:keys [engine events execution kraken strategy]}]
  (component/system-map
    :engine   (engine/new engine)
    :events   (events/new events)
    :executor (executor/new execution)
    :kraken   (kraken/new kraken (:symbols events))
    :strategy (strategy/new strategy)))

(defn make-system-dependencies []
  {:engine {:events :events
            :executor :executor
            :strategy :strategy}
   :events {:service :kraken}
   :executor {}
   :kraken {}})

(defn start-system [system]
  (component/start system))

(defn stop-system [system]
  (component/stop system))
