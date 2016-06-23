import numpy as np
import util
p = np.array([ 0.19255596,  0.15655352,  0.21629385,  0.2097999 ,  0.22479676])
for i in range(1000):
    print i, util.weighted_randint(p)
