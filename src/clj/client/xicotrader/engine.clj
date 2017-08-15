(ns xicotrader.engine
  (:require
    [clojure.core.async :as a :refer [go-loop >! <!]]
    [clojure.tools.logging :as log]
    [com.stuartsierra.component :as component]
    [xicotrader
     [events :as events]
     [portfolio :as portfolio]
     [strategy :as strategy]]))

(defn- engine-loop [{events-in :ch-in events-out :ch-out} strategy config initial-portfolio]
  (go-loop [portfolio initial-portfolio]
    (when-let [{:keys [portfolio-updates] :as event} (<! events-in)]
      (let [new-portfolio (portfolio/update-portfolio portfolio portfolio-updates)]
        (when-let [action (strategy/evaluate strategy new-portfolio event)]
          (>! events-out action)
          (recur new-portfolio))))))

(defrecord Component [config]
  component/Lifecycle
  (start [this]
    (let [events (:events this)
          strategy (:strategy this)
          initial-portfolio (events/init events)]
      (engine-loop events strategy config initial-portfolio))
    this)
  (stop [this]
    this))

(defn new [config]
  (Component. config))
