(ns xicotrader.private
  (:require [xicotrader.public :as public]))

(def user-secrets
  {"javier" "AEIOU"})

(def user-portfolios
  (atom {"javier" {"EUR" 5000.
                   "BTC" 1.
                   "ETH" 5.}}))

(defn get-portfolio [user-id]
  (@user-portfolios user-id))

(defn trade! [user-id operation pair qty]
  (let [portfolio (@user-portfolios user-id)
        source-currency (.substring pair 3)
        target-currency (.substring pair 0 3)
        buy? (= :buy operation)
        [source-currency target-currency] (if buy?
                                            [source-currency target-currency]
                                            [target-currency source-currency])
        last-price (:last (public/get-tick pair))
        last-price (if buy? last-price (/ 1 last-price))
        source-funds (* last-price qty)]
    (when (>= (portfolio source-currency) source-funds)
      (swap! user-portfolios update
             user-id (fn [$]
                       (-> $
                           (update target-currency #(+ qty %))
                           (update source-currency #(- % source-funds))))))))
