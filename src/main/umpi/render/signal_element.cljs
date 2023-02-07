(ns umpi.render.signal-element
  (:require [umpi.effect-context :as ec]
            [umpi.render.component-context :as cc]))


(defn render-signal-element [signal]
  (let [e         (js/document.createTextNode "")
        effect-fn (fn []
                    (let [value (str @signal)]
                      (.replaceData e 0 -1 value)))]
    (cc/add-effect-context (ec/effect-context effect-fn))
    e))
