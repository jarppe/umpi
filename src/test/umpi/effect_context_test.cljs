(ns umpi.effect-context-test
  (:require [clojure.test :as test :refer [deftest testing is async]]
            [match.core :refer [matches?]]
            [umpi.effect-context :as ec]
            [umpi.signal :as sig]))


(test/use-fixtures :each
  {:before #(async done
                   (reset! ec/effect-build-context nil)
                   (done))})


#_(deftest signal-build-context-test
    (testing "without build context signal-referenced is nop"
      (ec/signal-referenced 'some-signal)
      (is (nil? @ec/effect-build-context))
      (is (nil? (ec/pop-effect-build-context!))))
    (testing "context captures references"
      (ec/push-effect-build-context!)
      (ec/signal-referenced 'some-signal)
      (is (= #{'some-signal}
             (ec/pop-effect-build-context!))))
    (testing "context captures are scoped"
      (ec/push-effect-build-context!)
      (ec/signal-referenced 'some-signal-1)
      (ec/push-effect-build-context!)
      (ec/signal-referenced 'some-signal-2)
      (is (= #{'some-signal-2}
             (ec/pop-effect-build-context!)))
      (is (= #{'some-signal-1}
             (ec/pop-effect-build-context!)))))


#_(deftest effect-context-simple-test
    (async done
           (let [result (atom [])
                 s1     (sig/signal 1)
                 f      (fn [] (swap! result conj @s1))
                 ec     (ec/effect-context f)]
             (is (= [1] @result))
             (swap! s1 inc)
             (js/setTimeout (fn []
                              (is (= [1 2] @result))
                              (done))
                            0))))


#_(deftest effect-context-no-refs-test
    (is (nil? (ec/effect-context (fn [])))))


#_(deftest effect-context-batching-test
    (async done
           (let [result (atom [])
                 s1     (sig/signal 1)
                 f      (fn [] (swap! result conj @s1))
                 ec     (ec/effect-context f)]
             (is (= [1] @result))
             (swap! s1 inc)
             (swap! s1 inc)
             (swap! s1 inc)
             (js/setTimeout (fn []
                              (is (= [1 4] @result))
                              (done))
                            0))))


#_(deftest effect-context-updates-signal-watches-test
    (async done
           (let [result (atom [])
                 s1     (sig/signal 0)
                 s2     (sig/signal 's2)
                 s3     (sig/signal 's3)
                 f      (fn []
                          (swap! result conj
                                 (case @s1
                                   0 [0]
                                   1 [1 @s2]
                                   2 [2 @s3])))
                 ec     (ec/effect-context f)]

             (is (= [[0]] @result))
             (is (= #{s1} (.-signals ec)))
             (is (= {ec ec} (.-watches s1)))
             (is (= {} (.-watches s2)))
             (is (= {} (.-watches s3)))

             (swap! s1 inc)

             (js/setTimeout
              (fn []
                (is (= [[0] [1 's2]] @result))
                (is (= #{s1 s2} (.-signals ec)))
                (is (= {ec ec} (.-watches s1)))
                (is (= {ec ec} (.-watches s2)))
                (is (= {} (.-watches s3)))

                (swap! s1 inc)

                (js/setTimeout
                 (fn []
                   (is (= [[0] [1 's2] [2 's3]] @result))
                   (is (= #{s1 s3} (.-signals ec)))
                   (is (= {ec ec} (.-watches s1)))
                   (is (= {} (.-watches s2)))
                   (is (= {ec ec} (.-watches s3)))
                   (done))
                 0))
              0))))
