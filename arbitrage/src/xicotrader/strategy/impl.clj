(ns xicotrader.strategy.impl
  (:require
    [xicotrader.arbitrage.core :refer [do-when-previous-action-filled do-arbitrage]])
  (:import [xicotrader.strategy Strategy]
           [com.stuartsierra.component Lifecycle])
  (:gen-class))

(defrecord Component [config]
  Lifecycle
  (start [this]
    this)
  (stop [this]
    this)

  Strategy
  (compute [strategy portfolio portfolio-updates market tick-data]
    (do-when-previous-action-filled portfolio-updates)
    (do-arbitrage portfolio market tick-data)))

(defn new [config]
  (Component. config))
