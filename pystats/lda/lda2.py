from __future__ import division
import numpy as np

def cvb0_sync(mm, inds=None):
    if inds is None: inds = range(mm.N_tok)

    numer = mm.C_doc_topic + mm.theta_prior
    denom = (mm.C_word_topic + mm.phi_prior) / (mm.C_topic + mm.phi_prior * mm.N_word)

    # numer: DxK
    # denom: VxK

    new_q = numer[mm.tok_d] * denom[mm.tok_w]
    new_q = (new_q.T / new_q.sum(1)).T          # normalize
    new_q = np.ascontiguousarray(new_q)

    update_counts(mm, new_q)
    mm.tok_topic = new_q
    return {}


def update_counts(mm, new_q):
    ## Need crosstabs support here.
    mm.C_topic = new_q.sum(0)
    old_q = mm.tok_topic
    # update C_doc_topic
    # update C_word_topic

    for ii in range(mm.N_tok):
        dd = mm.tok_d[ii]
        ww = mm.tok_w[ii]
        for kk in range(mm.N_topic):
            delta = new_q[ii,kk] - old_q[ii,kk]
            mm.C_doc_topic[dd,kk] += delta
            mm.C_word_topic[ww,kk] += delta





