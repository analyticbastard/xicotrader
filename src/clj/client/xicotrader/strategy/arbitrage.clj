(ns xicotrader.strategy.arbitrage
  (:require
    [clojure.set :as set]
    [clojure.walk :as walk]
    [com.stuartsierra.component :as component]
    [xicotrader.strategy :as strategy]))

(def currently-traded-asset (atom nil))

(defn do-when-previous-action-filled [portfolio-updates]
  )

(defn cycle-eur [portfolio {:keys [last-etheur last-btceur last-ethbtc]}]
  (-> (get-in portfolio ["EUR"])
      (/ last-etheur)
      (* last-ethbtc)
      (* last-btceur)))

(defn cycle-eth [portfolio {:keys [last-etheur last-btceur last-ethbtc]}]
  (-> (get-in portfolio ["ETH"])
      (* last-ethbtc)
      (* last-btceur)
      (/ last-etheur)))

(defn cycle-btc [portfolio {:keys [last-etheur last-btceur last-ethbtc]}]
  (-> (get-in portfolio ["BTC"])
      (* last-btceur)
      (/ last-etheur)
      (* last-ethbtc)))

(defn get-pair-to-trade [max-eurethbtceur max-ethbtceureth max-btceurethbtc
                         {:keys [last-etheur last-btceur last-ethbtc]}]
  (let [pair (key (apply max-key val
                         {"ETHEUR" max-eurethbtceur
                          "ETHBTC" (* max-ethbtceureth last-etheur)
                          "BTCEUR" (* max-btceurethbtc last-btceur)}))]
    [pair (if (= pair "ETHEUR") :buy :sell)]))

(defn get-qty [portfolio {:keys [last-etheur last-btceur last-ethbtc]} what-to-buy]
  (case what-to-buy
    "ETHEUR" (/ (portfolio "EUR") last-etheur)
    "ETHBTC" (* (portfolio "ETH") last-ethbtc)
    "BTCEUR" (* (portfolio "BTC") last-btceur)))

(defn get-source-asset [what-to-buy]
  ({"ETHEUR" "EUR"
    "ETHBTC" "ETH"
    "BTCEUR" "BTC"} what-to-buy))

(defn do-arbitrage [portfolio market {:keys [portfolio-updates]}]
  (let [market (-> (set/rename-keys market {"ETHEUR" "last-etheur"
                                            "ETHBTC" "last-ethbtc"
                                            "BTCEUR" "last-btceur"})
                   (walk/keywordize-keys)
                   (select-keys [:last-etheur :last-ethbtc :last-btceur]))
        all-exist? (fn [[last-etheur last-btceur last-ethbtc]]
                     (and last-etheur last-btceur last-ethbtc))]
    (when (all-exist? (vals market))
      (let [[max-eurethbtceur
             max-ethbtceureth
             max-btceurethbtc] ((juxt (partial cycle-eur portfolio)
                                      (partial cycle-eth portfolio)
                                      (partial cycle-btc portfolio)) market)
            [what-to-trade
             operation] (get-pair-to-trade max-eurethbtceur max-ethbtceureth
                                           max-btceurethbtc market)
            qty (get-qty portfolio market what-to-trade)
            what-to-spend (get-source-asset what-to-trade)]
        {:operation operation
         :pair      what-to-trade
         :qty       qty}))))

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
