(ns xicotrader.simulator-service
  (:require
    [com.stuartsierra.component :as component]
    [clj-http.client :as http]
    [xicotrader.service :as service]))

(defn get-user-data [{:keys [host port private-url user-id secret-key]}]
  (let [url (format "http://%s:%s/%s/%s" host port private-url "portfolio")
        response (http/get url
                           {:query-params {:user-id    user-id
                                           :secret-key secret-key}})]
    (:body response)))

(defrecord Component [config]
  component/Lifecycle
  (start [this]
    this)
  (stop [this]
    this)

  service/Service
  (init [this]
    (get-user-data config)))

(defn new [config]
  (Component. config))
