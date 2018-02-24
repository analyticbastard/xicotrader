(ns xicotrader.strategy
  (:require
    [clojure.core.async :as a :refer [go-loop >! <! <!! >!! alts!!]]
    [clojure.tools.logging :as log]
    [com.stuartsierra.component :as component]
    [schema.core :as s]
    [xicotrader.schema :refer :all])
  (:gen-class))

(defprotocol Strategy
  (compute [this strategy portfolio tick-data]))

(defn evaluate [{:keys [ch-in ch-out]}
                portfolio
                {:keys [portfolio-updates] :as event}]
  (>!! ch-in (assoc event :portfolio portfolio))
  (let [[msg ch] (alts!! [ch-out (a/timeout 100)])]
    (if (= ch ch-out) msg {})))

(defn safe-compute [strategy portfolio tick-data]
  (try
    (s/validate Portfolio portfolio)
    (s/validate Tick tick-data)
    (s/validate Order (.compute strategy portfolio tick-data))
    (catch Throwable t
      (log/error (.getMessage t)))))

(defn- strategy-loop [strategy ch-in ch-out]
  (go-loop []
    (when-let [{:keys [portfolio tick-data]} (<! ch-in)]
      (let [action (safe-compute strategy portfolio tick-data)]
        (when action (>! ch-out action))
        (recur)))))

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
