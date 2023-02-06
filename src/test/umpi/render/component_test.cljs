(ns umpi.render.component-test
  (:require [clojure.test :as test :refer [deftest is are]]
            [match.core :refer [matches?]]
            [umpi.mock-dom :as m]
            [umpi.render.component :as component]))


(m/init)


(deftest normalize-render-vector-test
  (are [form expect] (= (component/normalize-render-vector form) expect)
    [:foo] [:foo nil nil]
    [:foo {:x 1}] [:foo {:x 1} nil]
    [:foo 1] [:foo nil '(1)]
    [:foo 1 2] [:foo nil '(1 2)]
    [:foo {:x 1} 1] [:foo {:x 1} '(1)]
    [:foo {:x 1} 1 2] [:foo {:x 1} '(1 2)]))



(deftest render-vector-fragment-test
  (is (matches? (ex-info "fragment elements can't have props" {})
                (component/render-vector-fragment {} nil)))
  (is (some? (component/render-vector-fragment nil nil))))
