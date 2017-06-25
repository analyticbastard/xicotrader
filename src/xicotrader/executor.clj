(ns xicotrader.executor
  (:require
    [com.stuartsierra.component :as component]))

(defrecord Component [config]
  component/Lifecycle
  (start [this]
    this)
  (stop [this]
    this))

(defn new [config]
  (Component. config))
