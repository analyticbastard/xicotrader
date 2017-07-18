(ns xicotrader.service)

(defprotocol Service
  (init [this]))

(defn init [service]
  (.init service))