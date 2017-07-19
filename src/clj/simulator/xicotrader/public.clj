(ns xicotrader.public
  (require
    [clj-time.core :as time]
    [ring.util.http-response :as http-response]
    [cheshire.core :as cheshire]))

(defn get-pairs []
  ["BTCEUR" "BTCETH" "ETHEUR"])

(defn- generate-data [current-time]
  (let [next-time (time/plus current-time (time/seconds 5))
        last (+ 150. (* 10 (Math/random)))]
    (lazy-seq (cons {:last last :time current-time} (generate-data next-time)))))

(def data {"ETHEUR" (generate-data (time/now))})

(defn get-tick [pair]
  (->> (get data pair)
       (filter #(time/after? (:time %) (time/now)))
       (take 1)))

(defn endpoint-pairs []
  (http-response/ok
    (get-pairs)))

(defn endpoint-get-tick [pair]
  (http-response/ok
    (cheshire/encode (get-tick pair))))
