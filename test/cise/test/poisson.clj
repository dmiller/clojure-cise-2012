(ns cise.test.poisson
  (:use [clojure.test :only (deftest is are)]
        [cise.poisson :only (next-value slice-sizes step-slice-in-place
                             agents agt-at left-agt right-agt agent-solver)]))

(deftest next-value-computes
  (is (= (next-value 0.0 0.0 0.0) 0.0))
  (is (= (next-value 6.0 2.0 4.0) 6.0)))


(deftest slice-sizes-computes
  (are [n nslices result] (= (slice-sizes n nslices) result)
       32 4 [8 8 8 8]
       34 4 [9 9 8 8]
       142 7 [21 21 20 20 20 20 20]))

(def rhos (double-array [ 0.0   0.00  20   0.00  0.0 ]))
(def d0   (double-array [ 0.0   0.00   0   0.00  0.0 ]))
(def d1   (double-array [ 0.0   0.00  10   0.00  0.0 ]))
(def d2   (double-array [ 0.0   5.00  10   5.00  0.0 ]))
(def d3   (double-array [ 2.5   5.00  15   5.00  2.5 ]))
(def d4   (double-array [ 2.5   8.75  15   8.75  2.5 ]))


(deftest step-slice-in-place-computes
  (are [in out]
       (let [arr (double-array in)]
         (step-slice-in-place arr rhos 0 (count rhos) 0.0 0.0)
         (is (= (seq arr) (seq out))))
         d0 d1
         d1 d2
         d2 d3
         d3 d4))

(deftest find-agents
  (let [arr (int-array [0 1 2 3 4])]
    (dosync (ref-set agents arr))
    (dotimes [i 5]
      (is (= (agt-at i) i)))
    (dotimes [i 4]
      (is (= (left-agt (inc i)) i)))
    (is (nil? (left-agt 0)))
    (dotimes [i 4]
      (is (= (right-agt i) (inc i))))
    (is (nil? (right-agt 4)))))


(deftest agent-solver-solves
  (is (= (seq (agent-solver rhos 1 4))
         (seq d4)))
  (is (= (seq (agent-solver rhos 2 4))
         (seq d4))))