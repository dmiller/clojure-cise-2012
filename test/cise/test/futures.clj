(ns cise.test.futures
  (:use [clojure.test :only (deftest is)]
        [cise.futures :only (future-solver)]
        [cise.simple  :only (iter-solver-w-endpoints step-slice)]))

(deftest future-solver-solves
  (let [erhos (double-array (concat (repeat 51 0.0) [1.0] (repeat 51 0.0)))
        n (count erhos)
        num-iters 10]
    (doseq [i [1 2 3 4 5 10 20]]
      (let [result (future-solver erhos i num-iters)
            answer (iter-solver-w-endpoints step-slice erhos num-iters)]
        (is (= (seq answer) (seq result)))))))