(ns xicotrader.config
  (:require
    [clojure.java.io :as io])
  (:import (java.util Properties)
           (java.io Reader)))

(defn load-props
  [file-name]
  (let [props (Properties.)
        file-resource (io/resource file-name)]
    (with-open [^Reader reader (io/reader file-resource)]
      (.load props reader)
      (into {} (for [[k v] props]
                 [(keyword k) (read-string v)])))))

(defn config [profile module]
  (load-props (format "conf/%s/%s.properties" (name profile) (name module))))

(defn system-config [profile component-names]
  (into {} (map #(do [%1 (config profile %1)])
                component-names)))
