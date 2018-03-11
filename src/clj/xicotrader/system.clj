(ns xicotrader.system
  (:require
    [com.stuartsierra.component :as component]
    [xicotrader
     [controller :as controller]
     [receiver :as recv]
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
        strategy-jar (get-in system-map [:strategy :strategy])
        strategy-component (loader/load-strategy config system-map strategy-jar)
        service-key (get-in system-map [:receiver :service])
        service-component (loader/load-service config system-map service-key)]
    (if (and strategy-jar strategy-component
             service-key service-component)
      (component/system-map
        :controller (controller/new engine)
        :receiver (recv/new events)
        :strategy (strategy/new strategy)
        service-key service-component
        strategy-jar strategy-component)
      (handle-init-fail service-key strategy-jar service-component strategy-component))))

(defn start-system [system]
  (component/start system))

(defn stop-system [system]
  (component/stop system))
