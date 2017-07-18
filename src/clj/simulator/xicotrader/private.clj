(ns xicotrader.private
  (require
    [ring.util.http-response :as http-response]))

(def user-secrets
  {"javier" "AEIOU"})

(defmacro with-validation [user-id secret-key & body]
  `(if (= (~user-secrets ~user-id) ~secret-key)
     (do ~@body)
     (http-response/forbidden)))

(def user-portfolios
  {"javier" {"EUR" 1000.
             "BTC" 1.
             "ETH" 5.}})

(defn get-portfolio [user-id]
  (user-portfolios user-id))

(defn endpoint-portfolio [user-id]
  (http-response/ok
    (get-portfolio user-id)))