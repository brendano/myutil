from __future__ import division
from collections import namedtuple
import numpy as np
import random, sys
sys.path.insert(0,'..')
from stats import util


D = 500
M = lambda: 100 + random.randrange(100)
K = 10
alpha = np.tile(1/K, K)
doc_multis = np.random.dirichlet(alpha, D)
doc_z_counts = [np.random.multinomial(M(), p) for p in doc_multis]

V = 500
beta = np.tile(50/V, V)
phi = np.zeros((V,K), dtype=np.double)
for k in range(K):
    phi[:,k] = np.random.dirichlet(beta)

token_data = []
token_z = []
for d,z_count in enumerate(doc_z_counts):
    for ztype,c in enumerate(z_count):
        for i in range(c):
            w = util.weighted_randint(phi[:,ztype])
            token_data.append((d,w))
            token_z.append(ztype)

data = namedtuple('Data','tok_d tok_w')(
        tok_d = np.array(token_data)[:,0],
        tok_w = np.array(token_data)[:,1],
        )

