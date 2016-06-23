"""
Point estimates of Dirichlet parameters, based on
Thomas P. Minka 2009, "Estimating a Dirichlet distribution"
http://research.microsoft.com/en-us/um/people/minka/papers/dirichlet/minka-dirichlet.pdf
"""

from __future__ import division
import numpy as np

def moment_match_ronning(pvecs):
  """Minka eq. 23. not extensively tested"""
  N,K = pvecs.shape
  if N < 10:
    print "warning, N={} is unreliable for moment matching".format(N)
  ep_all = pvecs.mean(0)
  vp_all = pvecs.var(0)
  vp_all[vp_all < 1e-5] = 1e-5

  ep = ep_all[:(K-1)]
  vp = vp_all[:(K-1)]
  terms = np.log(ep*(1-ep)/vp - 1)
  sumalpha = np.exp( np.mean(terms) )
  return ep_all * sumalpha

fitdir = moment_match_ronning


if __name__=='__main__':
  import random

  C=10
  N=20
  K=5
  alpha = C * np.ones(K) / K
  for outer in range(10):
    d = np.random.dirichlet(alpha, N)

    # bootstrap
    results = []
    for itr in range(10000):
      samp=np.array([random.randrange(N) for i in range(N)])
      a=fitdir(d[samp])
      results.append(a)
    r = np.array(results)

    print "Real ", alpha

    print "Estimates:"
    print "low  ",r.mean(0)-r.std(0)*2
    print "mean ",r.mean(0)
    print "high ",r.mean(0)+r.std(0)*2
    print ""

