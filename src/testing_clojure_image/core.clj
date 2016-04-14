(ns testing-clojure-image.core
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [selmer.parser :refer [render-file]]
            [environ.core :refer [env]]
            [clojure.data.json :as json]
            [testing-clojure-image.db :as db]
            [bing-search.search :refer [search set-key!]]
            [org.httpkit.server :refer [run-server]]))

(set-key! (env :bing))

(defn save-to-recent [term]
  (db/insert term))

(defn get-recent []
  (let [results (db/find 10)
        dateformat (java.text.SimpleDateFormat. "MMMM dd, yyyy HH:mm:ss aa")]
    ;; convert Date objects to string for json
    ;; also don't show object id
    (map #(select-keys (assoc % :timestamp (.format dateformat (% :timestamp))) [:timestamp :search]) results)))

(def not-nil? (complement nil?))

(defn extract-image-data
  "extracts just the useful data for each image, removes the noise {Title SourceUrl MediaUrl Thumbnail}"
  [images]
  (when (not-nil? images)
    (map (fn [i]
           (let [img (select-keys i ["Title" "SourceUrl" "MediaUrl"])
                 thumb ((i "Thumbnail") "MediaUrl")]
             (assoc img "Thumbnail" thumb)))
         images)))

(defn route-recent []
  {:status 200 :headers {"Content-Type" "application/json"} :body (json/write-str {:recent (get-recent)})})

(defn route-search
  [term offset]
  (let [offset (if (nil? offset) 0 offset)
        search-resp (search :Image term {:$format :json :$top 10 :$skip offset})
        result (extract-image-data (search-resp :result))
        resp {:headers {"Content-Type" "application/json"}}
        resp (if (not-nil? result)
                    (assoc resp :body {:query term :pagination offset :images result} :status 200)
                    (assoc resp :body {:error ((search-resp :response) :body)} :status 500))]
    (save-to-recent term)
    (assoc resp :body (json/write-str (resp :body)))))

(defn route-home []
  (render-file "views/index.html" []))

(defroutes app
  (GET "/" [] (route-home))
  (GET "/search/:term" [term offset] (route-search term offset))
  (GET "/recent" [] (route-recent)))

(defn -main [& port]
  (let [port (Integer. (or (first port) (env :port) 3000))]
    (println "HTTP Server starting on:" port)
    (run-server (wrap-defaults #'app site-defaults) {:port port})))
