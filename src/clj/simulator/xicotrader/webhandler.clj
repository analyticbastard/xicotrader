(ns xicotrader.webhandler
  (:require
    [clojure.string :as string]
    [clojure.java.io :as clj-io]
    [clojure.tools.logging :as log]
    [com.stuartsierra.component :as component]
    [ring.util.http-response :as http-response]
    [compojure.api.sweet :as compojure-api
     :refer [context defroutes GET PATCH POST PUT DELETE]]
    [ring.middleware
     [params :as params]]
    [modular.ring :refer [WebRequestHandler]]
    [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
    [schema.core :as s]
    [cheshire.core :as cheshire]
    [xicotrader
     [public :as public]
     [private :as private]]))

(defmacro with-validation [user-id secret-key & body]
  `(if (= (~private/user-secrets ~user-id) ~secret-key)
     (do ~@body)
     (http-response/forbidden)))

(defmulti handler (fn [e] (type e)))

(defmethod handler Exception [e]
  (http-response/internal-server-error))

(defn public-endpoint-pairs []
  (http-response/ok
    (public/get-pairs)))

(defn public-endpoint-get-tick [pair]
  (http-response/ok
    (cheshire/encode (public/get-tick pair))))

(defn private-endpoint-portfolio [user-id]
  (http-response/ok
    (private/get-portfolio user-id)))

(compojure-api/defapi xicotrader-api
  {:swagger
   {:ui "/sw"
    :spec "/sw/docs/swagger.json"
    :data {:info {:title "Xicotrader Simulator"}}}
   :exceptions
   {:handlers {::compojure.api.exception/default #(handler %)}}}
  (context "/api/public" []
    :tags ["Public API"]
    (GET "/pairs" []
      :summary "Get consumer files"
      (public-endpoint-pairs))
    (GET "/tick" []
      :summary "Get last tick for a trading pair"
      :query-params [pair :- (apply s/enum (public/get-pairs))]
      (public-endpoint-get-tick pair)))
  (context "/api/private" []
    :tags ["Private API"]
    (GET "/portfolio" []
      :summary "Get user's current portfolio"
      :query-params [user-id :- s/Str
                     secret-key :- s/Str]
      (with-validation user-id secret-key
        (private-endpoint-portfolio user-id)))))

(defroutes app xicotrader-api)

(defn- reloadably-attach-things [h cmpnt req]
  (h (merge req cmpnt {:consumer-input-io (-> cmpnt :consumer :task :input-io)})))

(defn- wrap-request-with-component
  "Merge all the state in the component into the request for easy access."
  [handler cmpnt]
  (fn [req]
    (reloadably-attach-things handler cmpnt req)))

(defn- build-request-handler [this]
  (-> ;;api
    app
    (wrap-request-with-component this)
    wrap-json-response
    (params/wrap-params)))

(defrecord WebHandler []
  WebRequestHandler
  (request-handler [this]
    (build-request-handler this)))

(defn new []
  (WebHandler.))
