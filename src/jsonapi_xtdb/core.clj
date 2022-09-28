(ns jsonapi-xtdb.core
  (:require [xtdb.api :as xt]
            [hato.client :as hc]
            [clojure.java.io :as io]
            [cheshire.core :refer [generate-string]]))


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

(def empty-request (request {:method :get
                             :url (api-url "tournaments.json")}))
;>>
{:data [],
 :included [],
 :meta {:count 0},
 :links {:self "https://api.challonge.com/v2/tournaments.json",
         :next "https://api.challonge.com/v2/tournaments.json?page=2&per_page=20",
         :prev "https://api.challonge.com/v2/tournaments.json?page=0&per_page=20"}}

(def r (request {:method :post
                 :url (api-url "tournaments.json")
                 :body (generate-string {:data {:type "tournaments"
                                                :attributes
                                                {:name "best color"
                                                 :tournament_type "single elimination"}}})}))
;>>
{:data
 {:id "sggdlmtq",
  :type "tournament",
  :attributes
  {:signUpUrl nil,
   :description "",
   :oauthApplicationId 227,
   :liveImageUrl "https://challonge.com/sggdlmtq.svg",
   :thirdPlaceMatch false,
   :private false,
   :notifyUponMatchesOpen true,
   :sequentialPairings false,
   :name "best color",
   :autoAssignStations nil,
   :gameName nil,
   :state "pending",
   :hideSeeds false,
   :timestamps
   {:startsAt nil,
    :startedAt nil,
    :createdAt "2022-09-28T21:48:08.474Z",
    :updatedAt "2022-09-28T21:48:08.474Z",
    :completedAt nil},
   :notifyUponTournamentEnds true,
   :tournamentType "Single Elimination",
   :url "sggdlmtq",
   :acceptAttachments false,
   :checkInDuration nil,
   :fullChallongeUrl "https://challonge.com/sggdlmtq",
   :signupCap nil,
   :onlyStartMatchesWithStations nil,
   :openSignup false},
  :relationships
  {:petition {},
   :stations
   {:links
    {:related
     "https://api.challonge.com/v2/tournaments/sggdlmtq/stations.json",
     :meta {:count 0}}},
   :community {},
   :series {},
   :game {},
   :participants
   {:links
    {:related
     "https://api.challonge.com/v2/tournaments/sggdlmtq/participants.json",
     :meta {:count 0}}},
   :matches
   {:links
    {:related
     "https://api.challonge.com/v2/tournaments/sggdlmtq/matches.json",
     :meta {:count 0}}},
   :localizedContents {},
   :organizer {}},
  :links
  {:self "https://api.challonge.com/v2/tournaments/sggdlmtq.json"}}}

(def empty-request (request {:method :get
                             :url (api-url "tournaments.json")}))
;>>
{:data
 [{:id "sggdlmtq",
   :type "tournament",
   :attributes
   {:signUpUrl nil,
    :description "",
    :oauthApplicationId 227,
    :liveImageUrl "https://challonge.com/sggdlmtq.svg",
    :thirdPlaceMatch false,
    :private false,
    :notifyUponMatchesOpen true,
    :sequentialPairings false,
    :name "best color",
    :autoAssignStations nil,
    :gameName nil,
    :state "pending",
    :hideSeeds false,
    :timestamps
    {:startsAt nil,
     :startedAt nil,
     :createdAt "2022-09-28T21:48:08.474Z",
     :updatedAt "2022-09-28T21:48:08.474Z",
     :completedAt nil},
    :notifyUponTournamentEnds true,
    :tournamentType "Single Elimination",
    :url "sggdlmtq",
    :acceptAttachments false,
    :checkInDuration nil,
    :fullChallongeUrl "https://challonge.com/sggdlmtq",
    :signupCap nil,
    :onlyStartMatchesWithStations nil,
    :openSignup false},
   :relationships
   {:petition {},
    :stations
    {:links
     {:related
      "https://api.challonge.com/v2/tournaments/sggdlmtq/stations.json",
      :meta {:count 0}}},
    :community {:data nil},
    :series {},
    :game {:data nil},
    :participants
    {:links
     {:related
      "https://api.challonge.com/v2/tournaments/sggdlmtq/participants.json",
      :meta {:count 0}}},
    :matches
    {:links
     {:related
      "https://api.challonge.com/v2/tournaments/sggdlmtq/matches.json",
      :meta {:count 0}}},
    :localizedContents {},
    :organizer {:data {:id "5663543", :type "user"}}},
   :links
   {:self "https://api.challonge.com/v2/tournaments/sggdlmtq.json"}}],
 :included
 [{:id "5663543",
   :type "user",
   :attributes
   {:username "tommylt3",
    :imageUrl
    "https://s3.amazonaws.com/challonge_app/misc/challonge_fireball_gray.png"}}],
 :meta {:count 1},
 :links
 {:self "https://api.challonge.com/v2/tournaments.json",
  :next
  "https://api.challonge.com/v2/tournaments.json?page=2&per_page=20",
  :prev
  "https://api.challonge.com/v2/tournaments.json?page=0&per_page=20"}}



(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

