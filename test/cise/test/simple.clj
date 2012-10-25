(ns cise.test.simple
  (:use [clojure.test :only (deftest is are)]
        [cise.poisson :only (step-slice-in-place)]
        [cise.simple  :only (step-slice iter-solver-w-endpoints iter-solver-in-place)]))


(def rhos (double-array [ 0.0   0.00  20   0.00  0.0 ]))
(def d0   (double-array [ 0.0   0.00   0   0.00  0.0 ]))
(def d1   (double-array [ 0.0   0.00  10   0.00  0.0 ]))
(def d2   (double-array [ 0.0   5.00  10   5.00  0.0 ]))
(def d3   (double-array [ 2.5   5.00  15   5.00  2.5 ]))
(def d4   (double-array [ 2.5   8.75  15   8.75  2.5 ]))

(defn- extend-double-array [arr]
  (double-array (concat [0.0] arr [0.0])))

(deftest extend-double-array-extends
  (is (= (seq [0.0 3.0 1.0 4.0 0.0])
         (seq (extend-double-array (double-array [3.0 1.0 4.0]))))))

(def erhos (extend-double-array rhos))
(def ed0 (extend-double-array d0))
(def ed1 (extend-double-array d1))
(def ed2 (extend-double-array d2))
(def ed3 (extend-double-array d3))
(def ed4 (extend-double-array d4))


(deftest step-slice-computes
  (are [in out]
       (let [dest (double-array (count in))]
         (step-slice in dest erhos 1 (dec (count erhos)))
         (is (= (seq dest) (seq out))))
       ed0 ed1
       ed1 ed2
       ed2 ed3
       ed3 ed4))


(deftest step-slice-in-place-computes
  (are [in out]
       (let [arr (double-array in)]
         (step-slice-in-place arr rhos 0 (count rhos) 0.0 0.0)
         (is (= (seq arr) (seq out))))
         d0 d1
         d1 d2
         d2 d3
         d3 d4))
       

(deftest simple-endpoint-solver-solves
  (is (= (seq (iter-solver-w-endpoints step-slice         erhos 4)) (seq ed4))))


(deftest simple-in-place-solver-solves
  (is (= (seq (iter-solver-in-place    step-slice-in-place rhos 4)) (seq d4))))


