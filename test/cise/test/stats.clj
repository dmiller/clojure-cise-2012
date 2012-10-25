(ns cise.test.stats
  (:use [clojure.test :only (deftest is)]
        [cise.stats :only (compute-sums rnum rsum)]))


(defn- double= [^double x ^double y ^double epsilon]
  (let [scale (if ( or (zero? x) (zero? y)) 1.0 (Math/abs x))]
    (<= (Math/abs (- x y)) (* scale epsilon))))


(defn test-sum [xs nt]
  (let [s (reduce + xs)
        n (count xs)
        v (compute-sums xs nt)]
    (is (double= s @rsum 0.00001))
    (is (double= n @rnum 0.00001))))

(deftest computes-nominal-value-on-empty-sequence
  (test-sum [] 3))

(deftest computes-on-singleton-sequence
  (test-sum [10] 3))

(deftest computes-generally
  (test-sum (repeatedly 100 rand) 3))

  