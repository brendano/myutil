by Brendan O'Connor, http://brenocon.com

Java utilities for statistics/machinelearning and various supporting tools.
(Often intended for NLP applications, though not much NLP in this library.)
This needs a better name; currently it's "myutil",
https://github.com/brendano/myutil

The idea is to be a library of functions for well-known algorithms, as opposed
to a grand ML/NLP framework, because those are never as useful as one would
hope (in my experience at least).

This is under active development so any of it may be broken at any time.
If there are comments with a testing procedure, that may be a good sign.

# Stuff in here

Math/stats/opt things:
* Arr.java: lots of array/matrix math and manipulation utilities.  Unlike Colt
  or Jama, uses the more natural Java arrays and array-of-arrays
  representations.  Also includes all Java standard library methods, because I
  can't remember which class is which.
* MCMC.java: generic MCMC algorithms: Slice sampling, Metropolis-Hastings
* LibLBFGS: a port of LibLBFGS to Java.  Seems to behave similarly as
  Stanford's OWLQN port, but it's more efficient.
* FastRandom: a random number generator that's 10 times faster than the Java
  standard library's.
* GaussianInference: conjugate posterior inference (exact and sampling) for
  Gaussian scalars, linear regression, and DLM's (Kalman filter, smoother,
  FFBS)
* MVNormal2: linear algebra inference and samplers for multivariate normals
  (ported from Mallet)
* LNInference: logistic normal MAP and samplers
* ChainInfer.java: discrete chain inference: Viterbi, forward-backward, FFBS
* Online algorithms: Vitter reservoir sampling (ReservoirSampler), and Welford
  running mean/variance (OnlineNormal1d(Weighted))
* Util.java: some other math/stats functions

Non-math-y things:
* ThreadUtil: basically ThreadPool wrappers for divide-and-conquer workloads
* U.java: printing utilities (mostly)
* BasicFileIO: IO utilities
* Vocabulary: feature name/numberization (I'd love to get a better/more
  efficient one here)
* Timer: timings for large sections of your program
* JsonUtil: very simple wrappers for Jackson

NLP things:
* corenlp/: runners for Stanford CoreNLP that work with JSON or XML-based
  one-line-per-document formats.  Once you have thousands of documents, these
  formats are typically much faster to deal with than CoreNLP's
  one-document-per-file strategy.  They're more Hadoop-friendly too.  To use
  these, need to drop in the model file (stanford-corenlp-3.2.0-models.jar)
  into lib/stanford_extras

Example models:
* In the root package, example implementation of CGS LDA. When working
  on a related model, I copy-and-paste one to get started then hack it up.
  scripts/ has viewers for it.

# Licenses

Let's say new code is GPL version 2.
Note there's code from other libraries inside here too,
like JAMA and LibLBFGS and the Java SDK, which have their own licenses.

