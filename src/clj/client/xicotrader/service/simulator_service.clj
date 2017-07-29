(ns xicotrader.service.simulator-service
  (:require
    [clojure.core.async :as a :refer [go-loop <! >!]]
    [com.stuartsierra.component :as component]
    [clj-http.client :as http]
    [cheshire.core :as cheshire]
    [xicotrader.service :as service])
  (:import (clojure.lang Atom)))

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

(defn poll-loop [{:keys [ch-in]}
                 config ^Atom running?]
  (go-loop []
    (>! ch-in {:tick-data (get-tick config "ETHEUR")
               :portfolio-updates {}})
    (Thread/sleep 1000)
    (when @running? (recur))))

(defrecord Component [config running?]
  component/Lifecycle
  (start [this]
    (assoc this :ch-in  (a/chan)
                :ch-out (a/chan)))
  (stop [this]
    (reset! running? false)
    this)

  service/Service
  (init [this]
    (reset! running? true)
    (poll-loop this config running?)
    (get-user-data config)))

(defn new [config]
  (Component. config (atom false)))
