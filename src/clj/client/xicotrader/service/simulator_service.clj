(ns xicotrader.service.simulator-service
  (:require
    [clojure.core.async :as a :refer [go-loop <! >!]]
    [clojure.tools.logging :as log]
    [com.stuartsierra.component :as component]
    [clj-http.client :as http]
    [cheshire.core :as cheshire]
    [xicotrader.service :as service]))

(defn get-user-data [{:keys [host port private-url user-id secret-key]}]
  (let [url (format "http://%s:%s/%s/%s" host port private-url "portfolio")
        response (http/get url
                           {:query-params {:user-id    user-id
                                           :secret-key secret-key}})]
    (cheshire/parse-string (:body response))))

(defn get-tick [{:keys [host port public-url]} pair]
  (let [url (format "http://%s:%s/%s/%s" host port public-url "tick")
        response (http/get url
                           {:query-params {:pair pair}})]
    (cheshire/parse-string (:body response))))

(defn poll-loop [{:keys [ch-in]} config]
  (go-loop []
    (when-let [event (>! ch-in {:tick-data         (get-tick config "ETHEUR")
                                :portfolio-updates {}})]
      (Thread/sleep 1000)
      (recur))))

(defn order-loop [{:keys [ch-out]} config]
  (go-loop []
    (let [action (<! ch-out)]
      (log/info "Will send" action)
      (recur))))

(defrecord Component [config]
  component/Lifecycle
  (start [this]
    (assoc this :ch-in  (a/chan)
                :ch-out (a/chan)))
  (stop [this]
    this)

  service/Service
  (init [this]
    (poll-loop this config)
    (order-loop this config)
    (get-user-data config)))

(defn new [config]
  (Component. config))
