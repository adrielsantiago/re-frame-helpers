# Simplified http requests

This library uses [re-frame-http-fx][re-frame-http-fx] which depends on [cljs-ajax](https://github.com/JulianBirch/cljs-ajax) for http requests. In brief, [re-frame-http-fx][re-frame-http-fx] adds an `:http-xhrio` effect to configure and ultimately trigger an http request when returned from an event handler. Often times requests end up looking quite similar, which can lead to a lot of repeat code.

### xhrio Interceptor

The `xhrio` interceptor pulls from the `re-frame-helpers.xhrio/!xhrio-defaults` atom to determine a default configuration. Each event declaration can override whatever aspects of the defaults they desire. The defaults should be a map whose values are either the value itself or a function which accepts the coeffects at the time the request is called and returns the value. Without any setup the following defaults are used:

```
{:method          :get
 :format          (ajax/json-request-format)
 :response-format (ajax/json-response-format {:keywords? true})}
```

In the case of an application calling a backend api to get most of its data, this would mean that configuring the defaults with sufficient settings could allow for an `:http-xhrio` effect that only requires the `:uri` and `:on-success` key value pairs. As always, the event could still return `:params` or any other supported properties and they would get added to the defaults. If the event handler returns a property that is already defined in the default, the handler's value is honored and overrides the default. Here is what that might look like:

```
;; Main File - set useful defaults
(ns my.main)
  (:require [re-frame-helpers.xhrio :refer [!xhrio-defaults]])

(set! !xhrio-defaults {:headers (fn [cofx] (extract-headers cofx))
                        :with-credentials false})

;; Re-frame Events File
(ns my.events
  (:require [re-frame-helpers.macros :refer-macros [defevent]]
            [re-frame-helpers.xhrio :refer [xhrio]]))

(defevent
  :fetch-something
  [xhrio]
  (fn [])
    {:http-xhrio {:uri 'api/fetch/something'
                  :on-success [:good-fetch]}})

(defevent
  :post-something
  [xhrio]
  (fn []
    {:http-xhrio {:method :post                    ;; overrides the default :get
                  :headers {"Authorization" "abc"} ;; overrides the !xhrio-defaults
                  :uri 'api/endpoint'
                  :params {:some 'parameters'}
                  :on-success [:success-handler]}}))
```

### defxhrio Macro

Typically when calling an endpoint we are really interested in three things on the frontend: the response, loading state and any potential errors. By using the `defxhrio` macro we can automatically apply the `xhrio` interceptor described above as well as provide default actions for `:on-success` and `:on-failure` that use the self-same dispatch event key to store a map with the response (`:response`), loading state (`:loading?`) and error (`:error`) in the `app-db`. It makes this available as a subscription.

To make it all easier to grok here is an example of what using this macro might look like:

```
;; dispatchers.cljs file
(ns some.dispatchers
  (:require [re-frame-helpers.macros :refer-macros [defxhrio]))

(defxhrio
  :get-something
  (fn []
    {:http-xhrio {:uri "fetch/some/data"}})

;; core.cljs file
(ns some.core)

(defn frontend-component []
  (let [{:keys [response loading? error]} @(rf/subscribe [:get-something])]
    [:button
      {:on-click #(rf/dispatch [:get-something])}
      "Show me some data"]
    (cond
      loading? ;; show progress bar
      error    ;; show the user an error message
      response ;; show the response data
```

NOTE: We both dispatch the request and get the request results by subscribing to the same key. In the example above that key is `:get-something`, but the key could be any keyword.

So what exactly is happening above? In the `frontend-component` the `:get-something` subscription value is instantiated as `{:response nil :loading? false :error nil}` then when the user clicks the `"Show me some data"` button the value becomes `{:response nil :loading? true :error nil}`. The loading state has been toggled to true, also causing our component to render the loading state code. Upon either success or failure the response or error are updated accordingly and loading state is set back to false. A successful request might look like `{:response {:the "data"} :loading? false :error nil}`, whereas a failed request might look like `{:response nil :loading? false :error {:status 403 :msg "Forbidden"}}`.

So by using this pattern we can simply dispatch http requests and any component that is subscribed to the dispatch key that triggers the request will update its UI without us having to manage separate re-frame events or db state changes for the loading state, response, or error.

Sometimes, we may need some more custom behavior than the basic example provided above. There are a couple additional options available when using the `defxhrio` macro:

```
(defxhrio
  :get-something
  (fn []
    {:http-xhrio {:uri "fetch/some/data"}})
     :xhrio/dispatch-n-on-success [[:do-something-else]]
     :xhrio/dispatch-n-on-failure [[:something-bad-happened]]
     :xhrio/mutate-response (fn [response] (sort response))
     :xhrio/mutate-error (fn [error] (assoc error :msg "Yikes!"))
```

|Custom Effect|Expects|Description|
|-|-|-|
|`:xhrio/dispatch-n-on-success`|vector of event vectors|dispatches the provided event vectors after completing a successful request - the response can be retrieved by injecting the `:get-something` subscription value into the event's coeffects (re-frame-helpers `defevent` can help with this)|
|`:xhrio/dispatch-n-on-failure`|vector of event vectors|dispatches the provided event vectors after a failed request - those events can inject `:get-something` to access the error|
|`:xhrio/mutate-response`|`function`|takes one argument: the request response. Whatever is returned from this function is stored in the db as the request response. Allows for mutating the response before storage (e.g. sanitization, sorting, only storing a subset of the response data, etc.)|
|`:xhrio/mutate-error`|`function`|takes one argument: the request error. Whatever is returned from this function is stored in the db as the request error.|

[re-frame-http-fx]: https://github.com/day8/re-frame-http-fx
