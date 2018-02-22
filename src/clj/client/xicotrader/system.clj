(ns xicotrader.system
  (:require
    [com.stuartsierra.component :as component]
    [xicotrader
     [engine :as engine]
     [events :as events]
     [strategy :as strategy]]
    [xicotrader.loader :as loader]
    [xicotrader.config :as config]
    [xicotrader.service :as service]))

(defn handle-init-fail [service-key strategy-key service-component strategy-component]
  (let [found-keys {"service-key" service-key
                    "strategy-key" strategy-key}
        found-components {"service-component" service-component
                          "strategy-component" strategy-component}]
    (throw (Exception. (str "Failed to start system"
                            found-keys
                            found-components)))))

(defn make-system-component [{:keys [engine events strategy] :as config}]
  (let [system-map (config/system-deps config)
        strategy-key (get-in system-map [:strategy :strategy])
        strategy-component (loader/load-strategy config system-map strategy-key)
        service-key (get-in system-map [:events :service])
        service-component (loader/load-service config system-map service-key)]
    (if (and strategy-key strategy-component
             service-key service-component)
      (component/system-map
        :engine (engine/new engine)
        :events (events/new events)
        :strategy (strategy/new strategy)
        service-key service-component
        strategy-key strategy-component)
      (handle-init-fail service-key strategy-key service-component strategy-component))))

(defn start-system [system]
  (component/start system))

(defn stop-system [system]
  (component/stop system))
