from __future__ import division
import numpy as np
import mcmc; reload(mcmc)

## Compare: (1) Polya urn procedural likelihood, versus (2) DCM PMF likelihood
## Remove the number-of-sequences correction for DCM and they're the same.

from rpy import r
K = 5
N = 10
alpha = r.rgamma(K, .1, scale=1000)
data = np.random.randint(0,K, size=N)
counts = np.tile(0, K)
def prob(z):
  return (counts[z]+alpha[z]) / (sum(counts) + sum(alpha))

print "alpha\t",alpha

lp = 0
for z in data:
  lp += np.log(prob(z))
  counts[z] += 1

print "counts\t",counts

print "urn process\t", lp, np.exp(lp)

x = mcmc.dcm(np.array(counts),np.array(alpha), single=True)
print "DCM-1   \t", x, np.exp(x)

x = mcmc.dcm(np.array(counts),np.array(alpha), single=False)
print "DCM full\t", x, np.exp(x)


lp = 0
for z in data:
  counts[z] -= 1
  lp += np.log(prob(z))
  counts[z] += 1

print "posthoc-1\t", lp, np.exp(lp)

lp = 0
for z in data:
  lp += np.log(prob(z))

print "posthoc    \t", lp, np.exp(lp)

