# Define subscriptions

Defining subs with `defsub` is a wrapper around `re-frame.core/reg-sub` but with the added benefit of being able to import the function handler from the namespace when compiling for development. For more information, see the description under the `Easier Testing` header in [Define Events](#define-events).
