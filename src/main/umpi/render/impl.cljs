(ns umpi.render.impl
  (:require [umpi.render.core :refer [IRender]]
            [umpi.signal :as signal]
            [umpi.render.component :as c]
            [umpi.render.text-element :as te]
            [umpi.render.signal-element :as se]))


(extend-protocol IRender
  PersistentVector
  (-render [this]
    (c/render-component this))

  signal/Signal
  (-render [this]
    (se/render-signal-element this))

  string
  (-render [this]
    (te/render-text-element this))

  number
  (-render [this]
    (te/render-text-element (str this)))

  boolean
  (-render [this]
    (te/render-text-element (if this "true" "false")))

  Cons
  (-render [this]
    (throw (ex-info "Not implemented yet" {:type 'Cons
                                           :this this})))

  List
  (-render [this]
    (throw (ex-info "Not implemented yet" {:type 'List
                                           :this this})))

  LazySeq
  (-render [this]
    (throw (ex-info "Not implemented yet" {:type 'LazySeq
                                           :this this})))

  nil
  (-render [_this]
    nil))
