(ns re-frame-helpers.utils)

(defn conj-to-vec-or-make-vec
  [v? & addends]
  (apply conj (if (vector? v?) v? []) addends))
