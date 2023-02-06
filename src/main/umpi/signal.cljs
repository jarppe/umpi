(ns umpi.signal
  (:require [umpi.effect-context :as context]))


(deftype Signal [^:mutable value
                 signal-meta
                 ^:mutable watches]
  IAtom

  IDeref
  (-deref [this]
    (context/signal-referenced this)
    value)

  IWatchable
  (-add-watch [this key f]
    (set! (.-watches this) (assoc watches key f)))
  (-remove-watch [this key]
    (set! (.-watches this) (dissoc watches key)))
  (-notify-watches [this old-value new-value]
    (doseq [[key f] watches]
      (f key this old-value new-value)))

  IReset
  (-reset! [this new-value]
    (when (not= value new-value)
      (let [old-value value]
        (set! (.-value this) new-value)
        (-notify-watches this old-value new-value)))
    new-value)

  ISwap
  (-swap! [this f]
    (-reset! this (f value)))
  (-swap! [this f a]
    (-reset! this (f value a)))
  (-swap! [this f a b]
    (-reset! this (f value a b)))
  (-swap! [this f a b xs]
    (-reset! this (apply f value a b xs)))

  IHash
  (-hash [this]
    (goog/getUid this))

  INamed
  (-name [_this]
    (:name signal-meta))

  IMeta
  (-meta [_this]
    signal-meta)

  Object
  (equiv [this other]
    (-equiv this other))
  (toString [this]
    (str "#object[umpi.signal.Signal[ {:val " value ", :name \"" (name this) "\"}]]"))

  IEquiv
  (-equiv [o other]
    (identical? o other)))


(defn ^Signal signal
  (^Signal [value] (signal value nil))
  (^Signal [value signal-meta]
   (Signal. value signal-meta {})))


(defn signal? [other]
  (instance? Signal other))

