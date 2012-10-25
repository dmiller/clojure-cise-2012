# Clojure examples for Computing in Science and Engineering article, 2012

Wherein we investigate parallelism in Clojure to pique the interests of Fortran/C++ coders.

## Installation

We recommend installing Leiningen. Leiningen will help install Clojure and it make it easy to run tests and start a REPL. For more information on Leiningen, including installation instructions,
go to [http://github.com/technomancy/leiningen].

Buttons to download a .zip or a .tar.gz of the contents can be found [the download page](https://github.com/dmiller/clojure-cise-2012/downloads) of our Github repository.  Download and unzip to somewhere.  Start a terminal/cmd/whatever you call it and cd to where you unzipped the code.

To run the tests, 

   $ lein test

To start a REPL,

   $ lein repl

Once you have the REPL going:

    user=> (load "cise/core")
    nil
    user=> (in-ns 'cise.core)
    #<Namespace cise.core>
    cise.core=> 

To test the _stats_ program:

    cise.core=> (test-sum 1000)
    [1000 500.34570874019175 0.5003457087401918]

To test the _poisson_ program:

    cise.core=> (def a (double-array 100))  ; double array, size 100, all 0  
    #'cise.core/a
     
    cise.core=> (aset a 50 1.0)             ; let's get at least one non-zero value in there                                  
    1.0
     
    cise.core=> (def r (agent-solver a 5 200))    ;  5 slices, 200 iterations
    #'cise.core/r
     
    cise.core=> (aget r 50)
    5.634847900925116
     
    cise.core=> (aget r 49)
    5.163022140429744

Time to get serious:

    cise.core=> (def a (double-array 1000000))
    #'cise.core/a
     
    cise.core=> (aset a 500000 1.0)
    1.0
     
    cise.core=> (time (agent-solver a 1 1000))  ; 1,000,000 points, 1 thread, 1000 iterations
    "Elapsed time: 6271.475 msecs"
     
    cise.core=> (time (agent-solver a 2 1000))  ; 2 threads
    "Elapsed time: 4152.594 msecs"
     
    cise.core=> (time (agent-solver a 4 1000))
    "Elapsed time: 3212.367 msecs"
     
    cise.core=> (time (agent-solver a 5 1000))
    "Elapsed time: 3272.189 msecs"
     
     cise.core=> (time (agent-solver a 6 1000))
     "Elapsed time: 3130.349 msecs"
      
     cise.core=> (time (agent-solver a 7 1000))
     "Elapsed time: 3124.874 msecs"
      
     cise.core=> (time (agent-solver a 8 1000))
     "Elapsed time: 3130.097 msecs"
       
     cise.core=> (time (agent-solver a 16 1000))
     "Elapsed time: 3132.442 msecs"

On a four-core machine, max speedup occurs at around 6 or 7 slices -- NumCores+2 is not unexpected.  The thread pooler throttles us from there.

To run the _sim_ program, it is best to move to the cise.sim namespace.

    cise.core=> (in-ns 'cise.sim)
    #<Namespace cise.sim>

We have left in some print statements so that you can see the ants behaving.  After you start the simulation running, you stop it by evaluating

    (def running false)

However, you will be trying to type that while the output is scrolling, so good luck!   After you've typed, all running agents will do one more iteration and shut down. You can then ask for report on the status of all the ants.

    cise.sim=> (start)
    "Creating ant at " [4 5]
    "Creating ant at " [5 1]
    "Creating ant at " [3 2]
    ...    
    nil

    cise.sim=> 
    5 ": Behaving at " [7 4]
    0 ": Behaving at " [4 5]
    4 ": Behaving at " [9 0]
    5 ": From " [7 4] " to " [7 5] ": " [7 5]
    4 ": From " [9 0] " to " [0 0] ": " [0 0]
    0 ": From " [4 5] " to " [3 4] ": " [3 4]
    2 ": Behaving at " [3 2]
    2 ": From " [3 2] " to " [2 3] ": " [2 3]
    3 ": Behaving at " [5 1]
    3 ": From " [5 1] " to " [6 0] ": " [6 0]
    6 ": From " [9 4] " to " [9 3] ": " [9 3]
    7 ": From " [0 0] " to " [9 1] ": " [9 1]
    ...

The number on the left of each line is the ant id.
Somewhere along the way, I typed (def running false), and eventually output ceased.

    cise.sim=> 
    cise.sim=> (report)
    ({:moves 21, :stays 2, :id 2} {:moves 18, :stays 4, :id 8} 
     {:moves 23, :stays 0, :id 6} {:moves 22, :stays 1, :id 3} 
     ...)


Type Ctrl-C to get out of the REPL.

## License

Copyright (C) 2012 Marty Kalin and David Miller

Distributed under the Eclipse Public License, the same as Clojure.

