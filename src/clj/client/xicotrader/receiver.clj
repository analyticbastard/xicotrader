(ns xicotrader.receiver
  (:require
    [clojure.core.async :as a :refer [go-loop >! <! >!! alts!!]]
    [com.stuartsierra.component :as component]
    [xicotrader.service :as service]))

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
    ;; todo - validate data coming in
    (>! c-in data)))

(defn new [config]
  (Component. config))
