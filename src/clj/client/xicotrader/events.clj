(ns xicotrader.events
  (:require
    [clojure.core.async :as a :refer [go-loop >! <! >!!]]
    [com.stuartsierra.component :as component]
    [xicotrader.service :as service]))

(defn- receive-loop [this]
  (let [service-ch (get-in this [:service :ch])]
    #_(go-loop []
      ;; todo format raw events here
      ;(>! ch (<! service-ch))
      (recur)))
  this)

(defprotocol Events
  (init [this]))

(defrecord Component [config]
  Events
  (init [this]
    (let [service (:service this)
          portfolio (service/init service)]
      portfolio))

  component/Lifecycle
  (start [this]
    (let [in-chan (a/chan)
          out-chan (a/chan)]
      (receive-loop (assoc this
                      :in-chan in-chan
                      :out-chan out-chan))))
  (stop [this]
    this))

(defn new [config]
  (Component. config))
