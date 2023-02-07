(ns umpi.render.component-test
  (:require [clojure.test :as test :refer [deftest is are testing async]]
            [match.core :refer [matches?]]
            [umpi.mock-dom :as m]
            [umpi.render.component :as component]
            [umpi.signal :as signal]))


(m/init)


(def normalize-render-vector #'component/normalize-render-vector)


(deftest normalize-render-vector-test
  (are [form expect] (= (normalize-render-vector form) expect)
    [:foo] [:foo nil nil]
    [:foo {:x 1}] [:foo {:x 1} nil]
    [:foo 1] [:foo nil '(1)]
    [:foo 1 2] [:foo nil '(1 2)]
    [:foo {:x 1} 1] [:foo {:x 1} '(1)]
    [:foo {:x 1} 1 2] [:foo {:x 1} '(1 2)]))


(def set-class #'component/set-class)


(deftest set-class-test
  (testing "Seting nil does not add classes"
    (let [e (m/mock-element "div")]
      (set-class e nil)
      (is (= #{} (-> e .-classList .entries set)))))
  (testing "Seting nil removes existing classes"
    (let [e (m/mock-element "div")]
      (set-class e "foo")
      (set-class e nil)
      (is (= #{} (-> e .-classList .entries set)))))
  (testing "Seting static class"
    (let [e (m/mock-element "div")]
      (set-class e "foo")
      (is (= #{"foo"} (-> e .-classList .entries set)))))
  (testing "Seting static class list"
    (let [e (m/mock-element "div")]
      (set-class e ["foo" "bar"])
      (is (= #{"foo" "bar"} (-> e .-classList .entries set)))))
  (testing "Adding static class list"
    (let [e (m/mock-element "div")]
      (set-class e "foo")
      (set-class e ["foo" "bar"])
      (is (= #{"foo" "bar"} (-> e .-classList .entries set)))))
  (testing "Removing static class list"
    (let [e (m/mock-element "div")]
      (set-class e ["foo" "bar"])
      (set-class e ["bar"])
      (is (= #{"bar"} (-> e .-classList .entries set)))))
  (testing "Using map to set static classes"
    (let [e (m/mock-element "div")]
      (set-class e {"foo" true
                    "bar" false
                    "boz" true})
      (is (= #{"foo" "boz"} (-> e .-classList .entries set))))))


(deftest set-class-dynamic-class-test
  (async done
         (let [e (m/mock-element "div")
               s (signal/signal "foo")]
           (set-class e s)
           (is (= #{"foo"} (-> e .-classList .entries set)))
           (reset! s "bar")
           (js/setTimeout (fn []
                            (is (= #{"bar"} (-> e .-classList .entries set)))
                            (done))))))


(deftest set-class-dynamic-class-list-test
  (async done
         (let [e (m/mock-element "div")
               s (signal/signal "foo")]
           (set-class e s)
           (is (= #{"foo"} (-> e .-classList .entries set)))
           (reset! s ["bar" "boz"])
           (js/setTimeout (fn []
                            (is (= #{"bar" "boz"} (-> e .-classList .entries set)))
                            (done))))))


(deftest set-class-map-with-dynamic-class-test
  (async done
         (let [e (m/mock-element "div")
               s (signal/signal false)]
           (set-class e {:foo true
                         :bar s})
           (is (= #{"foo"} (-> e .-classList .entries set)))
           (reset! s true)
           (js/setTimeout (fn []
                            (is (= #{"foo" "bar"} (-> e .-classList .entries set)))
                            (done))))))



(def render-vector-fragment #'component/render-vector-fragment)


(deftest render-vector-fragment-test
  (is (matches? (ex-info "fragment elements can't have props" {})
                (render-vector-fragment {} nil)))
  (is (some? (render-vector-fragment nil nil))))
