(ns xicotrader.engine
  (:require
    [clojure.core.async :as a :refer [go-loop >! <!]]
    [clojure.tools.logging :as log]
    [com.stuartsierra.component :as component]
    [xicotrader
     [events :as events]
     [portfolio :as portfolio]
     [strategy :as strategy]]))

(defn- engine-loop [{:keys [ch-in ch-out]} strategy config initial-portfolio running?]
  (go-loop [portfolio initial-portfolio]
    (when-let [{:keys [portfolio-updates tick-data]} (<! ch-in)]
      (let [new-portfolio (portfolio/update-portfolio portfolio portfolio-updates)]
        (when-let [action (strategy/evaluate strategy new-portfolio tick-data)]
          (>! ch-out action))
        (when @running? (recur new-portfolio))))))

(defrecord Component [config running?]
  component/Lifecycle
  (start [this]
    (let [events (:events this)
          strategy (:strategy this)
          initial-portfolio (events/init events)]
      (engine-loop events strategy config initial-portfolio running?))
    this)
  (stop [this]
    (reset! running? false)
    this))

(defn new [config]
  (Component. config (atom true)))
