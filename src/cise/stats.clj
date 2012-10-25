(ns cise.stats)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; Simple STM example
;;
;; Compute the sum and number of points
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def rnum (ref 0))  ; count of data points
(def rsum (ref 0))  ; their sum     

(defn compute-sum [xs]     ; xs is a sequence of numbers
  ;; let introduces local bindings for n and s
  (let [n (count xs)       ; count of sequence
        s (reduce + xs)]   ; sum of sequence
    (dosync
     (commute rnum + n)    ; update num
     (commute rsum + s)))) ; update rsum

(defn create-thread [xs]
  ;; create a Thread to compute partition's sum
  (Thread. #(compute-sum xs))) 

(defn compute-sums [xs num-threads]
  (dosync (ref-set rnum 0)
          (ref-set rsum 0))
  (let [partition-size (/ (count xs) num-threads)
        ;; partition xs into a sequence of subsequences
        partitions (partition-all partition-size xs)
        ts (map create-thread partitions)]
    ;; the doseq iterates t across the sequence ts
    (doseq [t ts] (.start t))  ; start computing threads
    (doseq [t ts] (.join t)))) ; await their completion
                                
 (defn available-processors []
  (.availableProcessors (Runtime/getRuntime)))

(defn test-sum [n]
  (let [xs (repeatedly n rand)           ; randomly generated data, count n
        nt (+ 2 (available-processors))] ; number of threads
    (compute-sums xs nt)
    [@rnum @rsum (/ @rsum @rnum)]))      ;Result is a vector of three values