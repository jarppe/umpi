(ns umpi.render.component-context)

;;
;; Context for building components:
;;


(defonce component-build-context (atom nil))


(defn push-component-build-context! []
  (swap! component-build-context conj ()))


(defn pop-component-build-context! []
  (-> (swap-vals! component-build-context rest)
      (ffirst)
      (seq)))


(defn- append-effect-context [[current-context & more] effect-context]
  (cons (conj current-context effect-context) more))


(defn add-effect-context [effect-context]
  (swap! component-build-context append-effect-context effect-context))