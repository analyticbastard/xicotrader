(ns xicotrader.strategy.arbitrage
  (:require
    [com.stuartsierra.component :as component]
    [xicotrader.strategy :as strategy]))

(defn do-arbitrage [portfolio market tick-data]
  (let [last-etheur (get-in market ["ETHEUR"])
        last-btceur (get-in market ["BTCEUR"])
        last-ethbtc (get-in market ["ETHBTC"])]
    (when (and last-etheur last-ethbtc last-btceur)
      (let [max-eurethbtceur (-> (get-in portfolio ["EUR"])
                                 (/ last-etheur)
                                 (* last-ethbtc)
                                 (* last-btceur))
            max-ethbtceureth (-> (get-in portfolio ["ETH"])
                                 (* last-ethbtc)
                                 (* last-btceur)
                                 (/ last-etheur))
            max-btceurethbtc (-> (get-in portfolio ["BTC"])
                                 (* last-btceur)
                                 (/ last-etheur)
                                 (* last-ethbtc))
            what-to-buy (key (apply max-key val
                                    {"ETHEUR" max-eurethbtceur
                                     "ETHBTC" (* max-ethbtceureth last-etheur)
                                     "BTCEUR" (* max-btceurethbtc last-btceur)}))
            what-to-spend ({"ETHEUR" "EUR"
                            "ETHBTC" "ETH"
                            "BTCEUR" "BTC"} what-to-buy)]
        {:buy what-to-buy
         :qty (portfolio what-to-spend)}))))

(defrecord Component [config]
  component/Lifecycle
  (start [this]
    this)
  (stop [this]
    this)

  strategy/Strategy
  (compute [strategy portfolio market tick-data]
    (do-arbitrage portfolio market tick-data)))

(defn new [config]
  (Component. config))
