(ns xicotrader.strategy
  (:require
    [clojure.core.async :as a :refer [go-loop >! <! <!! >!! alts!!]]
    [clojure.tools.logging :as log]
    [com.stuartsierra.component :as component]
    [schema.core :as s]
    [xicotrader
     [schema :refer [Event Action]]])
  (:gen-class))

(defprotocol Strategy
  (compute [this event]))

(defn safe-compute [strategy event]
  (try
    (s/validate Event event)
    (s/validate Action (.compute strategy event))
    (catch Throwable t
      (log/error (.getMessage t)))))

(defn- strategy-loop [strategy ch-from c-to]
  (go-loop []
    (when-let [event (<! ch-from)]
      (let [action (safe-compute strategy event)]
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
