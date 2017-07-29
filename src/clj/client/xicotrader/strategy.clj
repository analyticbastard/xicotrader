(ns xicotrader.strategy
  (:require
    [clojure.core.async :as a :refer [go-loop >! <! <!! >!!]]
    [com.stuartsierra.component :as component]))

(defprotocol Strategy
  (compute [strategy portfolio market tick-data]))

(defn evaluate [{:keys [ch-in ch-out]} portfolio tick-data]
  (>!! ch-in {:portfolio portfolio :tick-data tick-data})
  (<!! ch-out))

(defn- strategy-loop [strategy ch-in ch-out]
  (go-loop [market {}]
    (when-let [data (<! ch-in)]
      (let [{:keys [portfolio tick-data]} data
            updated-market (apply merge market tick-data)]
        (>! ch-out (.compute strategy portfolio market tick-data))
        (recur updated-market)))))

(defrecord Component [config]
  component/Lifecycle
  (start [this]
    (let [ch-in (a/chan)
          ch-out (a/chan)]
      (strategy-loop (:strategy this) ch-in ch-out)
      (assoc this :ch-in ch-in
                  :ch-out ch-out)))
  (stop [this]
    (a/close! (:ch-in this))
    (a/close! (:ch-out this))
    this))

(defn new [config]
  (Component. config))
