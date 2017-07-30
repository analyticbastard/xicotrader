(ns xicotrader.service)

(defprotocol Service
  (init [this trade-assets]))

(defn init [service trade-assets]
  (.init service trade-assets))