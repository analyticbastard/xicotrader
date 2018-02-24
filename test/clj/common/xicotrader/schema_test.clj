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
  (testing "Test ticker is OHCL and not Last"
    (let [coin {:name "Bitcoin" :ticker "BTC"}
          ohcl {:open 1 :high 2 :low 0 :close 1}
          last {:last 1}
          date {:time (t/now)}
          ticker-ohcl (merge coin date ohcl)
          ticker-last (merge coin date last)]
      (is (nil? (s/check Tick ticker-ohcl)))
      (is (nil? (s/check Tick ticker-last))))))


