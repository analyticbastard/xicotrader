(ns xicotrader.receiver
  (:require
    [clojure.core.async :as a :refer [go-loop >! <! >!! alts!!]]
    [com.stuartsierra.component :as component]
    [schema.core :as s]
    [xicotrader
     [service :as service]
     [schema :refer [Event]]]))

(defprotocol Receiver
  (receive [this data]))

(defrecord Component [config]
  component/Lifecycle
  (start [this]
    (let [c-in (a/chan)
          this (assoc this :c-in c-in)]
      this))
  (stop [this]
    (a/close! (:c-in this))
    this)

  Receiver
  (receive [{:keys [c-in] :as this} data]
    (s/validate Event data)
    (>! c-in data)))

(defn new [config]
  (Component. config))
