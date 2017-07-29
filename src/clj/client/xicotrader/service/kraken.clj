(ns xicotrader.service.kraken
  (:require
    [clojure.core.async :as a :refer [go >!]]
    [clojure.edn :as edn]
    [clj-http.client :as client]
    [cheshire.core :as cheshire]
    [com.stuartsierra.component :as component]
    [cronj.core :as cronj]))

(defn- parse-body [response]
  (-> response :body cheshire/parse-string))

(defn- get-and-parse-body [url & [query-params]]
  (parse-body (client/get url
                          (merge {:accept :json}
                                 (when query-params {:query-params query-params})))))

(defn get-assets
  ([]
   (get-and-parse-body "https://api.kraken.com/0/public/Assets"))
  ([ch]
   (go (>! ch (get-assets)))))

(defn get-markets
  ([]
   (get-and-parse-body "https://api.kraken.com/0/public/AssetPairs"))
  ([ch]
   (go (>! ch (get-markets)))))

(defn get-ticker
  ([pair]
   (get-and-parse-body "https://api.kraken.com/0/public/Ticker" {"pair" pair}))
  ([ch pair]
   (go (>! ch (get-ticker pair)))))

(defn get-ohcl
  ([]
   (get-and-parse-body "https://api.kraken.com/0/public/OHLC"))
  ([ch]
   (go (>! ch (get-ohcl)))))

(defn- poll-kraken [ch assets]
  (doseq [asset assets]
    (get-ticker ch asset)))

(def select-values (comp vals select-keys))

(defrecord Component [config symbols]
  component/Lifecycle
  (start [this]
    (let [ch (a/chan)
          id "Kraken"
          kraken-mapping (into {} (:assets config))
          cron {:id       id
                :handler  (fn [_ _] (poll-kraken ch (select-values kraken-mapping symbols)))
                :schedule (:cron config)
                :opts     {:output (format "%s %s" id "task executing")}}
          job (cronj/cronj :entries [cron])]
      (cronj/start! job)
      (assoc this :job job :ch ch)))
  (stop [this]
    (let [job (:job this)]
      (when job (cronj/stop! job)))
    this))

(defn new [config symbols]
  (Component. config symbols))