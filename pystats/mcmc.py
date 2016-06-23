# -*- encoding: utf-8 -*-
# vim:sw=4:sts=4
"""
Some MCMC algorithms.
"""

from __future__ import division
import numpy as np
import random,sys,math
import scipy.special as sp
import statutil

def metropolis(logdist, proposer, initial, niter=1000, thin=1):
    """
    Original Metropolis (1953) algorithm: for a symmetric proposal.
    Do not need to pass in a proposal density function -- only the proposal sampler.

    logdist: density function for target distribution
    proposer: proposal function
    initial: initial state

    @returns history
    """

    state = initial
    d_cur = logdist(state)
    history = []
    for itr in xrange(niter):
        if itr % 1000 == 0: sys.stdout.write("."); sys.stdout.flush()
        proposal = proposer(state)
        d_proposal = logdist(proposal)
        if d_proposal >= d_cur:
            state = proposal
            d_cur = d_proposal
        else:
            r = random.random()
            if r < np.exp(d_proposal - d_cur):
                state = proposal
                d_cur = d_proposal
        if itr % thin == 0:
            history.append(state)
            # print "METRO", state
    return history


def hastings(target_density, proposal_density, proposer, initial, niter=1000, thin=1):
    """
    Full Metropolis-Hastings algorithm (Hastings 1970).

    target_density(x) ==> density evaluated at state x
    proposal_density(xold, xnew) ==> for xold, eval proposal prob for xnew
    proposer(xold) ==> samples an xnew from proposal_density
    initial ==> initial state

    @returns history
    """
    assert False, "unimplemented"

def test_metropolis():
    import pylab
    # andrew thomas' slide 42
    #Try the triangle distribution
    #Pr(Y =y)∝yI(y <11,y >0))+(20−y)I(y >10,y <20)).
    #Proposal distribution: Yprop = Yi−1 + 2Be(1) − 1. (One step 2
    #positive or negative).
    proposal=lambda x: x+random.randrange(2)*2-1
    h=metropolis(triangle_lp, proposal, 5, niter=10000)
    pylab.clf()
    pylab.hist(h,100)

def triangle_lp(x):
    # print "EVAL"
    p = float( (0 < x < 20) * (x*(x<10) + (20-x)*(x>=10)) ) / 5
    if p==0: return -1e100
    return math.log(p)
  
def triangle_tests():
    proposal=lambda x: x+random.randrange(2)*2-1
    history = metropolis(triangle_lp, proposal, 5, niter=10000)
    for h in history: print "METRO", h
    print "START SS"
    history = slice_sample(triangle_lp, np.array([5]), np.array([1]), 1000)
    for h in history: print "SLICE", h[0]


def slice_sample(logdist, initial, widths, niter=10, step_out=True):
    """
    Slice sampling (Neal 2003; MacKay 2003, sec. 29.7)

    logdist: log-density function of target distribution
    initial: initial state. either 1-dim vector, or scalar
    widths: step sizes for expanding the slice

    This is my port of Iain Murray's
    http://homepages.inf.ed.ac.uk/imurray2/teaching/09mlss/slice_sample.m 
    which in turn derives from MacKay.  Murray notes where he found bugs in
    MacKay's pseudocode... good sign
    """

    # Process options
    if isinstance(initial, (int,float)):
        initial = np.array([initial])
    initial = initial * 1.0

    assert len(initial.shape) == 1
    D = len(initial)

    if isinstance(widths, (int,float)):
        widths = np.tile(widths, D)
    widths = widths * 1.0
    assert len(widths) == len(initial)


    # Initialize
    state = initial
    log_Px = logdist(state)
    if log_Px is None:
        # placeholder for MPI trickiness
        return [initial]
    history = []

    def dump():
        print "state",state, "log_Px",log_Px

    # Main loop
    for itr in xrange(niter):
        #print "Slice iter",itr,; dump()
        if itr%100==0: 
            sys.stdout.write("."); sys.stdout.flush()
        log_uprime = np.log(random.random()) + log_Px
        inds = range(D); random.shuffle(inds)

        # Sweep through axes
        for dd in inds:
            x_l  = state.copy()
            x_r  = state.copy()
            xprime = state.copy()
            # Create a horizontal interval (x_l, x_r) enclosing xx
            r = random.random()
            x_l[dd] = state[dd] - r*widths[dd]
            x_r[dd] = state[dd] + (1-r)*widths[dd]
            if step_out:
                while logdist(x_l) > log_uprime:
                    x_l[dd] -= widths[dd]
                while logdist(x_r) > log_uprime:
                    x_r[dd] += widths[dd]
            # Inner loop:
            # Propose xprimes and shrink interval until good one is found.
            zz = 0
            while True:
                zz += 1
                if zz%1000==0: 
                    sys.stdout.write("_");sys.stdout.flush() 
                #print "iter",itr, "step",zz
                xprime[dd] = random.random() * (x_r[dd] - x_l[dd]) + x_l[dd]
                log_Px = logdist(xprime)
                if log_Px > log_uprime:
                    break
                else:
                    if xprime[dd] > state[dd]:
                        x_r[dd] = xprime[dd]
                    elif xprime[dd] < state[dd]:
                        x_l[dd] = xprime[dd]
                    else:
                        assert False, "BUG, shrunk to current position and still not acceptable"
            state[dd] = xprime[dd]
        # END sweep through axes
        history.append(np.copy(state))
        # print "SLICE", state[0]
    return history

def trace_plots(history, burnin):
    from rpy import r
    h = np.array(history)
    print "{} total, {} burning, {} remaining".format(len(history), burnin, len(history)-burnin)
    r.par(mfrow=[2,2])
    r.acf(h[burnin:])
    r.plot(h,xlab='',ylab='',main='')
    r.abline(v=burnin, col='blue')
    #r.hist(h[burnin:],breaks=30,xlab='',ylab='',main='histogram')
    r.plot(r.density(h[burnin:]), xlab='',ylab='',main='density')

def qqplot_density(samples, density_x, density_y):
    from rpy import r
    # LAME: should do better quantile calculation.  r.quantile() returns a
    # hard-to-use dictionary unfortunately.  r.approx() as per ?quantile, maybe.
    percs = list(np.arange(1,100, .1))
    data_perc=np.percentile(samples, percs)
    real_perc=np.percentile(r.sample(density_x, len(samples)*10, prob=density_y, replace=True), percs)
    r.plot(real_perc, data_perc,  xlab='',ylab='',main='');
    r.abline(a=0,b=1,col='blue')



####################################################################

def dcm(countvec, alpha, conc=None, N=None, single=False):
    """Dirichlet-Compound Multinomial PMF
    Returns log-likelihood of counts, given dirchlet alpha vector
    single=True ==> Return log-lik of just ONE sequence .. not really a DCM.
    """
    G = math.lgamma
    vG = sp.gammaln  ## vectorized but slower
    K = len(countvec);  assert len(alpha)==K
    N = countvec.sum() if N is None else N
    A = alpha.sum() if conc is None else conc
    log_numseq = 0 if single else G(N+1) - vG(countvec + 1).sum()
    # G(A)-G(A+N) is nearly as fast as -pochhammer(A,N)... lame
    return log_numseq + \
            -statutil.poch(A,N) + \
            vG(countvec+alpha).sum() - vG(alpha).sum()

def dcm_symm(countvec, alpha_symm, N=None, single=False):
    """Dirichlet-Compound Multinomial PMF (single path)"""
    G = math.lgamma
    K = len(countvec)
    N = countvec.sum() if N is None else N
    A = alpha_symm*K
    assert single
    log_numseq = 0
    return log_numseq + \
            -statutil.poch(A,N) + \
            statutil.faster_lgamma_sum(countvec, alpha_symm) - K*G(alpha_symm)

