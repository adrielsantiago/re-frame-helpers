(ns re-frame-helpers.xhrio
  (:require-macros [re-frame-helpers.macros :refer [defevent]])
  (:require [re-frame.core :as rf]))

(def !xhrio-defaults (atom {}))

(rf/reg-fx :xhrio/dispatch-n-on-success (fn []))
(rf/reg-fx :xhrio/dispatch-n-on-failure (fn []))
(rf/reg-fx :xhrio/mutate-response (fn []))
(rf/reg-fx :xhrio/mutate-error (fn []))

(defn ^:private mutate? [mutate value]
  (cond-> value (fn? mutate) mutate))

(defevent
  :xhrio/good-request
  (fn [{:keys [db]} [_ container-key dispatch-n mutate response]]
    (cond-> {:db (assoc db container-key {:response (mutate? mutate response)
                                          :loading? false
                                          :error nil})}
      (vector? dispatch-n) (assoc :dispatch-n dispatch-n))))

(defevent
  :xhrio/bad-request
  (fn [{:keys [db]} [_ container-key dispatch-n mutate error]]
    (cond-> {:db (assoc db container-key {:response nil
                                          :loading? false
                                          :error (mutate? mutate error)})}
      (vector? dispatch-n) (assoc :dispatch-n dispatch-n))))
