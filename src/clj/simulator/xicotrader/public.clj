(ns xicotrader.public
  (require
    [clojure.set :as set]
    [clojure.string :as string]
    [clojure.java.io :as io]
    [clj-time.core :as time :refer [now]]
    [cheshire.core :as cheshire]))

(defn get-pairs []
  ["BTCUSD" "ETHBTC" "ETHUSD"])

(def base-time (atom nil))

(defn load-data [pair]
  (some-> (format "%s.json" (string/lower-case pair))
          (io/resource)
          (io/reader)
          (cheshire/parsed-seq keyword)
          (first)
          (#(do (when-not @base-time
                  (reset! base-time ((first %) :date)))
                %))))

;; 10 min in reality is 1 sec in simulation assuming 5 min unix timestamps in milliseconds
(def simulation-time-factor (* 600))

(defn- shift-time [right-now]
  (fn [$]
    (-> (set/rename-keys $ {:close :last})
        (update :date #(time/plus right-now
                                  (-> (- % @base-time)
                                      (/ simulation-time-factor)
                                      (int)
                                      (time/seconds)))))))

(def data
  (let [right-now (time/now)]
    (atom
      {"ETHBTC" (->> (load-data "ethbtc")
                     (map (shift-time right-now)))
       "BTCUSD" (->> (load-data "btcusd")
                     (map (shift-time right-now)))
       "ETHUSD" (->> (load-data "ethusd")
                     (map (shift-time right-now)))})))

(defn get-tick [pair]
  (-> (swap! data update pair (partial drop-while #(time/before? (% :date) (time/now))))
      (get pair)
      first))
