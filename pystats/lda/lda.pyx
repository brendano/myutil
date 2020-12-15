#cython: boundscheck=False, cdivision=True
# vim:sts=4:sw=4
import numpy as np
cimport numpy as np
cimport cython
cimport openmp
from cython.parallel import *
from libc.math cimport log
#import random
#rand = random.random
from stats import mcmc

cdef extern from "stdlib.h":
    void free(void* ptr) nogil
    void* malloc(size_t size) nogil
    void* calloc(size_t count, size_t size) nogil

cdef extern from "stdio.h":
    void printf(char *fmt) nogil

cdef extern from "gsl/gsl_rng.h":
    ctypedef struct gsl_rng_type:
        pass
    ctypedef struct gsl_rng:
        pass
    gsl_rng_type *gsl_rng_mt19937
    gsl_rng *gsl_rng_alloc(gsl_rng_type * T)
    double gsl_rng_uniform(gsl_rng * r) nogil

cdef gsl_rng *rngs[10]
for i in range(10):
    rngs[i] = gsl_rng_alloc(gsl_rng_mt19937)


cdef int weighted_randint(double *unnorm_probs, double psum, int K) nogil:
    return weighted_randint_threaded(unnorm_probs,psum,K,0)

cdef int weighted_randint_threaded(double *unnorm_probs, double psum, int K, int threadid) nogil:
    cdef double cum = 0
    cdef double r
    r = gsl_rng_uniform(rngs[threadid]) * psum
    cdef int i
    for 0 <= i < K:
        cum += unnorm_probs[i]
        if r <= cum: return i
    #with gil: assert False, "WTF"
    printf("WTF\n")
    return 0

#######################################################################

cdef int NA = -1

cdef class LDA:
    
    cdef public int N_topic, N_word, N_tok, N_doc
    cdef public double phi_prior, theta_prior
    cdef public np.ndarray tok_topic
    cdef public np.ndarray tok_d, tok_w
    cdef public np.ndarray C_word_topic, C_doc_topic, C_doc, C_topic
    cdef public object mode

    def __init__(self, N_topic=10, phi_prior=0.01, theta_prior=1, mode='cgs'):
        self.N_topic = N_topic
        self.phi_prior = phi_prior
        self.theta_prior = theta_prior
        self.mode = mode

    def bind_data(_, tok_d, tok_w, N_word, N_doc):
        _.N_tok = len(tok_d)
        assert len(tok_d) == len(tok_w)
        _.N_word = N_word
        _.N_doc = N_doc
        _.tok_d = tok_d
        _.tok_w = tok_w

        if _.mode=='cgs':
            count_type = np.int
            _.tok_topic = np.random.randint(_.N_topic, size=_.N_tok)
        elif _.mode=='cvb0':
            count_type = np.double
            _.tok_topic = np.random.random((_.N_tok, _.N_topic))
        else: assert False

        _z = lambda shape: np.zeros(shape, dtype=count_type)
        _.C_word_topic      = _z((_.N_word, _.N_topic))
        _.C_doc_topic       = _z((_.N_doc, _.N_topic))
        _.C_doc             = _z(_.N_doc)
        _.C_topic           = _z(_.N_topic)

        ## Initialize count tables
        for i in range(_.N_tok):
            if _.mode=='cvb0': _.tok_topic[i] /= _.tok_topic[i].sum()
            for k in range(_.N_topic):
                if _.mode=='cgs':
                    if _.tok_topic[i] != k: continue
                    delta = 1
                else:
                    delta = _.tok_topic[i,k]
                d = _.tok_d[i]
                w = _.tok_w[i]
                _.C_doc_topic[d,k]  += delta
                _.C_topic[k]        += delta
                _.C_doc[k]          += delta
                _.C_word_topic[w,k] += delta

    @property
    def alpha_conc(self):
        return self.theta_prior * self.N_topic
    @property
    def beta_conc(self):
        return self.phi_prior * self.N_word
    @property
    def gamma_conc(self):
        return 1

def w_loglik(mm, beta_conc):
    # p(w | z, beta)  [[integrating out phi]]
    betavec = np.tile(beta_conc / mm.N_word, mm.N_word)
    tot_ll = 0
    for kk in range(mm.N_topic):
        word_counts = mm.C_word_topic[:,kk]
        tot_ll += mcmc.dcm(word_counts, betavec, conc=beta_conc, single=True)
    return tot_ll

def z_loglik(mm, alpha_conc):
    # p(z | alpha)  [[ integrating out theta ]]
    alphavec = np.tile(alpha_conc / mm.N_topic, mm.N_topic)
    conc = np.sum(alphavec)
    ll = 0
    for dd in range(len(mm.C_doc)):
        zs = mm.C_doc_topic[dd]
        ll += mcmc.dcm(zs, alphavec, conc=conc, N=mm.C_doc[dd], single=True)
    return ll

def loglik(mm):
    w_ll = w_loglik(mm, mm.beta_conc)
    z_ll = w_loglik(mm, mm.alpha_conc)
    return {'ll': w_ll+z_ll}


#######################################################################

cpdef cgs_ser(LDA mm, inds=None):
    if inds is None: inds = range(mm.N_tok)
    cdef int N_word = mm.N_word
    cdef int changes = 0

    cdef unsigned int ii,kk,cur,oldz,newz
    cdef int ww,dd
    #cdef double denom,pp,psum
    cdef np.ndarray[np.double_t] unnorm_probs = np.ones(mm.N_topic)
    cdef double denom,pp,psum

    # stupid, doesn't allow buffer type declaration on object attributes
    cdef np.ndarray[np.int_t,ndim=2] C_word_topic = mm.C_word_topic
    cdef np.ndarray[np.int_t,ndim=2] C_doc_topic = mm.C_doc_topic
    cdef np.ndarray[np.int_t] C_topic = mm.C_topic
    cdef np.ndarray[np.int_t] tok_d = mm.tok_d
    cdef np.ndarray[np.int_t] tok_w = mm.tok_w
    cdef np.ndarray[np.int_t] tok_topic = mm.tok_topic

    #cdef int start=min(inds), end=max(inds)+1
    #with parallel():
    #for ii in prange(start, end, nogil=True):
    for ii in inds:
        psum = 0
        oldz = tok_topic[ii]
        ww = tok_w[ii]
        dd = tok_d[ii]
        for 0 <= kk < mm.N_topic:
            cur = int(kk == oldz)
            pp = 1
            # word likelihood
            denom = C_topic[kk] - cur + N_word*mm.phi_prior
            pp *= (C_word_topic[ww,kk] - cur + mm.phi_prior) / denom
            # class prior
            pp *= (C_doc_topic[dd,kk] - cur + mm.theta_prior)
            # record
            unnorm_probs[kk] = pp
            psum += pp

        # Sample!
        newz = weighted_randint(<double *> unnorm_probs.data, psum, mm.N_topic)
        #print oldz, "->",newz

        if oldz != newz:
            tok_topic[ii] = newz
            C_word_topic[ww,oldz] -= 1
            C_word_topic[ww,newz] += 1
            C_doc_topic[dd,oldz] -= 1
            C_doc_topic[dd,newz] += 1
            C_topic[oldz] -= 1
            C_topic[newz] += 1
            changes += 1
        else:
            pass

    return {'changes': changes}


cpdef cgs_prange(LDA mm, inds=None):
    #print "start"
    if inds is None: inds = range(mm.N_tok)
    cdef int N_word = mm.N_word
    cdef int changes = 0

    cdef unsigned int ii,kk,cur,oldz,newz
    cdef int ww,dd

    # stupid, doesn't allow buffer type declaration on object attributes
    cdef np.ndarray[np.int_t,ndim=2] C_word_topic = mm.C_word_topic
    cdef np.ndarray[np.int_t,ndim=2] C_doc_topic = mm.C_doc_topic
    cdef np.ndarray[np.int_t] C_topic = mm.C_topic
    cdef np.ndarray[np.int_t] tok_d = mm.tok_d
    cdef np.ndarray[np.int_t] tok_w = mm.tok_w
    cdef np.ndarray[np.int_t] tok_topic = mm.tok_topic

    cdef double *pps, *psums
    #print "max threads", openmp.omp_get_max_threads()
    pps = <double *> calloc(openmp.omp_get_max_threads() * 32, sizeof(double))
    psums = <double *> calloc(openmp.omp_get_max_threads() * 32, sizeof(double))
    cdef double *unnorm_probss = <double *> calloc(mm.N_topic * openmp.omp_get_max_threads() * 32, sizeof(double))
    cdef double *pp, *psum, *unnorm_probs

    cdef int start=min(inds), end=max(inds)+1
    cdef int tid
    #with parallel():
    #for ii in prange(start, end, nogil=True):
    with nogil:
        for ii in range(start,end):
            tid = threadid()
            psum = psums + 32 * tid
            pp = pps + 32 * tid
            unnorm_probs = unnorm_probss + 32*tid*mm.N_topic
            
            psum[0] = 0
            oldz = tok_topic[ii]
            ww = tok_w[ii]
            dd = tok_d[ii]
            for 0 <= kk < mm.N_topic:
                cur = int(kk == oldz)
                pp[0] = 1
                # word likelihood
                pp[0] *= (C_word_topic[ww,kk] - cur + mm.phi_prior) / (C_topic[kk] - cur + N_word*mm.phi_prior)
                # class prior
                pp[0] *= (C_doc_topic[dd,kk] - cur + mm.theta_prior)
                # record
                unnorm_probs[kk] = pp[0]
                psum[0] += pp[0]

            # Sample!
            newz = weighted_randint_threaded(unnorm_probs, psum[0], mm.N_topic, tid)
            #print oldz, "->",newz

            if oldz != newz:
                tok_topic[ii] = newz
                C_word_topic[ww,oldz] -= 1
                C_word_topic[ww,newz] += 1
                C_doc_topic[dd,oldz] -= 1
                C_doc_topic[dd,newz] += 1
                C_topic[oldz] -= 1
                C_topic[newz] += 1
                changes += 1
            else:
                pass

    return {'changes': changes}


#######################################################################


cpdef cgs_outer(LDA mm):
    cdef int i
    starts = [0, int(mm.N_tok/2), mm.N_tok]
    #for i in range(len(starts)-1):
    #    cgs_inner(mm, starts[i], starts[i+1], 0)
    cdef int last = len(starts) - 1
    for i in prange(last, nogil=True, num_threads=2,schedule='static'):
        with gil:
            cgs_inner(mm, starts[i], starts[i+1], i)

cpdef cgs_inner(LDA mm, int start, int end, int threadid):
    #print "inner",threadid
    cdef int N_word = mm.N_word
    cdef int changes = 0

    cdef unsigned int ii,kk,cur,oldz,newz
    cdef int ww,dd
    cdef double *unnorm_probs = <double *> calloc(mm.N_topic, sizeof(double))
    cdef double denom,pp,psum

    # stupid, doesn't allow buffer type declaration on object attributes
    cdef np.ndarray[np.int_t,ndim=2] C_word_topic = mm.C_word_topic
    cdef np.ndarray[np.int_t,ndim=2] C_doc_topic = mm.C_doc_topic
    cdef np.ndarray[np.int_t] C_topic = mm.C_topic
    cdef np.ndarray[np.int_t] tok_d = mm.tok_d
    cdef np.ndarray[np.int_t] tok_w = mm.tok_w
    cdef np.ndarray[np.int_t] tok_topic = mm.tok_topic

    with nogil:
        for ii in range(start, end):
            #printf("THREAD %d  INDEX %d\n", threadid, ii)
            psum = 0
            oldz = tok_topic[ii]
            ww = tok_w[ii]
            dd = tok_d[ii]
            for 0 <= kk < mm.N_topic:
                cur = int(kk == oldz)
                pp = 1
                # word likelihood
                denom = C_topic[kk] - cur + N_word*mm.phi_prior
                pp *= (C_word_topic[ww,kk] - cur + mm.phi_prior) / denom
                # class prior
                pp *= (C_doc_topic[dd,kk] - cur + mm.theta_prior)
                # record
                unnorm_probs[kk] = pp
                psum += pp

            # Sample!
            newz = weighted_randint_threaded(<double *> unnorm_probs, psum, mm.N_topic, threadid)
            #print oldz, "->",newz

            if oldz != newz:
                # TODO LOCK
                tok_topic[ii] = newz
                C_word_topic[ww,oldz] -= 1
                C_word_topic[ww,newz] += 1
                C_doc_topic[dd,oldz] -= 1
                C_doc_topic[dd,newz] += 1
                C_topic[oldz] -= 1
                C_topic[newz] += 1
                #changes += 1
            else:
                pass





cpdef cvb0_iter(LDA mm, inds=None, suppress_offbyone=False):
    cdef int _so = int(suppress_offbyone)
    if inds is None: inds = range(mm.N_tok)
    cdef int N_word = mm.N_word
    cdef int changes = 0

    cdef unsigned int ii,kk
    cdef int ww,dd
    cdef double denom,pp,psum,delta,cur
    #cdef np.ndarray[np.double_t] cur
    cdef np.ndarray[np.double_t] unnorm_probs = np.ones(mm.N_topic)

    # stupid, doesn't allow buffer type declaration on object attributes
    cdef np.ndarray[np.double_t,ndim=2] C_word_topic = mm.C_word_topic
    cdef np.ndarray[np.double_t,ndim=2] C_doc_topic = mm.C_doc_topic
    cdef np.ndarray[np.double_t] C_topic = mm.C_topic
    cdef np.ndarray[np.int_t] tok_d = mm.tok_d
    cdef np.ndarray[np.int_t] tok_w = mm.tok_w
    cdef np.ndarray[np.double_t,ndim=2] tok_topic = mm.tok_topic

    for ii in inds:
        psum = 0
        ww = tok_w[ii]
        dd = tok_d[ii]
        for kk in range(mm.N_topic):
            cur = tok_topic[ii,kk] if not _so else 0.0
            pp = 1
            # word likelihood
            denom = C_topic[kk] - cur + N_word*mm.phi_prior
            pp *= (C_word_topic[ww,kk] - cur + mm.phi_prior) / denom
            # class prior
            pp *= (C_doc_topic[dd,kk] - cur + mm.theta_prior)
            # record
            unnorm_probs[kk] = pp
            psum += pp

        # Normalize
        for kk in range(mm.N_topic):
            unnorm_probs[kk] /= psum
        #print "NEW",unnorm_probs

        for kk in range(mm.N_topic):
            delta = unnorm_probs[kk] - tok_topic[ii,kk]
            C_word_topic[ww,kk] += delta
            C_doc_topic[dd,kk]  += delta
            C_topic[kk]         += delta
            tok_topic[ii,kk]    += delta

    return {}


