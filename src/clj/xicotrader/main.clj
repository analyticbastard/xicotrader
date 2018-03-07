(ns xicotrader.main
  (:require
    [xicotrader
     [config :as config]
     [system :as system]])
  (:gen-class))

(def prod-key :dev)
(def prod-profile {:profile prod-key})

(defn -main
  [& args]
  (system/start-system
    (system/make-system-component
      (config/system-config prod-profile (keys (config/system-deps prod-profile))))))
