(ns xicotrader.strategy.arbitrage
  (:require
    [com.stuartsierra.component :as component]
    [xicotrader.strategy :as strategy]))

(defn do-arbitrage [this portfolio tick-data]
  {:buy "ETHUSD"
   :qty 1.0})

(defrecord Component [config]
  component/Lifecycle
  (start [this]
    this)
  (stop [this]
    this)

  strategy/Strategy
  (compute [strategy portfolio market tick-data]
    (do-arbitrage portfolio market tick-data)))

(defn new [config]
  (Component. config))
