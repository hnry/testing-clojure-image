(defproject testing-clojure-image "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.0"]
                 [http-kit "2.2.0-alpha1"]
                 [environ "1.0.2"]
                 [selmer "1.0.4"]
                 [bing-search "0.1.0-SNAPSHOT"]
                 [org.clojure/data.json "0.2.6"]
                 [ring/ring-defaults "0.2.0"]

                 [clj-http "3.0.0"]]
  :main ^:skip-aot testing-clojure-image.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
