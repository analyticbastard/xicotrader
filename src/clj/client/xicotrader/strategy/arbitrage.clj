(ns xicotrader.strategy.arbitrage
  (:require
    [clojure.set :as set]
    [clojure.walk :as walk]
    [com.stuartsierra.component :as component]
    [xicotrader.strategy :as strategy]))

(def currently-trading? (atom false))

(defn do-when-previous-action-filled [portfolio-updates]
  (println portfolio-updates)
  (when (seq portfolio-updates)
    (reset! currently-trading? false)))

(defn cycle-usd [portfolio {:keys [last-ethusd last-btcusd last-ethbtc]}]
  (-> (get-in portfolio ["USD"])
      (/ last-ethusd)
      (* last-ethbtc)
      (* last-btcusd)))

(defn cycle-eth [portfolio {:keys [last-ethusd last-btcusd last-ethbtc]}]
  (-> (get-in portfolio ["ETH"])
      (* last-ethbtc)
      (* last-btcusd)
      (/ last-ethusd)))

(defn cycle-btc [portfolio {:keys [last-ethusd last-btcusd last-ethbtc]}]
  (-> (get-in portfolio ["BTC"])
      (* last-btcusd)
      (/ last-ethusd)
      (* last-ethbtc)))

(defn get-pair-to-trade [max-usdethbtcusd max-ethbtcusdeth max-btcusdethbtc
                         {:keys [last-ethusd last-btcusd last-ethbtc]}]
  (let [pair (key (apply max-key val
                         {"ETHUSD" max-usdethbtcusd
                          "ETHBTC" (* max-ethbtcusdeth last-ethusd)
                          "BTCUSD" (* max-btcusdethbtc last-btcusd)}))]
    [pair (if (= pair "ETHUSD") :buy :sell)]))

(defn get-qty [portfolio {:keys [last-ethusd last-btcusd last-ethbtc]} what-to-buy]
  (case what-to-buy
    "ETHUSD" (/ (portfolio "USD") last-ethusd)
    "ETHBTC" (* (portfolio "ETH") last-ethbtc)
    "BTCUSD" (* (portfolio "BTC") last-btcusd)))

(defn get-source-asset [what-to-buy]
  ({"ETHUSD" "USD"
    "ETHBTC" "ETH"
    "BTCUSD" "BTC"} what-to-buy))

(defn do-arbitrage [portfolio market tick-data]
  (when (or (not @currently-trading?) (seq tick-data))
    (let [market (-> (set/rename-keys market {"ETHUSD" "last-ethusd"
                                              "ETHBTC" "last-ethbtc"
                                              "BTCUSD" "last-btcusd"})
                     (walk/keywordize-keys)
                     (select-keys [:last-ethusd :last-ethbtc :last-btcusd]))
          all-exist? (fn [[last-ethusd last-btcusd last-ethbtc]]
                       (and last-ethusd last-btcusd last-ethbtc))]
      (when (all-exist? (vals market))
        (let [[max-usdethbtcusd
               max-ethbtcusdeth
               max-btcusdethbtc] ((juxt (partial cycle-usd portfolio)
                                        (partial cycle-eth portfolio)
                                        (partial cycle-btc portfolio)) market)
              [what-to-trade
               operation] (get-pair-to-trade max-usdethbtcusd max-ethbtcusdeth
                                             max-btcusdethbtc market)
              qty (get-qty portfolio market what-to-trade)
              what-to-spend (get-source-asset what-to-trade)]
          (reset! currently-trading? true)
          {:operation operation
           :pair      what-to-trade
           :qty       qty})))))

(defrecord Component [config]
  component/Lifecycle
  (start [this]
    this)
  (stop [this]
    this)

  strategy/Strategy
  (compute [strategy portfolio portfolio-updates market tick-data]
    (do-when-previous-action-filled portfolio-updates)
    (do-arbitrage portfolio market tick-data)))

(defn new [config]
  (Component. config))
