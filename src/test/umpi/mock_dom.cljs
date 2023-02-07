(ns umpi.mock-dom
  (:require [applied-science.js-interop :as j]))


(defn mock-class-list ^js []
  (let [classes    (atom #{})]
    (j/obj :add (fn [class-name] (swap! classes conj class-name))
           :remove (fn [class-name] (swap! classes disj class-name))
           :entries (fn [] (seq @classes)))))


(defn mock-element ^js [tag]
  (let [state      (atom {:attributes {}
                          :children   []
                          :listeners  {}})]
    (clj->js {:_type            :element
              :_state           state
              :tagName          tag
              :setAttribute     (fn [k v] (swap! state update :attributes assoc k v))
              :appendChild      (fn [e] (swap! state update :children conj e))
              :classList        (mock-class-list)
              :style            #js {}
              :addEventListener (fn [event-name listener opts] (swap! state update :listeners assoc event-name [listener opts]))})))


(defn mock-fragment ^js []
  (let [state (atom {:children []})]
    (clj->js {:_type       :fragment
              :_state      state
              :appendChild (fn [e] (swap! state update :children conj e))})))


(def ^js mock-document #js {:_type                  :document
                            :createElement          mock-element
                            :createDocumentFragment mock-fragment})


(defn init []
  ; TODO: should throw if called in browser
  (set! js/document mock-document))



(comment
  (require 'umpi.signal)
  (require 'umpi.render.component)
  (require '[goog.object :as go])

  (def set-class-list #'umpi.render.component/set-class-list)

  (let [^js e (mock-element "div")]
    (set-class-list e [:foo :bar])
    (set (.entries (.-classList e))))

  (def set-style #'umpi.render.component/set-style)

  (let [^js e (mock-element "div")]
    (set-style e {:foo "1"
                  :bar "2"
                  :boz (umpi.signal/signal "foozaa")})
    (.-style e))

  (def add-listener #'umpi.render.component/add-listener)

  (let [^js e (mock-element "div")]
    (add-listener e :on-mousemove 'listener 'opts)
    (-> e .-_state deref :listeners))



  ;
  )