(ns umpi.mock-dom
  (:require [applied-science.js-interop :as j]))


(defn class-list ^js []
  (let [classes (atom #{})]
    (j/obj :add (fn [class-name] (swap! classes conj class-name))
           :remove (fn [class-name] (swap! classes disj class-name))
           :entries (fn [] (seq @classes)))))


(defn create-element ^js [tag]
  (let [state (atom {:attributes {}
                     :children   []
                     :listeners  {}})]
    (clj->js {:_type            :element
              :_state           state
              :tagName          tag
              :setAttribute     (fn [k v] (swap! state update :attributes assoc k v))
              :appendChild      (fn [e] (swap! state update :children conj e))
              :classList        (class-list)
              :style            #js {}
              :addEventListener (fn [event-name listener opts] (swap! state update :listeners assoc event-name [listener opts]))})))


(defn create-document-fragment ^js []
  (let [state (atom {:children []})]
    (clj->js {:_type       :fragment
              :_state      state
              :appendChild (fn [e] (swap! state update :children conj e))})))


(def ^js document #js {:_type                  :document
                       :createElement          create-element
                       :createDocumentFragment create-document-fragment})


(defn init []
  ; TODO: should throw if called in browser
  (set! js/document document))
