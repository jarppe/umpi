(ns hello.app
  (:require [umpi.core :as umpi])
  (:require-macros [umpi.core :refer [with-effect]]))


(def a (umpi/create-signal 0))


(def colors ["hotpink" "turquoise" "tomato" "blue" "lime" "violet" "pink"])


(defn Hello []
  (let [color (umpi/create-signal nil)]
    (with-effect
      (reset! color (nth colors (mod @a (count colors)))))
    [:div {:class "container"}
     [:h1 "Hello: " [:span {:style {:color color}} a]]
     [:div {:class "buttons"}
      [:a {:href     "#"
           :role     "button"
           :on-click (fn [e]
                       (println "click" e)
                       (swap! a inc))}
       "+"]
      [:a {:href     "#"
           :role     "button"
           :on-click (fn [e]
                       (println "click" e)
                       (swap! a dec))}
       "-"]]]))


(defonce root (umpi/create-root (js/document.getElementById "app")))


(defn ^:export start []
  (println "start")
  (umpi/render [Hello] root))
