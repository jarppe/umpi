(ns umpi.effect-context
  (:require [clojure.set :as set]
            [umpi.render.component-context :as cc]))


;;
;; Context for building effects:
;;


(defonce effect-build-context (atom nil))


(defn push-effect-build-context! []
  (swap! effect-build-context conj #{}))


(defn pop-effect-build-context! []
  (-> (swap-vals! effect-build-context rest)
      (ffirst)))


(defn- add-signal-ref [[current-context & more] signal]
  (when current-context
    (cons (conj current-context signal) more)))


(defn signal-referenced [signal]
  (swap! effect-build-context add-signal-ref signal))

;
; EffectContext
;
; Effect context captures a context for executing effects. It holds a
; set of signals (each signal being an atom) the effect execution 
; references. This set is updated each time effect is executed.
; 
; When effect executuion references new signals, effect context registers 
; itself as a watch listener to those signals. If most recent effect execution 
; did not reference a signal it had previously referenced, the effect context 
; removes itself as wather from those signals.
;
; When a signal that is referenced bu must recent effect executiuon has it's value
; updated, it notifies the effect context by calling effect context. Effect context
; batches the changes to a batch that is processed on next JS event loop lap 
; (using `(js/setTimeout batch-fn 0)`).
;
; The batch notifications are executed by calling the effect. The effect execution
; is done within an effect build context to capture signal references.
;
; Effect context is added to component context. This allows component context to
; notify effect context about component unmount. At this time effect context removes
; itself as wather from all signals.
;
; Effect context is implemented by type EffectContext.
;


(defprotocol IEffectContext
  (execute [this] "Execute associated context and gather signal references")
  (close [this] "Remove this effect context from referenced signals"))


(deftype EffectContext [effect-fn
                        ^:mutable signals
                        ^{:tag     boolean
                          :mutable true} batching?
                        effect-context-meta]
  IEffectContext
  (execute [this]
    (set! (.-batching? this) false)
    (push-effect-build-context!)
    (try
      (effect-fn)
      (finally
        (let [used-signals (pop-effect-build-context!)
              old-signals  (set/difference signals used-signals)
              new-signals  (set/difference used-signals signals)]
          (doseq [old-signal old-signals]
            (remove-watch old-signal this))
          (doseq [new-signal new-signals]
            (add-watch new-signal this this))
          (set! (.-signals this) used-signals)))))
  (close [this]
    (js/console.log "EffectContext: close")
    (doseq [signal signals]
      (remove-watch signal this))
    (set! (.-signals this) nil)
    (set! (.-batching? this) false))

  IFn
  (-invoke [this _key _signal _old-value _new-value]
    (when-not batching?
      (set! (.-batching? this) true)
      (js/setTimeout (fn [] (execute this)) 0)))

  IHash
  (-hash [this]
    (goog/getUid this))

  INamed
  (-name [_this]
    (:name effect-context-meta))

  IMeta
  (-meta [_this]
    effect-context-meta)

  Object
  (equiv [this other]
    (-equiv this other))
  (toString [this]
    (str "#object[umpi.effect-context.EffectContext[{:name \"" (name this) "\"}]]"))

  IEquiv
  (-equiv [o other]
    (identical? o other)))


(defn ^EffectContext effect-context
  (^EffectContext [effect-fn] (effect-context effect-fn nil))
  (^EffectContext [effect-fn effect-context-meta]
   (let [effect-context (EffectContext. effect-fn nil false effect-context-meta)
         response       (execute effect-context)]
     (when-not (empty? (.-signals effect-context))
       (cc/add-effect-context effect-context))
     response)))

