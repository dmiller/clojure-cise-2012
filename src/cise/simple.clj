(ns cise.simple
  (:use [cise poisson]))

(set! *warn-on-reflection* true)


;;;  There are several considerations for writing code to generate one iteration from the previous.
;;;  Assuming here we are working with arrays or slices of arrays.
;;;
;;;  In-place or not:
;;;
;;;  Is the next iteration computed into another array or into the same array.
;;;  For the current problem, an in-place solution is possible by keeping track
;;;    of the previous value of the a[i-1]-th position.
;;;
;;;
;;;  Dealing with endpoints.
;;;  We need to compute a[i-1] and a[i+1], raising the possibility of going off the end of the array.
;;;  Several solutions:
;;;     Solution 1: special case the 0th and (n-1)st position calculations.
;;;     Solution 2: expand the array to have the default values in the first
;;;                 and last position (add positions)
;;;   I'm sure you can think of other solutions
;;;
;;;  I wrote some solution 1 steppers, but they were always just a little slower than solution 1.
;;;  So I got rid of them.
;;;
;;;  I played around with several variations of the above in order to see what
;;;  would be easiest to explain.
;;;
;;;  I also used some of the really simple ones to help test the others.
;;;
;;;  Here are some I kept around.
;;   The code used in the article is in cise.poisson.


;;; Type 2 solution: (add positions), not in-place

(defn step-slice [src dest rhos start-idx end-idx]
  (loop [i (int start-idx)]
    (when (< i (int end-idx))
      (aset ^doubles dest
            i
            (next-value (aget ^doubles rhos i)
                        (aget ^doubles src (unchecked-dec i))
                        (aget ^doubles src (unchecked-inc i))))
      (recur (unchecked-inc i)))))


(defn iter-solver-w-endpoints [stepper rhos num-iters]
  ;; Assume rhos has endpoints added
  (let [n (count rhos)
        a0 (double-array n)
        a1 (double-array n)]
  (loop [i 0 src a0 dest a1]
    (if (< i num-iters)
      (do (stepper src dest rhos 1 (dec n))
          (recur (inc i) dest src))
      src))))


(defn iter-solver-in-place [stepper rhos num-iters]
  (let [n (count rhos)
        arr (double-array n)]
    (dotimes [i num-iters]
      (stepper arr rhos 0 n 0.0 0.0))
    arr))


(defn time-next-slices
  [num-points num-iters]
  (let [src (double-array num-points)
        dest (double-array num-points)
        rhos (double-array num-points)]
    (time (dotimes [i num-iters] (step-slice          src dest rhos 1 (dec num-points))))
    (time (dotimes [i num-iters] (step-slice-in-place src      rhos 0 num-points 0.0 0.0)))
    ))


(defn time-iter-loop-solver
  [num-points num-iters]
  (let [rhos (double-array (concat (repeat (/ num-points 2) 0.0) [1.0] (repeat (/ num-points 2) 0.0)))]
    (time (iter-solver-w-endpoints step-slice           rhos num-iters))
    (time (iter-solver-in-place step-slice-in-place     rhos num-iters))))
    
