^{::clerk/visibility {:code :hide :result :hide}}
(ns jsonapi-xtdb.core
  (:require [xtdb.api :as xt]
            [hato.client :as hc]
            [clojure.java.io :as io]
            [cheshire.core :refer [generate-string]]
            [nextjournal.clerk :as clerk]
            [jsonapi-xtdb.auth :refer [refresh-tokens]]
            [clojure.set]
            [clojure.instant])
  (:import [javax.imageio ImageIO]))


^{::clerk/visibility {:code :hide :result :hide}}
(comment
  ^{::clerk/visibility {:code :show :result :hide}}
  (def tokens (refresh-tokens))


  (defn api-url [end] (str "https://api.challonge.com/v2/" end))

  (defn request [{:keys [method url body] :as req}]
    (let [additional-opts {:headers {"Authorization-Type" "v2"}
                           :oauth-token (:access_token tokens)
                           :content-type "application/vnd.api+json"
                           :accept :json
                           :as :json}]
      (:body (hc/request (merge additional-opts
                                req
                                {:body (if (and body (map? body))
                                         (generate-string body)
                                         body)})))))

  ^{::clerk/visibility {:code :hide :result :hide}}
  (comment (doall (for [tourney (:data (request {:method :get
                                                 :url (api-url "tournaments.json")}))]
                    (request {:method :delete
                              :url (-> tourney :links :self)}))))

  (def list-tournaments-empty (request {:method :get
                                        :url (api-url "tournaments.json")}))
  

  (def created-tournament
    (request {:method :post
              :url (api-url "tournaments.json")
              :body {:data {:type "tournaments"
                            :attributes
                            {:name "best color"
                             :tournament_type "single elimination"}}}}))

  (def list-tournaments (request {:method :get
                                  :url (api-url "tournaments.json")}))

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
  

  (def participants (request {:method :get
                              :url participant-url}))
  ;; ok, it really is empty. like the (-> tourney :relationships :participants :meta :count)

  (doseq [color ["red" "green" "blue" "orange" "maroon" "coral" "crimson" "scarlet" "pink"
                 "rust" "salmon"]]
    (request {:method :post
              :url participant-url
              :body
              {:data
               {:type "Participant"
                :attributes
                {:name color
                 :misc (str (java.util.UUID/randomUUID))}}}}))

  (def participants (request {:method :get
                              :url participant-url}))
  

  participants

  (def matches (request {:method :get
                         :url (-> list-tournaments
                                  :data
                                  first
                                  :relationships
                                  :matches
                                  :links
                                  :related)}))
  (spit "matches.edn" matches))

^{::clerk/visibility {:code :hide :result :show}}
(clerk/md "# Datalog for json munging")



;; Hello all, my name is tommy and I love clojure. You can find me on [github](https://github.com/tommy-mor) or [email me](mailto:thmorriss@gmail.com). This article was created with [clerk](https://github.com/nextjournal/clerk).
;; 
;; I am working on a gameserver plugin for my favorite fighting game [MGE](https://wiki.teamfortress.com/wiki/MGE_Mod), and want to be able able to run tournaments automatically on a weekly basis, similar to how many melee groups run low-stakes weekly tournaments. 

;; Thankfully, the company [challonge](https://challonge.com) (owned now by logitech) hosts software that lets you create and run your own tournaments. It displays the bracket, handles tournament state, player signups, multistage tournaments (swiss/round-robin/group-stage), winners/losers brackets, and much more. It also lets you embed the (realtime updating) bracket as an iframe.

;; It also has an API. So lets get started. Below is an example tournament that I've created. Its to decide which color is the best color. A couple matches have already been played (apparently crimson is favorable over rust by 6 points...)

^{::clerk/visibility {:code :hide :result :show}}
(ImageIO/read (io/file "bracket.png"))

;; Below is the parsed json we get back from the challonge api after asking for all the matches of the pictured tournament.
;; Challonge shapes its responses according to a standard called [json-api](https://jsonapi.org/). Json-api describes itself as something to stop bikeshedding questions about how json responses should be formatted.

;; Its uniformity somewhat enables the rest of what happens in this post.


^{::clerk/opts {:auto-expand-results? true}}
(def matches (clojure.edn/read-string (slurp "matches.edn")))
;; I asked for matches, I got very complicated piece of data.
;; The `:data` key has an array of 10 resources, each with a type, id, some attributes, and some relationships.

;; For a each match in the tournament, its relationships are the two opponents facing off in that match. However, the participant nested in the match `(get-in matches [:data 0 :relationships :player1])` has very little information. If you want to see any `attributes` of a player, you must look in the toplevel `:included` vector and find the right one.

;; The `:included` vector contains all 12 participants (colors) in this tournament.

;; Also notice that each participant (in the `:included` vector) has a `:misc` attribute. This is any small piece of data you want, and in my case it is the gameserver ID of the player. This is domain specific to my code.

;; This is a fairly complex nested structure, but it's well thought out, and handles many edge cases. I personally would prefer to expose my data using something like [pathom3](https://github.com/wilkerlucio/pathom3), but I prefer the standardized json-api approach over having arbitrary shapes that are different for every company.



^{::clerk/visibility {:code :hide :result :show}}
(clerk/md "## Answering questions about the tournament")

;; Now that we have all this data, we have to answer questions about it to run the gameserver logic.


;; To run my tournament, I need to know which matches are active (not completed, but have two defined participants), so I can enable the correct arenas ingame. Each match has a "state" attribute that defines this, so we can just filter on that. Easy enough.
(defn matches->pending [matches]
  (->> matches
       :data
       (filter #(= "open" (:state (:attributes %))))))

^{::clerk/opts {:auto-expand-results? true}}
(matches->pending matches)

;; This filters my 10 matches down to four, so now I know to allocate four mach arenas in the game.

;; I also need to know which players to allow in which arenas, so I need the corresponding uuids stored in the `:misc` attribute.

;; Lets start by getting the player embedded in each match's `:relationship` map.
(defn matches->participating-user-ids [matches]
  (let [pending-matches (matches->pending matches)]
    (->> pending-matches
         (map :relationships)
         (map (juxt :player1 :player2))
         flatten)))

(first (matches->participating-user-ids matches))
;; Nice! But we're still not done. We have the player ids, but its not the entire resource. The piece of data we want is the included resources. Lets try again, but build a join from challonge id to gameserver uuid first... 
(defn matches->participating-users [matches]
  (let [challongeid->myid (->> matches
                               :included
                               (map (juxt :id (comp :misc :attributes)))
                               (into {}))
        pending-matches (matches->pending matches)]
    (->> pending-matches
         (map :relationships)
         (map (juxt :player1 :player2))
         flatten
         (map (comp :id :data))
         (map challongeid->myid))))

(matches->participating-users matches)

;; Great! now we have a list of uuids I can send to my gameserver.
;; Slinging maps around like this is one of clojure's strenghts, so this felt fairly natural to write.

^{::clerk/visibility {:code :hide :result :show}}
(clerk/md "# Problems")

;; This code works, but what about other questions we could ask of our api response?
;; We went from match to gameserver uuid. What if we had a gameserver uuid and wanted to know which matches they are a part of? (the player runs a chat command to display their upcoming matches) 

;; We would have to write more or less the same amount of code (~30 lines), this time building the join in the other direction. 

;; Every new question we ask of the data requires a new set of functions to traverse the resource graph from question to answer, with very little code reuse, producing code that is hard to read.

^{::clerk/visibility {:code :hide :result :show}}
(clerk/md "# A better way")

;; Enter [xtdb](https://xtdb.com/), the graph database from JUXT. We are receiving a normalized graph of resources from the json-api endpoint, and are computing queries on it. Why not have xtdb run the queries for us.

;; Lets start an xtdb node with an empty configuration map: Instead of persisting the data in one of their [pluggable backends](https://docs.xtdb.com/administration/configuring/#_storage), we store it in plain java datstructures.

^{::clerk/visibility {:code :show :result :hide}}
(def node (xt/start-node {}))


;; Xtdb is schemaless so we don't have to worry about defining any attributes ahead of time.

;; Although you can keep the documents nested and query them in datalog (you can run `(get-in _ [:attributes :timestamps :startedAt])` directly in the query if you wanted), its cleaner to flatten the data before ingesting it.

;; Because challonge happens to use json-api, each response is predictibly structured such that:
;;   * every toplevel response has `:data` and `:included` keys. they contain either a single resource, or a list of them.
;;   * each resource has an `id` key.


;; Lets use this (12 year old!!) function to recursively flatten keys, joining nested keys with a "."

(defn flatten-keys
  "adapted from http://blog.jayfields.com/2010/09/clojure-flatten-keys.html"
  ([m] (flatten-keys {} [] m))
  ([a ks m] (if (map? m)
              (reduce into (map (fn [[k v]] (flatten-keys a (conj ks k) v)) (seq m)))
              (assoc a (keyword (clojure.string/join "." (map name ks))) m))))

;; Lets see a before and after of just one resource.

^{::clerk/opts {:auto-expand-results? true}}
(->> matches :data first)
^{::clerk/opts {:auto-expand-results? true}}
(flatten-keys (->> matches :data first))


;; Much better.

;; Now lets deal with toplevel responses. 

;; This funciton takes every resource from the response, flattens it, and puts it into our database node.

;; Notice how we tranform the challonge notion `:id` to `:xt/id`, which every xtdb document needs.

(defn ingest-jsonapi-response [resp]
  (let [payload (->> resp
                     ((juxt :data :included))
                     (map (fn [res] (cond
                                      (map? res) [res]
                                      (vector? res) res)))
                     flatten
                     (map flatten-keys)
                     (map #(clojure.set/rename-keys % {:id :xt/id})))]
    (xt/await-tx node (xt/submit-tx node (for [doc payload]
                                           [::xt/put doc])))))

(ingest-jsonapi-response matches)

^{::clerk/visibility {:code :hide :result :show}}
(clerk/md "# Datalog is beautiful")
;; Now lets ask all the same questions as before, but this time in a declarative style.

(defn xtdb-matches->pending [node]
  "see which matches are pending (need to and can be played)"
  (map first (xt/q (xt/db node) '{:find [match]
                                  :where [[match :type "match"]
                                          [match :attributes.state "open"]]})))

;; If you have never seen datalog before, dont fret. This query can be read as
;; "find the id of every document that has the key `:type` with a value of `"match"`, and the
;; key `:attributes.state` with a value of `"open"`. Each triple in the `:where` clause of the query is defining a rule/restriction in the shape `[entity attribute value]` that the returned documents must comply to.

;; The effect of declaring that an attribute must match a data literal like `[match :attribute.state "open"]` is a filtering of documents. This line is like my above function `matches->pending` which took my 10 matches down to only 4 open ones.
;; This is a nice delcarative filtering, but nothing mind shattering. The magic comes when you put a variable in the value position of the triple. Now any time you use this variable again (in entity or value position of another rule), the values must match up.  

(xtdb-matches->pending node)

(defn xtdb-matches->participating-users [node]
  "see which matches are pending, and which users are in those matches"
  (xt/q (xt/db node) '{:find [match p1uuid p1name p2uuid p2name]
                       :where [[match :type "match"]
                               [match :attributes.state "open"]
                               [match :relationships.player1.data.id p1]
                               [match :relationships.player2.data.id p2]
                               [p1 :attributes.misc p1uuid]
                               [p1 :attributes.name p1name]
                               [p2 :attributes.misc p2uuid]
                               [p2 :attributes.name p2name]]}))

;; Here we extract the p1 and p2 values from the match relationships, then use those values as entity ids to extract information about the player.

^{::clerk/opts {:auto-expand-results? true}}
(xtdb-matches->participating-users node)


;; Now the reverse operation, (find match ids from gameserver uuid), is trivial.
(defn xtdb-uuid->matches [node uuid]
  "find the challonge match ids that a given player is currently allowed to play in"
  (xt/q (xt/db node) '{:find [match]
                       :where [[p1 :attributes.misc uuid]
                               (or [match :relationships.player1.data.id p1]
                                   [match :relationships.player2.data.id p1])
                               [match :attributes.state "open"]]
                       :in [uuid]}
        uuid))


(xtdb-uuid->matches node "90e9ec35-eaa8-46f8-85ec-ee6e2fd1114e")

;; Or we can ask, given two player names, which matches they are in.
(defn xtdb-names->match [node p1name p2name]
  "find the challonge match ids that a given player is currently allowed to play in"
  (xt/q (xt/db node) '{:find [match]
                       :where [[p1 :type "participant"]
                               [p2 :type "participant"]
                               [p1 :attributes.name p1name]
                               [p2 :attributes.name p2name]
                               [match :type "match"]
                               [match :attributes.state "open"]
                               (or (and
                                    [match :relationships.player1.data.id p1]
                                    [match :relationships.player2.data.id p2])
                                   (and
                                    [match :relationships.player1.data.id p2]
                                    [match :relationships.player2.data.id p1]))]
                       :in [p1name p2name]}
        p1name
        p2name))

(xtdb-names->match node "red" "scarlet")

;; Datalog can also run arbitraty code inside queries. Here, the challonge api gives us the winner of a match as an integer, even though the primary keys are all strings. 
;; Easily rectified by calling str on the key before using it furthur.

(xt/q (xt/db node) '{:find [winnername]
                     :where [[match :type "match"]
                             [match :attributes.winners winner']
                             [(str winner') winner]
                             [winner :type "participant"]
                             [winner :attributes.name winnername]]})

;; We can also run arbitrary predicates on logic variables (`not=`) and access nested documents (`get-in`).

(xt/q (xt/db node) '{:find [points participant]
                     :where [[match :type "match"]
                             [match :attributes.pointsByParticipant points']
                             [(get-in points' [:scores 0]) points]
                             [(get-in points' [:participantId]) participant]
                             [(not= nil points)]]})

;; There is much more you can do with datalog, almost any question you could want to ask is answerable. See [here](https://docs.xtdb.com/language-reference/datalog-queries/) for more examples.


^{::clerk/visibility {:code :hide :result :show}}
(clerk/md "# Conclusion")

;; Thanks to json-api, every response to further api calls moving the tournament forward tan be run through `ingest-jsonapi-resonpse` and be otherwise forgotten.

;; Before xtdb, I would have to write code to answer my questions. After xtdb, every question I can ask of my data is already answered, I just have to phrase the quesiton!
