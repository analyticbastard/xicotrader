(ns xicotrader.public
  (require
    [ring.util.http-response :as http-response]
    [cheshire.core :as cheshire]))

(defn get-pairs []
  ["BTCEUR" "BTCETH" "ETHEUR"])

(defn get-tick [pair]
  150.)

(defn endpoint-pairs []
  (http-response/ok
    (get-pairs)))

(defn endpoint-get-tick [pair]
  (http-response/ok
    (cheshire/encode (get-tick pair))))