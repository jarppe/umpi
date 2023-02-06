(ns umpi.mock-dom)


(defn mock-element [tag]
  (let [state (atom {:attributes {}
                     :children   []})]
    (clj->js {:_type        :element
              :_state       state
              :tagName      tag
              :setAttribute (fn [k v] (swap! state update :attributes assoc k v))
              :appendChild  (fn [e] (swap! state update :children conj e))})))


(defn mock-fragment []
  (let [state (atom {:children []})]
    (clj->js {:_type       :fragment
              :_state      state
              :appendChild (fn [e] (swap! state update :children conj e))})))


(def mock-document #js {:_type                  :document
                        :createElement          mock-element
                        :createDocumentFragment mock-fragment})


(defn init []
  (set! js/document mock-document))
