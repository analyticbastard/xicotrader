(ns xicotrader.public)

(defn- get-pairs []
  ["BTCUSD" "BTCETH" "ETHUSD"])

(defn endpoint-pairs []
  (get-pairs))