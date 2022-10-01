(ns jsonapi-xtdb.core
  (:require [xtdb.api :as xt]
            [hato.client :as hc]
            [clojure.java.io :as io]
            [cheshire.core :refer [generate-string]]
            [nextjournal.clerk :as clerk]))


;; started by making api application with challonge
;; https://connect.challonge.com/challonge/apps

(+ 3 3)

^{::clerk/visibility {:code :show :result :hide}}
(def secrets (clojure.edn/read-string (slurp "token.edn")))

(defn api-url [end] (str "https://api.challonge.com/v2/" end))

(defn request [{:keys [method url body] :as req}]
  (let [additional-opts {:headers {"Authorization-Type" "v2"}
                         :oauth-token (:access_token secrets)
                         :content-type "application/vnd.api+json"
                         :accept :json
                         :as :json}]
    (:body (hc/request (merge additional-opts
                              req
                              {:body (if (and body (map? body))
                                       (generate-string body)
                                       body)})))))

(def list-tournaments-empty (request {:method :get
                                      :url (api-url "tournaments.json")}))
;>>
{:data [],
 :included [],
 :meta {:count 0},
 :links {:self "https://api.challonge.com/v2/tournaments.json",
         :next "https://api.challonge.com/v2/tournaments.json?page=2&per_page=20",
         :prev "https://api.challonge.com/v2/tournaments.json?page=0&per_page=20"}}

(def created-tournament
  (request {:method :post
            :url (api-url "tournaments.json")
            :body {:data {:type "tournaments"
                          :attributes
                          {:name "best color"
                           :tournament_type "single elimination"}}}}))
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

(def list-tournaments (request {:method :get
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

;; ok cool. lots of nested data, i like the structure and how it includes related resources in a consistent manner
;; lets make a full tournament to get more data

(-> list-tournaments
    :data
    first
    :relationships
    :participants)

(def participant-url (-> list-tournaments
                         :data
                         first
                         :relationships
                         :participants
                         :links
                         :related))

(request {:method :get
          :url participant-url})

;; ok, it really is empty. like the (-> tourney :relationships :participants :meta :count)

(doseq [color ["red" "green" "blue" "orange" "maroon" "coral" "crimson" "scarlet" "pink"
               "rust" "salmon"]]
  (request {:method :post
            :url participant-url
            :body
            {:data
             {:type "Participant"
              :attributes
              {:name color}}}}))

;; see 




(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

