(ns re-frame-helpers.macros
  #?(:cljs (:require [re-frame-helpers.interceptors]
                     [re-frame-helpers.xhrio])))

(defn ^:private parse-args [args]
  (let [sugar-marker :<-
        handler (last args)
        first-arg (first args)
        interceptors (when (vector? first-arg) first-arg)
        subs-args (drop (if (some? interceptors) 1 0) (butlast args))
        pairs (partition 2 subs-args)
        query-vecs (mapv last pairs)
        error? (and (> (count pairs) 0)
                    (not (and (every? #{sugar-marker} (map first pairs))
                              (every? vector? query-vecs))))]
    {:handler handler
     :interceptors interceptors
     :injection (when (seq subs-args)
                  `(re-frame-helpers.interceptors/inject ~query-vecs))
     :error (when error?
              (apply str "sub injection requires pairs of " sugar-marker
                     "and query vectors, got:" subs-args))}))

(defmacro defevent [id & args]
  (let [{:keys [handler interceptors injection error]} (parse-args args)]
    `(do
       (when ^boolean goog/DEBUG
         (do
           (when ~error (.error js/console ~id "registration error:" ~error))
           (def ~(-> id name symbol) ~handler)))
       (re-frame.core/reg-event-fx ~id
                                   ~(cond-> (or interceptors [])
                                      (some? injection) (conj injection))
                                   ~handler))))

(defmacro defsub [id & args]
  `(do
     (when ^boolean goog/DEBUG
       (def ~(-> id name symbol) ~(last args)))
     (re-frame.core/reg-sub ~id ~@args)))

(defmacro defxhrio [id & args]
  (let [{:keys [handler interceptors injection]} (parse-args args)
        assoc-response `(re-frame-helpers.interceptors/assoc-response ~id)
        xhrio `re-frame-helpers.interceptors/xhrio
        merged (apply conj [assoc-response xhrio] (or interceptors []))]
    `(do
       (defsub ~id #(get % ~id))
       (defevent ~id
         ~(cond-> merged (some? injection) (conj injection))
         ~handler))))
