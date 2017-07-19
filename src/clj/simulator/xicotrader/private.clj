(ns xicotrader.private)

(def user-secrets
  {"javier" "AEIOU"})

(def user-portfolios
  {"javier" {"EUR" 1000.
             "BTC" 1.
             "ETH" 5.}})

(defn get-portfolio [user-id]
  (user-portfolios user-id))
