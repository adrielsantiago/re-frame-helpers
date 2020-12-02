(ns docs.core
  (:require-macros [docs.md :refer [get-md-files]])
  (:require [clojure.string :as s]
            [reagent.core :as r]
            [reagent.dom :as rdom]
            [docs.components :as ui]
            [docs.sub-injection.core :as sub-injection]))

(def markdown (get-md-files))

(def examples {:event-sub-injection sub-injection/example})

(def toc-md
  (reduce (fn [acc {:keys [key]}]
            (let [header (name key)]
              (str acc
                   "\n\n ["
                   (-> header
                       (.replaceAll "-" " ")
                       s/capitalize)
                   "](#" header ")")))
          "# Table of Contents"
          markdown))

(defn docs []
  [:<>
   [ui/card "top" toc-md nil]
   (for [{:keys [md key]} markdown]
     [:<>
      {:key key}
      [ui/card (name key) md (examples key)]])])

(defn render []
  (rdom/render [docs] (.getElementById js/document "app")))

(defn init [] (render))
