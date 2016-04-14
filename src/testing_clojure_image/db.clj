(ns testing-clojure-image.db
  (:require [monger.core :as mongo]
            [monger.collection :as collection]
            [monger.query :as query]
            [environ.core :refer [env]]))

(def table "image-search")

(defn connect []
  (let [uri (env :mongouri)]
    (mongo/connect-via-uri uri)))

(defn insert
  [term]
  (let [{conn :conn db :db} (connect)
        datetime (java.util.Date.)]
  (collection/insert db table {:search term :timestamp datetime})))

(defn find
  [size]
  (let [{conn :conn db :db} (connect)]
    (query/with-collection db table
      (query/limit size)
      (query/sort (sorted-map :timestamp -1)))))
