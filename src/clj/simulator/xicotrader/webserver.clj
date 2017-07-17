(ns xicotrader.webserver
  (:require [com.stuartsierra.component :as component]
            [modular.http-kit :refer [new-webserver]]))

(defn new [config]
  (apply new-webserver (flatten (into [] config))))
