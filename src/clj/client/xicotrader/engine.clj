(ns xicotrader.engine
  (:require
    [clojure.core.async :as a :refer [go-loop >! <!]]
    [clojure.tools.logging :as log]
    [com.stuartsierra.component :as component]
    [xicotrader
     [events :as events]
     [portfolio :as portfolio]
     [strategy :as strategy]]))

(defn- engine-loop [{events-in :ch-in events-out :ch-out}
                    strategy config initial-portfolio running?]
  (go-loop [portfolio initial-portfolio]
    (let [{:keys [portfolio-updates tick-data]} (<! events-in)
          new-portfolio (portfolio/update-portfolio portfolio portfolio-updates)
          action (strategy/evaluate strategy new-portfolio tick-data)]
      (log/info tick-data)
      (when action (>! events-out action))
      (recur new-portfolio))))

(defrecord Component [config running?]
  component/Lifecycle
  (start [this]
    (let [events (:events this)
          strategy (:strategy this)
          initial-portfolio (events/init events)]
      (engine-loop events strategy config initial-portfolio running?))
    this)
  (stop [this]
    this))

(defn new [config]
  (Component. config (atom false)))
