(ns umpi.mutation
  (:require [goog.object :as go]))


(defn mutation-remove-node [^js node]
  (println "removing node" node)
  (when-let [on-unmount (go/get node "on-unmount")]
    (on-unmount))
  (doseq [child (.-childNodes node)]
    (mutation-remove-node child)))


(defn on-mutation [^js mutation-record]
  (doseq [^js record mutation-record
          ^js node   (.-removedNodes record)]
    (mutation-remove-node node)))


(defn mutation-listener [dom-element]
  (let [mutation-observer (js/MutationObserver. on-mutation)]
    (.observe mutation-observer dom-element #js {:subtree       true
                                                 :childList     true
                                                 :characterData true})
    (fn []
      (.disconnect mutation-observer))))