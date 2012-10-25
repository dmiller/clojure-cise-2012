(ns cise.test.sim
  (:use [clojure.test :only (deftest is)]
        [cise.sim :only (dim create-world place world dim)]))

(deftest create-world-has-right-dimensions
  (let [nr 5
        nc 7
        w (create-world nr nc)]
    (is (= (count w) nr))
    (is (every? #(= (count %) nc) w))))

(deftest created-world-has-refs
  (let [w (create-world 5 5)]
    (is (every? #(= (class %) clojure.lang.Ref) (flatten w)))))

(deftest created-world-has-zero-counts
  (let [w (create-world 5 5)]
    (is (every? #(zero? (:count @%)) (flatten w)))))

(deftest created-world-has-no-occupants
  (let [w (create-world 5 5)]
    (is (every? #(nil? (:occupant @%)) (flatten w)))))

(defn- set-coord [r i j]
  (alter r assoc :i i :j j))

(defn- set-coords [w]
  (dosync
   (doall
    (map-indexed (fn [i row]
                   (doall (map-indexed (fn [j r]
                                         (set-coord r i j))
                                       row)))
                 w))))

(deftest place-finds-refs
  (set-coords world)
    (dotimes [i 4]
      (dotimes [j 4]
        (let [r (place [i j])]
          (is (= (:i @r) i))
          (is (= (:j @r) j))))))
