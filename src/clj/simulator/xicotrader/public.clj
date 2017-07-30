(ns xicotrader.public
  (require
    [clj-time.core :as time]))

(defn get-pairs []
  ["BTCEUR" "ETHBTC" "ETHEUR"])

(defn- generate-data [current-time base-price max-price-reach]
  (let [next-time (time/plus current-time (time/seconds 5))
        base-price (double base-price)
        max-price-reach (double max-price-reach)
        last (+ base-price (* max-price-reach (Math/random)))]
    (lazy-seq (cons {:last last :time current-time} (generate-data next-time base-price max-price-reach)))))

(def data {"ETHEUR" (generate-data (time/now) 200 10)
           "BTCEUR" (generate-data (time/now) 2000 200)
           "ETHBTC" (generate-data (time/now) 0.1 0.01)})

(defn get-tick [pair]
  (->> (get data pair)
       (filter #(time/after? (:time %) (time/now)))
       first))
