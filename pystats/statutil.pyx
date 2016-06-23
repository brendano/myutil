import numpy as np
cimport numpy as np
from libc.math cimport log, lgamma
import math
import random
rand = random.random

cdef extern from "lightspeed_util.h":
    cdef double pochhammer(double x, int n)

cpdef poch(double x, int n):
    """
    pochhammer: same as lgamma(x+n) - lgamma(x)
    http://mathworld.wolfram.com/PochhammerSymbol.html
    https://github.com/jacobeisenstein/SAGE/blob/master/3rd-party/fastfit/pochhammer.m
    """
    return pochhammer(x,n)

cpdef double faster_lgamma_sum(np.ndarray[np.int_t] countvec, double alpha_symm): 
    """ 
    designed for sparse (though densely represented) count vector.
    same as: scipy.special.gammaln(countvec + alpha_symm).sum() 
    """
    cdef double zeroval = math.lgamma(alpha_symm)
    cdef int ii, xx
    cdef int N = countvec.shape[0]
    cdef double ss = 0
    for ii in range(N):
        xx = countvec[ii]
        if xx==0:
            ss += zeroval
        else:
            ss += lgamma(xx + alpha_symm)
    return ss

cpdef double sparse_lgamma_sum(countvec, double symm_alpha):
    """countvec: a CSR matrix representing a vector as a single row"""
    assert countvec.shape[0]==1
    cdef int N = countvec.shape[1]
    cdef np.ndarray[np.int_t] data = countvec.data
    cdef double ss=0
    cdef int nnz = countvec.nnz

    for ii in range(nnz):
        ss += lgamma(data[ii] + symm_alpha)
    ss += (N - nnz) * lgamma(symm_alpha)
    return ss


cdef int _weighted_randint(double *unnorm_probs, double psum, int K):
    # psum: the total sum of unnorm_probs
    # K: unnorm_probs' length
    cdef double cum = 0
    cdef double r
    cdef int i
    r = rand() * psum
    for 0 <= i < K:
        cum += unnorm_probs[i]
        if r <= cum: return i

    print "R",r,"CUM",cum,"PSUM",psum, "K",K
    assert False, "WTF something is wrong"
    return 0

def weighted_randint(np.ndarray unnorm_probs, psum=None):
    """
    Draw once from a multinomial, represented as an integer.  
    Take care of funny representations unnorm_probs can take.
    If you supply the weights' sum it will be used, else we compute it here.
    """
    if unnorm_probs.dtype != np.double:
        unnorm_probs = np.array(unnorm_probs, dtype=np.double)
    unnorm_probs = np.ascontiguousarray(unnorm_probs)
    if psum is None:
        psum = unnorm_probs.sum()
    return _weighted_randint(<double *> unnorm_probs.data, psum, unnorm_probs.size)

