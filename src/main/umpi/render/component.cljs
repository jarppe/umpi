(ns umpi.render.component
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [goog.object :as go]
            [umpi.render.core :as r]
            [umpi.effect-context :as ec]
            [umpi.render.component-context :as cc]
            [umpi.signal :as signal]))


(defn- normalize-render-vector [data]
  (let [tag      (first data)
        props?   (map? (second data))
        props    (if props? (second data) nil)
        children (if props? (seq (drop 2 data)) (next data))]
    [tag props children]))


(defn- set-class-list [^js e classes]
  ; TODO: should we cache classes somewhere?
  ; TODO: handle case where class value is signal
  (let [class-list      (go/get e "classList")
        current-classes (set (.entries class-list))
        classes         (->> classes
                             (map (fn [class]
                                    (if (keyword? class) (name class) (str class))))
                             (set))
        remove-classes  (set/difference current-classes classes)
        new-classes     (set/difference classes current-classes)]
    (doseq [remove-class remove-classes]
      (.remove class-list remove-class))
    (doseq [new-class new-classes]
      (.add class-list new-class))))


(defn- toggle-class [^js e class-name activate?]
  (let [class-list (go/get e "classList")]
    (if activate?
      (.add class-list class-name)
      (.remove class-list class-name))))


(defn- set-class [^js e class]
  (cond
    (nil? class) (set-class-list e [])
    (string? class) (set-class-list e [class])
    (map? class) (set-class-list e (keep (fn [[k v]]
                                           (if (signal/signal? v)
                                             (let [class-name (name k)]
                                               (ec/effect-context (fn [] (toggle-class e class-name @v)))
                                               nil)
                                             (when v k)))
                                         class))
    (sequential? class) (set-class-list e class)
    (signal/signal? class) (ec/effect-context (fn [] (set-class e @class)))
    :else (throw (ex-info (str "invalid value for element class: " (pr-str class)) {})))
  e)


(defn- set-style [^js e style]
  ; TODO: Should we cache styles?
  ; TODO: Is this the most efficient way?
  (if (signal/signal? style)
    (ec/effect-context (fn [] (set-style e @style)))
    (let [css    (go/get e "style")
          styles (reduce-kv (fn [acc k v]
                              (if (signal/signal? v)
                                (update acc :dynamic assoc (name k) v)
                                (update acc :static str (name k) ":" (str v) ";")))
                            {:static  ""
                             :dynamic {}}
                            style)]
      (when-let [static-style (:static styles)]
        (go/set css "cssText" static-style))
      (doseq [[style-name signal] (:dynamic styles)]
        (ec/effect-context (fn [] (go/set css style-name @signal))))))
  e)


(defn- add-listener [^js e k listener]
  (let [event-name (subs (name k) 3)
        opts       (->> (select-keys (meta listener) [:capture :once :passive])
                        (merge {:capture false
                                :once    false
                                :passive false})
                        (clj->js))]
    (.addEventListener e event-name listener opts))
  e)


(defn- set-attr [^js e k v]
  (case k
    :class (set-class e v)
    :style (set-style e v)
    (cond
      (str/starts-with? (name k) "on-") (add-listener e k v)
      (signal/signal? v) (ec/effect-context (fn [] (set-attr e k @v)))
      :else (.setAttribute e (name k) (str v))))
  e)


(defn- set-on-unmount [e effect-contexts]
  (when effect-contexts
    (go/set e "on-unmount" (fn []
                             (doseq [effect-context effect-contexts]
                               (ec/close effect-context)))))
  e)


(defn- render-vector-fragment [props children]
  (when props
    (throw (ex-info "fragment elements can't have props" {})))
  (let [e (js/document.createDocumentFragment)]
    (doseq [c     children
            :let  [ce (r/-render c)]
            :when (some? ce)]
      (.appendChild e ce))
    e))


(defn- render-vector-keyword [tag props children]
  (let [e (js/document.createElement (name tag))]
    (cc/push-component-build-context!)
    (doseq [[k v] props]
      (set-attr e k v))
    (doseq [c     children
            :let  [ce (r/-render c)]
            :when (some? ce)]
      (.appendChild e ce))
    (set-on-unmount e (cc/pop-component-build-context!))
    e))


(defn- render-vector-fn [tag props children]
  (cc/push-component-build-context!)
  (let [e (ec/effect-context (fn []
                               (r/-render (tag (assoc props :children children)))))]
    (set-on-unmount e (cc/pop-component-build-context!))))


(defn render-component [data]
  (let [[tag props children] (normalize-render-vector data)]
    (cond
      (= tag :<>) (render-vector-fragment props children)
      (keyword? tag) (render-vector-keyword tag props children)
      (fn? tag) (render-vector-fn tag props children)
      :else (throw (ex-info "unsupported component form" {:tag tag})))))

