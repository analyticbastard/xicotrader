(ns xicotrader.public
  (require
    [ring.util.http-response :as http-response]))

(defn- get-pairs []
  ["BTCUSD" "BTCETH" "ETHUSD"])

(defn endpoint-pairs []
  (http-response/ok
    (get-pairs)))