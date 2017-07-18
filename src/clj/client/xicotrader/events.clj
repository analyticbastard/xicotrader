(ns xicotrader.events
  (:require
    [clojure.core.async :as a :refer [go-loop >! <! >!!]]
    [com.stuartsierra.component :as component]
    [xicotrader.service :as service]))

(defn- receive-loop [{:keys [service ch-in]} running?]
  (let [service-in (:ch-in service)]
    (go-loop []
      (>! ch-in (<! service-in))
      (when @running? (recur)))))

(defprotocol Events
  (init [this]))

(defrecord Component [config running?]
  Events
  (init [this]
    (reset! running? true)
    (let [service (:service this)
          portfolio (service/init service)]
      portfolio))

  component/Lifecycle
  (start [this]
    (let [in-chan (a/chan)
          out-chan (a/chan)
          this (assoc this :ch-in  in-chan
                           :ch-out out-chan)]
      (receive-loop this running?)
      this))
  (stop [this]
    (reset! running? false)
    this))

(defn new [config]
  (Component. config (atom false)))
