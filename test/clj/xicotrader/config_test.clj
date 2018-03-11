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
  (compute [this event]))

(defn fake-strategy-loader [config system-map strategy-jar]
  (->Component config))

(deftest read-system-test
  (testing "Read system dependencies"
    (is (= (system-deps user/dev-profile)
           {:controller {:receiver  :receiver
                         :sender    :sender
                         :strategy  :strategy}
            :strategy   {:strategy "arbitrage.jar"}
            :receiver   {}
            :sender     {:service :simulator}
            :simulator  {}}))))

(deftest read-config-test
  (testing "Read events module config"
    (is (= (get-config user/dev-profile :receiver)
           {:trade ["ETHBTC" "ETHUSD" "BTCUSD"]}))))

(deftest system-config-test
  (testing "Read all modules config"
    (with-redefs [xicotrader.loader/load-strategy fake-strategy-loader]
      (is (= (system-config (system-deps user/dev-profile) user/dev-profile)
             {:controller    (get-config user/dev-profile :controller)
              :strategy  (get-config user/dev-profile :strategy)
              :receiver    (get-config user/dev-profile :receiver)
              :sender    (get-config user/dev-profile :receiver)
              :simulator (get-config user/dev-profile :simulator)})))))
