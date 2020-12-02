(ns docs.md
  (:require [clojure.string :as s]))

(def ORDER [:define-events
            :define-subscriptions
            :event-sub-injection
            :simplified-http-requests])

(defmacro get-md-files []
  (mapv (fn [filename]
          {:md (slurp (str "public/md/" (name filename) ".md"))
           :key filename})
        ORDER))
