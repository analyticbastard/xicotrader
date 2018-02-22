(ns xicotrader.loader
  (:require
    [xicotrader.strategy :as strategy]
    [xicotrader.strategy
     [arbitrage :as arbitrage]]
    [xicotrader.service
     [simulator-service :as simulator]]))

(defn load-strategy [config system-map strategy-key]
  (arbitrage/new (:arbitrage config)))

(defn load-service [config system-map strategy-key]
  (simulator/new (:simulator config)))