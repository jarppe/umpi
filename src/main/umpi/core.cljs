(ns umpi.core
  (:require [umpi.signal :as signal]
            [umpi.effect-context :as ec]
            [umpi.mutation :as mut]
            [umpi.render :as render]))


#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn create-signal
  ([] (create-signal nil nil))
  ([initial-value] (create-signal initial-value nil))
  ([initial-value signal-meta]
   (signal/signal initial-value signal-meta)))



#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn create-effect
  ([f] (create-effect f nil))
  ([f meta]
   (ec/effect-context f meta)))


(defn- render-view [view]
  (render/render view))


#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn create-root [^js root-element]
  (atom {:root-element            root-element
         :close-mutation-observer (mut/mutation-listener root-element)}))


#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn delete-root [root]
  (when root
    (let [{:keys [close-mutation-observer]} @root]
      (close-mutation-observer))
    nil))


#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn render [view root]
  (let [{:keys [^js root-element ^js dom-element]} @root
        new-element                        (render-view view)]
    (if dom-element
      (do (println "replace view\n" dom-element "\n" new-element)
          (.replaceWith dom-element new-element))
      (do (println "append view\n" root-element "\n" new-element)
          (.appendChild root-element new-element)))
    (swap! root assoc :dom-element new-element)))


(comment

  ()
  (println "Hello")
  ;
  )