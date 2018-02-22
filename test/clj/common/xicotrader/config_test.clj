(ns xicotrader.config-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [xicotrader.config :refer :all]))

(deftest read-system-test
  (testing "Read system dependencies"
    (is (= (system-deps user/dev-profile)
           {:engine    {:events   :events
                        :strategy :strategy}
            :strategy  {:strategy :arbitrage}
            :events    {:service :simulator}
            :simulator {}
            :arbitrage {}}))))

(deftest read-config-test
  (testing "Read events module config"
    (is (= (get-config user/dev-profile :events)
           {:trade ["ETHBTC" "ETHUSD" "BTCUSD"]}))))

(deftest system-config-test
  (testing "Read all modules config"
    (is (= (system-config (system-deps user/dev-profile) user/dev-profile)
           {:engine (get-config user/dev-profile :engine)
            :strategy (get-config user/dev-profile :strategy)
            :events (get-config user/dev-profile :events)
            :simulator (get-config user/dev-profile :simulator)
            :arbitrage (get-config user/dev-profile :arbitrage)}))))
