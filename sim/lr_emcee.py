from __future__ import division
import math
import numpy as np
import emcee

X = np.loadtxt("sim/X")
Y = np.loadtxt("sim/Y")

def phi(x):
    'Cumulative distribution function for the standard normal distribution'
    return (1.0 + math.erf(x / math.sqrt(2.0))) / 2.0

def lnprob(beta):
    global X,Y
    scores = X.dot(beta)
    probs = np.exp(scores) / (1 + np.exp(scores))   ## logit
    # probs = np.array([ phi(s) for s in scores ])  ## probit
    ll = 0
    if np.sum(Y==1) > 0:
        ll += np.sum(np.log(probs[Y==1]))
    if np.sum(Y==0) > 0:
        ll += np.sum(np.log(1-probs[Y==0]))
    ll += -(1.0/2 / 1) * np.sum(beta**2)
    return ll

ndim = X.shape[1]
nwalkers = ndim*2
beta0 = [np.random.rand(ndim) for i in range(nwalkers)]

sampler = emcee.EnsembleSampler(nwalkers, ndim, lnprob)
sampler.run_mcmc(beta0, 10)
sampler.reset()
sampler.run_mcmc(beta0, 500)

print "acceptance rate ", np.mean(sampler.acceptance_fraction)

# mse = np.mean( (sampler.flatchain.mean(0) - np.loadtxt("sim/B"))**2 )
# print "mse", mse

for w in xrange(sampler.chain.shape[0]):
    for t in xrange(sampler.chain.shape[1]):
        print 'beta',w,t, ' '.join('%.6g' % x for x in sampler.chain[w,t,:])

