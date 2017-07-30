(ns xicotrader.strategy
  (:require
    [clojure.core.async :as a :refer [go-loop >! <! <!! >!! alts!!]]
    [com.stuartsierra.component :as component]))

(defprotocol Strategy
  (compute [strategy portfolio market tick-data]))

(defn evaluate [{:keys [ch-in ch-out]} portfolio tick-data]
  (>!! ch-in {:portfolio portfolio :tick-data tick-data})
  (alts!! [ch-out (a/timeout 100)]))

(defn- strategy-loop [strategy ch-in ch-out]
  (go-loop [market {}]
    (when-let [{:keys [portfolio tick-data]} (<! ch-in)]
      (let [updated-market (assoc market (:pair tick-data) (:last tick-data))
            action (.compute strategy portfolio updated-market tick-data)]
        (when action (>! ch-out action))
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
