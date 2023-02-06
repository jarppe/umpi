(ns umpi.render
  (:require [umpi.render.core :as rc]
            [umpi.render.impl]))


(defn render [data]
  (rc/-render data))
