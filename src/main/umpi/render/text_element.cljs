(ns umpi.render.text-element)

(defn render-text-element [text]
  (js/document.createTextNode text))
