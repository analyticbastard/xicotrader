(ns xicotrader.system
  (:require
    [clojure.core.async :as a]
    [com.stuartsierra.component :as component]
    [xicotrader
     [webhandler :as webhandler]
     [webserver :as webserver]]))

(defn- create-webserver [config]
  (assoc (webserver/new config) :ch (a/chan)))

(defn make-system-component
  [{:keys [webserver] :as config}]
  (component/system-map
    :webserver     (create-webserver webserver)
    :webhandler    (webhandler/new)))

(defn make-system-dependencies []
  {:webserver     {:handler :webhandler}})

(defn start-system [system]
  (component/start system))

(defn stop-system [system]
  (component/stop system))