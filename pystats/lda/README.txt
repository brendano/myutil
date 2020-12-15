LDA learning implementations in Cython for collapsed Gibbs sampling ("cgs")
and collapsed Variational Bayes variants ("cvb0"), including some attempts at
parallelization.
 - CGS: Steyvers and Griffiths 2004
 - CVB0: Asuncion et al. 2009

Written ~2012 by Brendan O'Connor
Checked in to github at https://github.com/brendano/myutil/
in response to https://twitter.com/srush_nlp/status/1338862250585559043

This code is actually standalone from the pystats/ stuff in the parent
directory, just done in the same programming language.

FILES:

lda.pyx - the most interesting file.  cgs_ser() has the most straightforward
CGS implementation

other files - bunch of random analyses

INTERNAL NOTE: written as part of "semdoc" and Data Analysis Project
and rejected ?EMNLP ~2012 submission

