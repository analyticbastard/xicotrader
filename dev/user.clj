(ns user
  (:require
    [clojure.tools.namespace.repl :refer [refresh refresh-all]]
    [com.stuartsierra.component :as component]
    [xicotrader
     [config :as config]
     [system :as system]]))

(def system nil)

(defmacro when-not-system [& body]
  `(if-not system
     (do ~@body)
     (println "System found!")))

(defmacro when-system [& body]
  `(if system
     (do ~@body)
     (println "System not found!")))

(defn- dev-config []
  (config/system-config :dev
                        (keys (system/make-system-dependencies))))

(defn make-system [config]
  (-> (system/make-system-component config)
      (component/system-using (system/make-system-dependencies))))

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
