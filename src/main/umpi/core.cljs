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
(defn create-effect [f]
  (ec/effect-context f))


(defn- render-view [view]
  (render/render view)
  #_(let [h1   (js/document.createElement "h1")
          _    (set! (.-innerText h1) "Hello!")
          _    (set! (.-id h1) "the-h1")
          div1 (doto (js/document.createElement "div")
                 (.appendChild h1)
                 (.setAttribute "foo" "42"))
          div2 (doto (js/document.createElement "div")
                 (.appendChild div1)
                 (.setAttribute "bar" "boz"))]
      div2))


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