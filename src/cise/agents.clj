(ns cise.agents
  (:use [cise.simple :only (step-slice)]
        [cise.poisson :only (slice-sizes)]))
  
(set! *warn-on-reflection* true)
  
;; Agent-based approach #1
;; Requires two copies of the array, current and next
;; Works one iteration at a time
;;
;; Agent state is a map with the following
;;  :agent-idx -- index of the agent for this slice, for debugging primarily
;;  :start-idx  -- index of first position of slice106
;;  :end-idx -- index of first position of next slice
;;  :src -- array with current values
;;  :dest -- array to put next iteration in
;;  :rhos -- yep
;;
;;  The src, dest, and rhos array could be passed in def'd vars,
;;    but I'd rather put them in the state.
;;
;; This will be a case-2 solution -- we will put the end values in position 0,n-1.
;; Assume we are handed the initial array with this satisfied.  Rhos also.
;; Thus the first slice will have :start-idx == 1
;; And  the last  slice will have   :end-idx == n-1
;;
;; We will swap src & dest at each iteration.

(defn step-agent1-slice [slice]
  (let [src (:src slice)
        dest (:dest slice)]
    (step-slice src
                dest
                (:rhos slice)
                (:start-idx slice)
                (:end-idx slice))
    (assoc slice :src dest :dest src)))

(defn agent1-solver [init-array rhos num-slices num-iters ]
  (let [n (count init-array)
        arr2 (double-array n)
        sizes (slice-sizes (- n 2) num-slices)
        start-indexes (reductions + 1 sizes)        
        make-slice (fn [i start-idx end-idx]
                     {:agt-idx i            ; for debugging
                      :start-idx start-idx
                      :end-idx end-idx
                      :src init-array
                      :dest arr2
                      :rhos rhos})
        slices (map make-slice (range num-slices) start-indexes (rest start-indexes))
        agts (map agent slices)]
    (dotimes [i num-iters]
      (doseq [a agts] (send a step-agent1-slice))
      (apply await agts))
    (:src @(first agts))))
  
