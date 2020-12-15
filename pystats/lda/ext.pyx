
# range() per thread.  doesn't seem to work.
#cython: boundscheck=False, cdivision=True

import numpy as np
cimport numpy as np
cimport cython
cimport openmp
from cython.parallel import *

cdef double proc(double *x, int start, int end) nogil:
    cdef int i, j
    cdef double s
    for i in range(start, end):
        for j in range(1000):
            s += x[i]
    return s

cpdef go(np.ndarray[np.double_t] x, int nblock):
    cdef int start,end, i,N
    cdef double ret
    N = len(x)
    #print "max threads", openmp.omp_get_max_threads()
    cdef double * x_data = <double *> x.data
    with nogil:
        for i in prange(nblock):
        #for i in range(nblock):
            start = int(i/nblock*N)
            end = int((i+1)/nblock*N)
            ret = proc(x_data, start, end)
            #printf("ret %g\n", ret)

cpdef mysum(np.ndarray[np.double_t] x):
    cdef double sumvec[2]
    sumvec[0] = 0; sumvec[1] = 0
    cdef int ii
    cdef int N=len(x)
    with nogil:
        #for ii in range(N):
        for ii in prange(N, num_threads=2):
            sumvec[ (ii % 2) ] += x[ii]
    print sumvec[0], sumvec[1]
