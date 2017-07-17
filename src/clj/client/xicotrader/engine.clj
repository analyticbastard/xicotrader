(ns xicotrader.engine
  (:require
    [clojure.core.async :as a :refer [go-loop >! <!]]
    [com.stuartsierra.component :as component]))

(defn- engine [this]
  (let [events-ch (get-in this [:events :ch])]
    (go-loop []
      (do
        ;; todo process events here
        (<! events-ch))
      (recur))))

(defrecord Component [config]
  component/Lifecycle
  (start [this]
    (engine this)
    this)
  (stop [this]
    this))

(defn new [config]
  (Component. config))
