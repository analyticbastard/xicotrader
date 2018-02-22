(ns xicotrader.arbitrage.util
  (:gen-class))

(defn print-trade [buy? target-currency source-currency qty source-funds last-price]
  (println (if buy? "buy " "sell")
           (if buy? target-currency source-currency)
           (format "%.4f" (if buy? qty source-funds))
           (format "at %.4f" (double (if buy? last-price (/ 1 last-price))))
           (if buy? "with" "for")
           (if buy? source-currency target-currency)
           (format "%.4f" (if buy? source-funds qty))))