(ns cise.test.agents
  (:use [clojure.test :only (deftest is)]
        [cise.agents  :only (step-agent1-slice agent1-solver)]
        [cise.simple  :only (iter-solver-w-endpoints step-slice)]))


;;; Tests for first agent-based solver

(defn- make-agent1-slice [src dest rhos start-idx end-idx]
  {:src src
   :dest dest
   :rhos rhos
   :start-idx start-idx
   :end-idx end-idx})

(defn- test-step-agent1-slice
  [src rhos start-idx end-idx answer]
  (let [dest (double-array src)
        slice (make-agent1-slice src dest rhos start-idx end-idx)
        slice2 (step-agent1-slice slice)]
    (is (identical? dest (:src slice2)))
    (is (identical? src (:dest slice2)))
    (is (= (seq dest) (seq answer)))))

(deftest step-slice-steps
  (test-step-agent1-slice
   (double-array [ 70.0  20.0 100.0  40.0  10.0 100.0  50.0 ]) 
   (double-array [  0.0  20.0  30.0  20.0  10.0  20.0  30.0 ])
   1
   6
   (double-array [ 70.0  95.0  45.0  65.0  75.0  40.0  50.0])))


(deftest agent1-solver-solves
  (let [erhos (double-array (concat (repeat 51 0.0) [1000.0] (repeat 51 0.0)))
        n (count erhos)
        num-iters 10]
    (doseq [i [1 2 3 4 5 10 20]]
      (let [esrc (double-array n)
            result (agent1-solver esrc erhos i num-iters)
            answer (iter-solver-w-endpoints step-slice erhos num-iters)]
        (is (= (seq answer) (seq result)))))))




