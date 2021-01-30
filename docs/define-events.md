# Define events

Defining events with `defevent` is a wrapper around `re-frame.core/reg-event-fx` with some added utility:

### Easier Testing

When compiling in development (or more specifically when `goog.DEBUG` is `true`) `defevent` makes the anonymous function handler available as a named export from the namespace. So if you were to:

```
(ns my.events)

(defevent
  :my/event-occurred
  (fn [] {}))
```

You could then access the handler function with

`(:require [my.events :refer [event-occurred]])`

This is primarily there to allow for easy unit testing of the function handlers since it isolates the logic and avoids having to worry about polluting the integrity of the test with global state.

NOTE: When referencing the handler, the namespace of the event is left off. You can see above that `:my/event-occurred` is accessed from `my.events` as `event-occurred`. It is best to avoid name collisions within the same file namespace to ensure it remains clear which handler is being referenced.

### Sub Injection

`defevent` provides some syntax sugar for injectiong subscription values into event coeffects. See the `/docs/event-sub-injection.md` for more details.
