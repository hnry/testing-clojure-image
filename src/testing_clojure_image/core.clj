(ns testing-clojure-image.core
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [selmer.parser :refer [render-file]]
            [environ.core :refer [env]]
            [clojure.data.json :as json]
            [bing-search.search :refer [search set-key!]]
            [org.httpkit.server :refer [run-server]]))

(set-key! (env :bing))

;; should be in bing-search
(defn parse-json
  [resp]
  (json/read-str (resp :body)))

;; TODO make this generic so it can handle XML data?
;; or really bing-search should generalize the data for us?
(defn extract-image-data
  "extracts just the useful data for each image, removes the noise {Title SourceUrl MediaUrl Thumbnail}"
  [resp]
  (let [data (parse-json resp)
        images (get (get data "d") "results")]
    (map (fn [i]
           (let [img (select-keys i ["Title" "SourceUrl" "MediaUrl"])
                 thumb ((select-keys (get i "Thumbnail") ["MediaUrl"]) "MediaUrl")]
             (assoc img "Thumbnail" thumb)))
         images)))

(defn route-recent []
  {:status 200 :headers {"Content-Type" "application/json"} :body (json/write-str "")})

(defn route-search [term]
  (let [images (extract-image-data (search term {:format :json :top 10}))]
    ;; (save recent-images) TODO
    {:status 200 :headers {"Content-Type" "application/json"} :body (json/write-str {:images images})}))

(defn route-home []
  (render-file "views/index.html" []))

;; ability to have an offset ?offset=2
(defroutes app
  (GET "/" [] (route-home))
  (GET "/search/:term" [term] (route-search term))
  (GET "/recent" [] (route-recent)))

(defn -main [& port]
  (let [port (Integer. (or port (env :port) 3000))]
    (println "HTTP Server starting on:" port)
    (run-server (wrap-defaults #'app site-defaults) {:port port})))
