(ns user
  (:require [nextjournal.clerk :as clerk]))

(comment (clerk/serve! {:browse? true})

         (clerk/show! "src/jsonapi_xtdb/core.clj"))
