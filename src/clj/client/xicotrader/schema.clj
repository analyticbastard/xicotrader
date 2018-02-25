(ns xicotrader.schema
  (:require
    [schema.core :as s :refer [defschema optional-key]]
    [clj-time.core :as t]
    [medley.core :as m])
  (:import (org.joda.time DateTime)))

(defn constrain-ticker-either-ohcl-or-last [m]
  (not (and (seq (select-keys m [:open :close :high :low]))
            (:last m))))

(defschema Pos
  (s/constrained s/Num (comp not neg?)))

(defschema Time
  {:time DateTime})

(defschema OHCL
  {:open  Pos
   :close Pos
   :high  Pos
   :low   Pos})

(defschema Last
  {:last Pos})

(defschema Price
  {:price Pos})

(defschema Volume
  {:volume Pos})

(defschema MarketCap
  {:marketcap Pos})

(defschema Supply
  {:supply Pos})

(defschema Coin
  {:name s/Str
   :ticker s/Str})

(defschema Pair
  {:num Coin
   :den Coin})

(defschema Tick
  (-> (merge
        Time
        Coin
        (m/map-keys optional-key OHCL)
        (m/map-keys optional-key Last)
        (m/map-keys optional-key Volume)
        (m/map-keys optional-key MarketCap)
        (m/map-keys optional-key Supply))
      (s/constrained constrain-ticker-either-ohcl-or-last)))

(defschema Holdings
  (merge Coin
         {:holdings Pos}))

(defschema Portfolio
  {:portfolio [Holdings]})

(defschema Operation
  {:operation (s/enum [:buy :sell :cancel])
   :type (s/enum :market :limit)})

(defschema Amount
  {:qty Pos})

(defschema ValidUntil
  {:valid-until DateTime})

(defschema Order
  (merge
    Operation
    Pair
    Amount
    (m/map-keys optional-key Price)
    (m/map-keys optional-key ValidUntil)))

(defschema OutputDataFeedService
  (merge
    (m/map-keys optional-key Portfolio)
    {(optional-key :ticker) Tick}
    {(optional-key :orders) [Order]}))

(defschema InputStrategyService
  OutputDataFeedService)

(defschema OutputStrategyService
  Order)
