(ns xicotrader.controller
  (:require
    [clojure.core.async :as a :refer [go-loop >! <!]]
    [clojure.tools.logging :as log]
    [com.stuartsierra.component :as component]))

(defn- receive-loop [{:keys [rec s-to]}]
  (go-loop []
    (when-let [data (<! rec)]
      (>! s-to data)
      (recur))))

(defn- send-loop [{:keys [send s-from]}]
  (go-loop []
    (when-let [data (<! s-from)]
      (>! send data)
      (recur))))

(defrecord Component [config]
  component/Lifecycle
  (start [this]
    (receive-loop this)
    (send-loop this)
    this)
  (stop [this]
    this))

(defn new [config]
  (Component. config))
