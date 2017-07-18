(ns xicotrader.engine
  (:require
    [clojure.core.async :as a :refer [go-loop >! <!]]
    [com.stuartsierra.component :as component]
    [xicotrader
     [events :as events]
     [portfolio :as portfolio]
     [strategy :as strategy]]))

(defn- engine [events-in events-out config initial-portfolio]
  (println initial-portfolio)
  (go-loop [portfolio initial-portfolio]
    (let [{:keys [portfolio-updates tick-data]} (<! events-in)
          action (strategy/evaluate portfolio tick-data)]
      (>! events-out action)
      (recur (portfolio/update-portfolio portfolio portfolio-updates)))))

(defn start-engine [this config]
  (let [{:keys [in-chan out-chan] :as events} (:events this)
        initial-portfolio (events/init events)]
    (engine in-chan out-chan config initial-portfolio)))

(defrecord Component [config]
  component/Lifecycle
  (start [this]
    (start-engine this config)
    this)
  (stop [this]
    this))

(defn new [config]
  (Component. config))
