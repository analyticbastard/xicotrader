(ns xicotrader.strategy.arbitrage
  (:require
    [xicotrader.strategy :as strategy]))

(defrecord Arbitrage []
  strategy/Strategy
  (evaluate [this portfolio event]
    ))
