(ns hello.app
  (:require [umpi.core :as umpi]))


(def a (umpi/create-signal 0))
(def b (umpi/create-signal 0))
(def sum (umpi/create-signal 0))

#_(defn Hello []
    (umpi/create-effect
     (fn []
       (reset! sum (+ @a @b))))
    [:<>
     [:h1 {:style {:color "hotpink"}} "Hello"]
     [:div a " + " b " = " sum]
     [:button {:on-click (fn [_] (swap! a inc))} "+ a"]
     [:button {:on-click (fn [_] (swap! b inc))} "+ b"]])


(defn Hello []
  (let [margin (umpi/create-signal "0em")]
    (umpi/create-effect
     (fn []
       (reset! margin (str @a "em"))))
    [:div {:style {:margin margin}}
     [:h1 "Hellou: " a]
     [:button
      {:on-click (fn [e]
                   (println "click" e)
                   (swap! a inc))}
      "push"]]))


(defonce root (umpi/create-root (js/document.getElementById "app")))


(defn ^:export start []
  (println "start")
  (umpi/render [Hello] root))
