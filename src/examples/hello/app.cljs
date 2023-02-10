(ns hello.app
  (:require [umpi.core :as umpi])
  (:require-macros [umpi.core :refer [with-effect]]))



(def colors ["hotpink" "turquoise" "tomato" "blue" "lime" "violet" "pink"])


(defn get-color [n]
  (nth colors (mod n (count colors))))


(defn Hello []
  (let [counter (umpi/create-signal 0)
        color   (umpi/create-signal nil)]
    (with-effect
      (reset! color (get-color @counter)))
    [:div {:class "container"}
     [:h1 {:style {:margin-top "2em"}} "Hello: "
      [:span {:style {:color color}} counter]]
     [:div {:class "buttons"}
      [:a {:href     "#"
           :role     "button"
           :on-click (fn [_] (swap! counter inc))
           :style    {:width "10em"}}
       "+"]]]))


(defonce root (umpi/create-root (js/document.getElementById "app")))


(defn ^:export start []
  (umpi/render [Hello] root))
