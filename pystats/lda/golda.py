import numpy as np
import random
seed=10000
np.random.seed(100);random.seed(100)
execfile("simlda.py")
import lda
sys.path.insert(0,'..')
import cross

m = lda.LDA(N_topic = K, phi_prior = beta[0], theta_prior=alpha[0], mode='cgs')
m.bind_data(data.tok_d,data.tok_w,V,D)
print "Initial", cross.reconstruction_error(phi, m.C_word_topic)
for i in range(30):
    #print "== Iter",i
    #info = lda2.cvb0_sync(m)
    #info = lda.cvb0_iter(m, suppress_offbyone=True)
    info = lda.cgs_prange(m)
    #info = lda.cgs_outer(m)
    #info = lda.cgs_inner(m, 0, m.N_tok, 0)
    info = {}
    info.update( lda.loglik(m) )
    print i,"\t", info, "\t", cross.reconstruction_error(phi, m.C_word_topic)
