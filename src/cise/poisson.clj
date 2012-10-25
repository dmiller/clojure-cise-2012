(ns cise.poisson)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;;  Agent example
;;
;; Compute a solution to 1D Poisson problem.
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(set! *warn-on-reflection* true)

;; Compute the next value at a location (unhinted version)
(defn next-value-no-hint  [rho left right]
  (* 0.5 (+ (+ left right) rho)))

;; Compute the next value at a location (unhinted version)
(defn next-value
  ^double [^double rho ^double left ^double right]
  (* 0.5 (+ (+ left right) rho)))

;; Update the slice [start-index, end-index) in array arr
(defn step-slice-in-place [^doubles arr ^doubles rhos start-index end-index lval rval]
  (let [last-idx (unchecked-dec (int end-index))]
    (loop [old-val (double lval)
           i (int start-index)]
      (if (< i last-idx)
        (let [cur-val (aget arr i)]
          (aset arr i (next-value (aget rhos i) old-val (aget arr (inc i))))
          (recur cur-val (unchecked-inc i)))
        (aset arr last-idx (next-value (aget rhos i) old-val (double rval)))))))

;; Agent state is a map with the following:
;;   :agent-idx -- index of the agent for this slice
;;   :start-idx -- index of first position of slice
;;   :end-idx -- index of first position of next slice
;;   :niters -- the number of iterations to perform
;;   :iter -- current iteration number
;;   :lq -- queue of values coming from the slice to the left
;;   :rq -- queue of values coming from the slice to the right
;;
;;   Careful analysis shows that no slice can be more than one ahead of any other slice,
;;     so a queue never gets larger than 2.  But it can be two.


(def agents (ref nil))   ; a vector of agents 
(def rhos   (ref nil))   ; an array of doubles
(def arr    (ref nil))   ; an array of doubles, the work vector
(def latch  (ref nil))   ; a countdown latch


(defn agt-at [i]
  (get @agents i))

(defn left-agt [i]
  (agt-at (dec i)))

(defn right-agt [i]
  (agt-at (inc i)))


(defn step-slice-data [slice lval rval]
  ;; (:key map) is equivalent to (get map :key)
  (step-slice-in-place @arr @rhos (:start-idx slice) (:end-idx slice) lval rval))

(declare update-slice)

(defn enqueue-left [slice value]
  (update-slice (assoc slice :lq (conj (:lq slice) value))))

(defn enqueue-right [slice value]
  (update-slice (assoc slice :rq (conj (:rq slice) value))))

(defn notify-neighbors [i lval rval]
  (let [lagt (left-agt i)
        ragt (right-agt i)
        agt (agt-at i)]
    (if (nil? lagt)
      (send agt enqueue-left 0.0)
      (send lagt enqueue-right lval))
    (if (nil? ragt)
      (send agt enqueue-right 0.0)
      (send ragt enqueue-left rval))))


(defn update-slice [slice]
 (let [{:keys [lq rq iter niters agt-idx start-idx end-idx]} slice
       lval (peek lq)
       rval (peek rq)]
   (if (or (nil? lval) (nil? rval) (>= iter niters))
     slice
     (do
       (step-slice-data slice lval rval)
       (notify-neighbors agt-idx
                         (aget ^doubles @arr start-idx)
                         (aget ^doubles @arr (dec end-idx)))
       (when (= (inc iter) niters)
         (swap! @latch dec))
       (assoc slice :iter (inc iter) :lq (pop lq) :rq (pop rq))))))


(defn slice-sizes [n num-slices]
  (let [r (rem n num-slices)
        d (quot n num-slices)]
    (concat (repeat r (inc d)) (repeat (- num-slices r) d))))

 
(defn create-agents [nvals nslices niters]
  (let [start-indexes (reductions + 0 (slice-sizes nvals nslices))
        q0 (conj clojure.lang.PersistentQueue/EMPTY 0.0)           ;; immutable, so all can share
        make-slice (fn [i start-idx end-idx]
                     {:agt-idx i
                      :start-idx start-idx
                      :end-idx end-idx
                      :niters niters
                      :iter 0
                      :lq q0
                      :rq q0})
        slices (map make-slice (range nvals) start-indexes (rest start-indexes))]
    (into [] (map agent slices))))


(defn start-agents [agts]
  (doseq [a agts]
    (send a update-slice)))

(defn agent-solver [rs nslices niters]
  (let [nvals (count rs)
        agts (create-agents nvals nslices niters)
        p (promise)
        a-watch (fn [key a old-val new-val]
                  (when (zero? new-val)
                    (deliver p true)))]
    (dosync
     (ref-set agents agts)
     (ref-set arr (double-array nvals))
     (ref-set rhos rs)
     (ref-set latch (atom nslices)))    
    (add-watch @latch :zero a-watch)
    (start-agents agts)
    @p
    @arr))
