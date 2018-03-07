(ns xicotrader.schema-test
  (:require
    [clojure.test :refer :all]
    [clj-time.core :as t]
    [schema.core :as s]
    [xicotrader.schema :refer :all]))

(deftest simple-schemas-test
  (testing "Simple schemas"
    (is (nil? (s/check Pos 0.1)))
    (is (s/check Pos -0.1))))

(deftest tick-test
  (let [coin1 {:name "Bitcoin" :ticker "BTC"}
        coin2 {:name "Ethereum" :ticker "ETH"}
        pair {:num coin2 :den coin1}
        ohcl {:open 1 :high 2 :low 0 :close 1}
        last {:last 1}
        date {:time (t/now)}
        ticker-ohcl (merge pair date ohcl)
        ticker-last (merge pair date last)
        ticker-both (merge pair date last ohcl)]
    (testing "Test ticker is OHCL and not Last"
      (is (nil? (s/check Tick ticker-ohcl))))
    (testing "Test ticker is not OHCL but Last"
      (is (nil? (s/check Tick ticker-last))))
    (testing "Test ticker cannot have OHCL and Last data at the same time"
      (is (s/check Tick ticker-both)))))


