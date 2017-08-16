(ns xicotrader.service.simulator-service
  (:require
    [clojure.set :as set]
    [clojure.edn :as edn]
    [clojure.tools.logging :as log]
    [clojure.core.async :as a :refer [go-loop <! >! <!! >!!]]
    [clojure.core.async.impl.protocols :as async-protocols]
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

(def ch (a/chan))

(defn parse-response [body]
  (cheshire/parse-string body keyword))

(defn get-tick [{:keys [host port public-url]} pair & [success-callback error-callback]]
  (let [url (format "http://%s:%s/%s/%s" host port public-url "tick")
        on-success (or success-callback (fn [response] (>!! ch response)))]
    (http/get url
              {:query-params {:pair pair}
               :async? true}
              (fn [response]
                (-> (:body response) parse-response on-success))
              (fn [exeption]))
    (when-not success-callback
      (<!! ch))))

(defn trade [{:keys [host port private-url user-id secret-key]} action]
  (let [url (format "http://%s:%s/%s/%s" host port private-url "trade")
        response (http/post url
                            {:query-params {:user-id user-id
                                            :secret-key secret-key}
                             :form-params action
                             :content-type :application/edn
                             :async? false})
        body (-> (:body response) edn/read-string)]
    (if (empty? body) nil body)))

(defn poll-loop [{:keys [ch-in]} config pairs]
  (go-loop []
    (when-not (async-protocols/closed? ch-in)
      (doseq [pair pairs]
        (get-tick config ((config :translation) pair)
                  (fn [response]
                    (>!! ch-in {:tick-data         (assoc response :pair pair)
                                :portfolio-updates {}}))))
      (Thread/sleep (:polltime config))
      (recur))))

(defn order-loop [{:keys [ch-out ch-in]} config]
  (go-loop []
    (when-let [{:keys [pair] :as action} (<! ch-out)]
      (when-let [portfolio-updates (and (seq action)
                                        (trade config
                                               (assoc action
                                                 :pair ((config :translation) pair))))]
        (>! ch-in {:portfolio-updates (portfolio-updates (config :user-id))}))
      (recur))))

(defrecord Component [config]
  component/Lifecycle
  (start [this]
    (assoc this :ch-in  (a/chan)
                :ch-out (a/chan)))
  (stop [this]
    (a/close! (:ch-in this))
    (a/close! (:ch-out this))
    this)

  service/Service
  (init [this trade-assets]
    (poll-loop this config trade-assets)
    (order-loop this config)
    (get-user-data config)))

(defn new [config]
  (Component. config))
