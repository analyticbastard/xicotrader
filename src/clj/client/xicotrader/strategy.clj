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

(defn safe-compute [strategy portfolio tick-data]
  (try
    (s/validate Portfolio portfolio)
    (s/validate Tick tick-data)
    (s/validate Order (.compute strategy portfolio tick-data))
    (catch Throwable t
      (log/error (.getMessage t)))))

(defn- strategy-loop [strategy ch-from c-to]
  (go-loop []
    (when-let [{:keys [portfolio tick-data]} (<! ch-from)]
      (let [action (safe-compute strategy portfolio tick-data)]
        (when action (>! c-to action))
        (recur)))))

(defrecord Component [config]
  component/Lifecycle
  (start [this]
    (let [c-from (a/chan)
          c-to (a/chan)]
      (strategy-loop (:strategy this) c-from c-to)
      (assoc this :c-from c-from
                  :c-to c-to)))
  (stop [this]
    (a/close! (:c-from this))
    (a/close! (:c-to this))
    this))

(defn new [config]
  (Component. config))
