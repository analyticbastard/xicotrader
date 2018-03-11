(ns xicotrader.sender
  (:require
    [clojure.core.async :as a :refer [go-loop >! <! >!! alts!!]]
    [com.stuartsierra.component :as component]
    [schema.core :as s]
    [xicotrader
     [service :as service]
     [schema :refer [Action]]]))

(defprotocol Connector
  (trade [this data]))

(defn- send-loop [{:keys [c-out]} sender]
  (go-loop []
    (when-let [action (<! c-out)]
      (s/validate Action action)
      (.trade sender action)
      (recur))))

(defrecord Component [config]
  component/Lifecycle
  (start [this]
    (let [c-out (a/chan)
          this (assoc this :c-out c-out)]
      (send-loop this (:service this))
      this))
  (stop [this]
    (a/close! (:c-out this))
    this))

(defn new [config]
  (Component. config))
