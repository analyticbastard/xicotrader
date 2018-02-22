(ns user
  (:require
    [clojure.tools.namespace.repl :refer [refresh refresh-all]]
    [com.stuartsierra.component :as component]
    [xicotrader
     [config :as config]
     [system :as system]]))

(def system nil)

(def dev-key :dev)
(def prod-key :dev)

(def dev-profile {:profile dev-key})
(def prod-profile {:profile dev-key})

(defmacro when-not-system [& body]
  `(if-not system
     (do ~@body)
     (println "System found!")))

(defmacro when-system [& body]
  `(if system
     (do ~@body)
     (println "System not found!")))

(defn- dev-config []
  (merge dev-profile
         (config/system-config
           (config/system-deps dev-profile)
           dev-profile)))

(defn make-system
  "Makes dev system, assocs the :dev to the profile in config"
  [config]
  (-> (system/make-system-component config)
      (component/system-using
        (config/system-deps (select-keys config [:profile])))))

(defn start
  ([] (start (dev-config)))
  ([config]
   (when-not-system
     (let [s (make-system (merge (dev-config) config))]
       (alter-var-root #'user/system (constantly s))
       (alter-var-root #'user/system system/start-system)
       (println "System started!")))))

(defn stop []
  (when-system
    (alter-var-root #'user/system system/stop-system)
    (alter-var-root #'user/system (constantly nil))
    (println "System stopped!")))

(defn restart
  ([]
   (restart (dev-config)))
  ([config]
   (stop)
   (start config)))
