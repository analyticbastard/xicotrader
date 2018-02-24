(ns xicotrader.config-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [com.stuartsierra.component :as component]
    [xicotrader.config :refer :all]
    [xicotrader.strategy :as strategy]))

(defrecord Component [config]
  component/Lifecycle
  (start [this] this)
  (stop [this] this)

  xicotrader.strategy/Strategy
  (compute [this strategy portfolio tick-data]))

(defn fake-strategy-loader [config system-map strategy-jar]
  (->Component config))

(deftest read-system-test
  (testing "Read system dependencies"
    (is (= (system-deps user/dev-profile)
           {:engine    {:events   :events
                        :strategy :strategy}
            :strategy  {:strategy "arbitrage.jar"}
            :events    {:service :simulator}
            :simulator {}}))))

(deftest read-config-test
  (testing "Read events module config"
    (is (= (get-config user/dev-profile :events)
           {:trade ["ETHBTC" "ETHUSD" "BTCUSD"]}))))

(deftest system-config-test
  (testing "Read all modules config"
    (with-redefs [xicotrader.loader/load-strategy fake-strategy-loader]
      (is (= (system-config (system-deps user/dev-profile) user/dev-profile)
             {:engine    (get-config user/dev-profile :engine)
              :strategy  (get-config user/dev-profile :strategy)
              :events    (get-config user/dev-profile :events)
              :simulator (get-config user/dev-profile :simulator)})))))
