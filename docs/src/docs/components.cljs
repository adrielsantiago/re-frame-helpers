(ns docs.components
  (:require ["react-markdown" :as react-markdown]
            ["remark-gfm" :as gfm]))

(defn button [props text]
  [:button
   (merge {:class ["border" "border-gray-600" "rounded" "px-4 py-2"
                   "focus:outline-none" "hover:bg-gray-700"
                   "hover:text-gray-200"]}
          props)
   text])

(defn divider []
  [:hr {:class "text-gray-200 border-t-2 border-dotted mt-6 mb-5"}])

(defn card [name md example]
  [:div
   {:id name
    :class ["max-w-4xl" "px-5" "pt-2" "pb-8" "mx-auto" "my-5" "bg-white"
            "text-justify" "rounded-xl" "border-b-4" "border-gray-700"]}
   [:> react-markdown {:source md :plugins [gfm]}]
   (when example
     [:<>
      [divider]
      [:h3 "Example"]
      [example]])
   (when-not (= name "top")
     [:<>
      [divider]
      [:div {:class "flex justify-end"}
       [:a {:href "#top"} "Back to Table of Contents"]]])])
