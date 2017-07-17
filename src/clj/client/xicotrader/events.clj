(ns xicotrader.events
  (:require
    [clojure.core.async :as a :refer [go-loop >! <!]]
    [com.stuartsierra.component :as component]))

(defn- events [this]
  (let [ch (a/chan)
        service-ch (get-in this [:service :ch])]
    (go-loop []
      ;; todo format raw events here
      (>! ch (<! service-ch))
      (recur))
    ch))

(defrecord Component [config]
  component/Lifecycle
  (start [this]
    (assoc this :ch (events this)))
  (stop [this]
    this))

(defn new [config]
  (Component. config))
