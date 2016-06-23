from __future__ import division
import random
import numpy as np
import scipy.stats as ss
from rpy import r
import mcmc; reload(mcmc)
execfile("rutil.py")

# DCM simulation experiment

N = 10000
M = lambda: 2
K = 3
alpha = np.tile(.1 * 1/K, K)
#print "N=",N, "M=",M, "N*M=",(N*M), "alpha=",alpha
multis = np.random.dirichlet(alpha, N)
counts = [np.random.multinomial(M(), p) for p in multis]

pprint(uniq_c([tuple(x) for x in counts]))
