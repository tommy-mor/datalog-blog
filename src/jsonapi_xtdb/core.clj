(ns jsonapi-xtdb.core
  (:require [xtdb.api :as xt]
            [hato.client :as hc]
            [clojure.java.io :as io]))


;; started by making api application with challonge
;; https://connect.challonge.com/challonge/apps

(def secrets (clojure.edn/read-string (slurp "token.edn")))

(defn api-url [end] (str "https://api.challonge.com/v2/" end))

(defn request [{:keys [method url] :as req}]
  (let [additional-opts {:headers {"Authorization-Type" "v2"}
                         :oauth-token (:access_token secrets)
                         :content-type "application/vnd.api+json"
                         :accept :json
                         :as :json}]
    (:body (hc/request (merge additional-opts req)))))

(request {:method :get
          :url (api-url "me.json")})

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

