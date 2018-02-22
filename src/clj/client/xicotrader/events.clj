(ns xicotrader.events
  (:require
    [clojure.core.async :as a :refer [go-loop >! <! >!! alts!!]]
    [com.stuartsierra.component :as component]
    [xicotrader.service :as service]))

(defn- receive-loop [{:keys [service ch-in]}]
  (let [service-in (:ch-in service)]
    (go-loop []
      (when-let [service-data (<! service-in)]
        (>! ch-in service-data)
        (recur)))))

(defn- send-loop [{:keys [service ch-out]}]
  (let [service-out (:ch-out service)]
    (go-loop []
      (when-let [action (<! ch-out)]
        (>! service-out action)
        (recur)))))

(defprotocol Events
  (init [this]))

(defrecord Component [config]
  Events
  (init [this]
    (let [service (:service this)
          portfolio (service/init-service service (:trade config))]
      portfolio))

  component/Lifecycle
  (start [this]
    (let [in-chan (a/chan)
          out-chan (a/chan)
          this (assoc this :ch-in  in-chan
                           :ch-out out-chan)]
      (receive-loop this)
      (send-loop this)
      this))
  (stop [this]
    (a/close! (:ch-in this))
    (a/close! (:ch-out this))
    this))

(defn new [config]
  (Component. config))
