(ns xicotrader.engine
  (:require
    [clojure.core.async :as a :refer [go-loop >! <!]]
    [com.stuartsierra.component :as component]
    [xicotrader
     [portfolio :as portfolio]
     [strategy :as strategy]]))

(defn- engine [{:keys [events] :as this}
               {:keys [initial-portfolio] :as config}]
  (let [events-in  (:in-chan events)
        events-out (:out-chan events)]
    (go-loop [portfolio initial-portfolio]
      (let [{:keys [portfolio-updates tick-data]} (<! events-in)
            action (strategy/evaluate portfolio tick-data)]
        (>! events-out action)
        (recur (portfolio/update portfolio portfolio-updates))))))

(defrecord Component [config]
  component/Lifecycle
  (start [this]
    (engine this config)
    this)
  (stop [this]
    this))

(defn new [config]
  (Component. config))
