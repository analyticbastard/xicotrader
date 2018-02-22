(ns xicotrader.config
  (:require
    [clojure.java.io :as io]
    [clojure.edn :as edn]
    [xicotrader.util :refer [ignoring-exceptions]])
  (:import (java.util Properties)
           (java.io Reader)))

(defn config-from-env [name default]
  [name (or (System/getenv name) default)])

(defn get-keys [config]
  (let [config-keys (dissoc config :profile)]
    (concat (keys config-keys))))

(defn load-props [file-name]
  (let [file-resource (io/resource file-name)]
    (or (when file-resource
          (try
            (edn/read-string (slurp file-resource))
            (catch Exception e
              (println (.getMessage e)))))
        {})))

(defn get-config [{:keys [profile]} module]
  (or (load-props (format "conf/%s/%s.edn" (name profile) (name module)))
      {}))

(defn system-deps [profile-map]
  (get-config profile-map :system))

(defn system-config [deps profile-map]
  (let [component-names (get-keys deps)]
    (into {} (map #(do
                     [%1 (get-config profile-map %1)]) component-names))))
