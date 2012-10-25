(ns cise.sim)

;; Very simple simulation to demonstrate agents + STM
;; Torus-shaped grid of Refs of dimension dim x dim
;; Ref state = hash,
;;    :occupant => if an ant, a hash representing the ant state
;;                 any non-nil value indicates the cell is occupied
;;    :count => number of times an ant has moved into the location
;; Ant state = hash
;;    :moves => number of moves the ant has made
;;    :stays => number of times the direction picked was occupied, no move
;; One agent per ant
;; Agent state: location [x y]
;;    :loc => current location [x y]
;; Agent action:  move a random direction
;;    if the proposed cell is occupied, stay put (location same, :stays incremented)
;;    otherwise, location changes, :moves incremented 
;;

;; The world is dim x dim
(def dim 10)

;; The number of ants to create
(def num-ants 10)

;; how long an ant's thread should sleep (to slow down the simulation)
(def ant-sleep-ms 200) 

;; create the world
(defn create-world [m n]
  (into [] (map (fn [_] (into [] (map (fn [_] (ref {:count 0})) (range n))))
                (range m))))

;; the world to use in the simulation
(def world (create-world  dim dim))

;; Retrieve the cell value (Ref) at the indicated coordinates
(defn place [[x y]] (-> world (nth x) (nth y)))

;; the steps in eight directions an ant can take
(def dirs [[0 -1] [1 -1] [1 0] [1 1] [0 1] [-1 1] [-1 0] [-1 -1]])

;; Compute the cell one step in a random direction
(defn random-step [[x y]]
  (let [[dx dy] (rand-nth dirs)]
    [ (mod (+ x dx) dim) (mod (+ y dy) dim)]))

;; Update the world state to represent an ant moving from loc to new-loc
;; Assumes that new-loc is not occupied
;; Assumes a transaction is active
(defn move [loc new-loc]
  (let [p (place loc)
        new-p (place new-loc)
        ant (:occupant @p)]
    (alter p dissoc :occupant)
    (alter new-p assoc
           :occupant (assoc ant :moves (inc (:moves ant)))
           :count (inc (:count @new-p)))
    new-loc))

;; Update the world state to represent an ant staying in its current location
;; Assumes a transaction is active
(defn stay [loc]
  (let [p (place loc)
        ant (:occupant @p)]
    (alter p assoc :occupant (assoc ant :stays (inc (:stays ant))))
    loc))

;; Used for to serialize print requests
(def pr-agt (agent nil))
(defn prn-w-agt [& strs] (send pr-agt (fn [v strs] (apply prn strs) v) strs))

;; Re-def to false to stop all running ant agents on their next iteration
(def running true)

;; For debugging
(defn ant-id-at-loc [loc]
  (:id (:occupant @(place loc))))

;; The action for an agent tracking an ant.
;; Agent state is the location of the ant being tracked.
(defn behave [loc]
  (prn-w-agt  (ant-id-at-loc loc) ": Behaving at " loc)
  (Thread/sleep ant-sleep-ms)
  (let [new-loc (random-step loc)
        new-p (place new-loc)]
    (let [next-loc 
          (dosync
           (when running
             (send *agent* behave))
           (ensure new-p)
           (if (:occupant @new-p)
             (stay loc)
             (move loc new-loc)))]
      (prn-w-agt (ant-id-at-loc next-loc) ": From " loc " to " new-loc ": " next-loc)
      next-loc)))
      
  
;; Create an ant at the given location (and with the given id, for debugging and reporting)
(defn create-ant [loc id]
  (let [p (place loc)
        a {:moves 0 :stays 0 :id id}]
    (prn "Creating ant at " loc)
    (alter p assoc :occupant a)
    (agent loc)))

;;  Setup the world to run a simulation
(defn setup []
  (dosync
   (doall
    (map (fn [r] (ref-set r {:count 0})) (flatten world))))
  (dosync
   (doall
    (for [i (range num-ants)]
      (create-ant [(rand-int dim) (rand-int dim)] i)))))

;; initialize and run the simulation
(defn start[]
  (let [agents (setup)]
    (dorun (map #(send % behave) agents))))

;; return a list of the data on all ants int the world
(defn report []
  (filter identity (map (fn [r] (:occupant @r)) (flatten world))))

