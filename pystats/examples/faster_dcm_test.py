from __future__ import division
import statutil
C=np.zeros(10000,dtype=int)
C[:50] = 2
A=np.tile(1.0/len(C),len(C))

import scipy.special as sp

import scipy.sparse
S = scipy.sparse.csr_matrix(C)

print sp.gammaln(C+A).sum()
print statutil.faster_lgamma_sum(C,A[0])
print statutil.sparse_lgamma_sum(S,A[0])

#In [1]: timeit util.sparse_lgamma_sum(S,A[0])
#10000 loops, best of 3: 23.1 us per loop
#
#In [2]: timeit util.faster_lgamma_sum(C,A[0])
#10000 loops, best of 3: 23.7 us per loop
#
#In [3]: timeit sp.gammaln(C+A).sum()
#1000 loops, best of 3: 569 us per loop

import mcmc
print mcmc.dcm_symm(C,A[0], single=True)
#Out[8]: -824.251394401741

print mcmc.dcm(C,A, single=True)
#Out[9]: -824.25139440437488

#In [10]: timeit mcmc.dcm(C,A, single=True)
#1000 loops, best of 3: 1.15 ms per loop
#
#In [11]: timeit mcmc.dcm_symm(C,A[0], single=True)
#10000 loops, best of 3: 40.1 us per loop

