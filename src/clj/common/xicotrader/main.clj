(ns xicotrader.main
  (:require
    [xicotrader
     [config :as config]
     [system :as system]])
  (:gen-class))

(defn -main
  [& args]
  (system/start-system
    (system/make-system-component
      (config/system-config :prod (keys (system/make-system-dependencies))))))
