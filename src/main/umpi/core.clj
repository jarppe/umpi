(ns umpi.core)


(defmacro with-effect [& body]
  `(umpi.effect-context/effect-context
    (fn []
      ~@body)))


(comment
  (macroexpand-1 '(with-effect (println "foo") (+ 1 2))))