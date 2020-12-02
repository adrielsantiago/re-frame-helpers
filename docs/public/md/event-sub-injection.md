# Event sub injection

Re-frame has some syntax sugar in the form of `:<-` to allow access to subscriptions from within a subscription function handler. That tends to look something like:

```clojure
(defsub
  :ns/calculated-value
  :<- [:some-other-sub]
  (fn [some-other-sub]
    (inc some-other-sub))
```

Subs are a Reagent `reaction`, which is a watcher that ensures sub values are updated automatically when their dependencies change. They also cache these values for quick access. This is ideal when dereferencing these subs within the context of a UI component.

However, sometimes it is convenient to access to these subs within an event handler. If you were to dereference a sub within an event handler you risk a small performance hit as well as a potential memory leak by creating reactions that never get freed. Refer to the [re-frame docs](https://github.com/day8/re-frame/blob/master/docs/FAQs/UseASubscriptionInAnEventHandler.md) if you are looking for additional information on why injecting subs into the event co-effects is the way to go.

This library offers subscription injection with some syntax sugar a la re-frame. In order to get the sub value, it will first try to pull it from the existing cache. If the sub is not currently in use in the UI, it is likely not going to be cached. In these cases it will calculate the value, refrain from caching it, and dispose of the reagent reaction immediately.

You can make use of this injection capability to access subs in events by using `defevent`. Injecting them into the coeffects is done using the same syntactic sugar supported by re-frame subscriptions: `:<-` (placed after the interceptors). The sub values get injected as a vector into the co-effects under the `:subs` keyword. For instance:

```clojure
(defevent
  :ns/something-happened
  [interceptor]
  :<- [:sub-a]
  :<- [:sub-b]
  :<- [:sub-c]
  (fn [{[a b c] :subs}]
    {:dispatch-n [[:fetch-something a b c]]}))
```
