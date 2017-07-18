(ns xicotrader.strategy
  (:require
    [com.stuartsierra.component :as component]))

(defn evaluate [portcolio event]
  )

(defrecord Component [config]
  component/Lifecycle
  (start [this]
    this)
  (stop [this]
    this))

(defn new [config]
  (Component. config))
