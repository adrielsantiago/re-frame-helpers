(ns docs.sub-injection.core
  (:require [re-frame-helpers.macros :refer [defevent defsub]
                                     :include-macros true]
            [re-frame.core :as rf]
            [docs.components :as ui]))

(defsub
  :sub-injection/doubled
  (fn [db] (* 2 (:value db))))

(defevent
  :sub-injection/set-value
  (fn [{:keys [db]} [_ val]]
    {:db (assoc db :value val)}))

(defevent
  :sub-injection/double-double
  :<- [:sub-injection/doubled]
  (fn [{:keys [db] [doubled] :subs}]
    {:db (assoc db :value doubled)}))

(defn example []
  (rf/dispatch [:sub-injection/set-value 10])
  (fn []
    (let [sub-value @(rf/subscribe [:sub-injection/doubled])]
      [:<>
       [:span "(code in docs.sub-injection.core)"]
       [:p (str "Value: " sub-value)]
       [ui/button
        {:on-click #(rf/dispatch [:sub-injection/double-double])}
        "Click to Double Up"]])))
