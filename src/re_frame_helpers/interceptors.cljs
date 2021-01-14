(ns re-frame-helpers.interceptors
  (:require [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [re-frame.core :refer [->interceptor]]
            [re-frame.db :refer [app-db]]
            [re-frame.interop :refer [reagent-id dispose!]]
            [re-frame.loggers :refer [console]]
            [re-frame.registrar :refer [get-handler]]
            [re-frame.utils :refer [first-in-vector]]
            [re-frame.subs :refer [cache-lookup]]
            [re-frame.trace :as trace :include-macros true]
            [re-frame-helpers.xhrio :refer [!xhrio-defaults]]
            [re-frame-helpers.utils :refer [conj-to-vec-or-make-vec]]))

(defn subscribe!
  "re-frame.core/subscribe but doesn't add to cache and returns derefed value"
  ([query] (subscribe! query app-db))
  ([query db]
   (trace/with-trace {:operation (first-in-vector query)
                      :op-type   :sub/create
                      :tags      {:query-v query}}
     (if-let [cached (cache-lookup query)]
       (do
         (trace/merge-trace! {:tags {:cached?  true
                                     :reaction (reagent-id cached)}})
         @cached)
       (let [query-id   (first-in-vector query)
             handler-fn (get-handler :sub query-id)]
         (trace/merge-trace! {:tags {:cached? false}})
         (if (nil? handler-fn)
           (do (trace/merge-trace! {:error true})
               (console :error (str "re-frame: no subscription handler "
                                    "registered for: " query-id
                                    ". Returning a nil subscription.")))
           (let [sub (handler-fn db query)
                 value @sub]
             (when-not (seq ^map (.-watches sub)) (dispose! sub))
             value)))))))

(defn inject
  "Inject subscriptions into event cofx"
  [query-v]
  (let [inject-key :subs
        multiple? (sequential? (first query-v))
        queries (if multiple? query-v [query-v])]
    (->interceptor
     :id :inject-sub
     :before (fn [{{:keys [db]} :coeffects :as context}]
               (update-in context
                          [:coeffects inject-key]
                          #(apply conj-to-vec-or-make-vec %
                                  (map subscribe! queries))))
     :after (fn [context]
              (update-in context
                         [:coeffects]
                         dissoc
                         inject-key)))))

(def xhrio
  (->interceptor
   :id :xhrio
   :after
   (fn [{:keys [coeffects] :as context}]
     (update-in context
                [:effects :http-xhrio]
                (fn [xhrio-settings]
                  (merge {:method           :get
                          :format           (ajax/json-request-format)
                          :response-format  (ajax/json-response-format
                                             {:keywords? true})}
                         (reduce (fn [acc [prop fn-or-val]]
                                   (assoc acc prop (if (fn? fn-or-val)
                                                       (fn-or-val coeffects)
                                                       fn-or-val)))
                                 {} (seq @!xhrio-defaults))
                         xhrio-settings))))))

(defn assoc-response [container-key]
  (->interceptor
   :id :assoc-response
   :after
   (fn [{:as context :keys [effects coeffects]}]
     (let [success-mutator (:xhrio/mutate-response effects)
           failure-mutator (:xhrio/mutate-error effects)
           success-dispatches (:xhrio/dispatch-n-on-success effects)
           failure-dispatches (:xhrio/dispatch-n-on-failure effects)]
       (-> context
           (update-in [:effects :http-xhrio]
                      #(merge {:on-success [:xhrio/good-request
                                            container-key
                                            success-dispatches
                                            success-mutator]
                               :on-failure [:xhrio/bad-request
                                            container-key
                                            failure-dispatches
                                            failure-mutator]}
                              %))
           (assoc-in [:effects :db] (:db coeffects))
           (update-in [:effects :db] merge (:db effects))
           (update-in [:effects :db container-key]
                      {:response nil :loading? true :error nil}))))))
