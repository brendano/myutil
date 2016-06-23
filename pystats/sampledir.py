from __future__ import division
import random
import numpy as np
import scipy.stats as ss
from rpy import r
import mcmc; reload(mcmc)
execfile("rutil.py")

# DCM simulation experiment

N = 10
M = lambda: random.randrange(5)+2
K = 5
alpha = np.tile(1/K, K)
#print "N=",N, "M=",M, "N*M=",(N*M), "alpha=",alpha
multis = np.random.dirichlet(alpha, N)
counts = [np.random.multinomial(M(), p) for p in multis]
asdf

def loglik(log_conc):
  alpha = np.tile(np.exp(log_conc)/K, K)
  loglik = sum(mcmc.dcm(c,alpha) for c in counts)
  #prior = r.dgamma(np.exp(log_conc), .5, scale=1000, log=True)
  #prior = 0
  return loglik

logpost = lambda x: loglik(x) + r.dgamma(np.exp(x), .1, scale=1e5, log=True)
#logpost = lambda x: loglik(x) + r.dgamma(np.exp(x), 10, scale=.1, log=True)

#r.par(mfrow=[2,2])
# Exhaustive computation to compare to slice sampler

log_concs = np.arange(-50,20,.1)
lls = np.array([ loglik(log_conc) for log_conc in log_concs ])
mle = log_concs[lls.argmax()]
print "MLE", np.exp(mle)
lps = np.array([ logpost(log_conc) for log_conc in log_concs ])
map = log_concs[lps.argmax()]
print "MAP", np.exp(map)
#r.plot(log_concs, lls, xlab='x',ylab='ll')
#plot(log_concs, lls, main='loglik')
#plot(log_concs, lps, main='unnorm logpost')


# For comparisons

real_s = r.sample(log_concs,len(histories)*2,prob=np.exp(lps),replace=True)


# Slice sampler

h = mcmc.slice_sample(logpost, 0.0, 1.0, niter=1000)
h = np.array(h)

# Replications of single slice-sample
#for i in range(500):
#  init = histories[i][-1]
#  h = mcmc.slice_sample(logpost, init, 1.0, niter=200)
#  histories[i] += h
