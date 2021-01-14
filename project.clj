(defproject hzi/re-frame-helpers "0.0.3"
  :description "Helpers to make life with re-frame easier"
  :url ""

  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}

  :deploy-repositories [["clojars" {:sign-releases false}]]

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.773"]
                 [re-frame "1.1.2"]
                 [cljs-ajax "0.8.1"]
                 [day8.re-frame/http-fx "0.2.1"]])
