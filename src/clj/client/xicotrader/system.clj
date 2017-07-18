(ns xicotrader.system
  (:require
    [com.stuartsierra.component :as component]
    [xicotrader
     [engine :as engine]
     [events :as events]
     [executor :as executor]
     [kraken :as kraken]
     [simulator-service :as simulator-service]
     [strategy :as strategy]]))

(defn make-system-component [{:keys [engine events simulator kraken strategy]}]
  (component/system-map
    :engine    (engine/new engine)
    :events    (events/new events)
    :simulator (simulator-service/new simulator)
    :strategy  (strategy/new strategy)))

(defn make-system-dependencies []
  {:engine {:events :events
            :strategy :strategy}
   :events {:service :simulator}
   :simulator {}})

(defn start-system [system]
  (component/start system))

(defn stop-system [system]
  (component/stop system))
