(ns cise.futures
  (:use [cise.poisson :only (slice-sizes)]
        [cise.simple :only (step-slice)]))

(set! *warn-on-reflection* true)

;; iterative solves 1-D Poisson problem
;; rhos is a (Java) array of doubles.
;; requires dummy values in first and last position
;; Does num-iters iteration to solve
;; Number of threads = num-slices
;; On each iteration
;;    create a future on each slice
;;     Wait for each future to be computed 
;;    Swap source and destination arrays for next iteration

(defn future-solver [rhos num-slices num-iters]
  (let [n (count rhos)
        dest1 (double-array n)
        dest2 (double-array n)
        sizes (slice-sizes (- n 2) num-slices)
        start-indexes (reductions + 1 sizes)
        end-indexes (rest start-indexes)]
    (loop [i 0 src dest1 dest dest2]
           (if (< i num-iters)
             (let [fs (doall (map #(future (step-slice src dest rhos %1 %2)) start-indexes end-indexes))]
               (doseq [f fs] @f)
               (recur (inc i) dest src))
               src))))