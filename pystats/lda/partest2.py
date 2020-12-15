import ext,sys,time
import numpy as np
N = int(100e6)
x = np.arange(N, dtype=np.double)
nblock = 100
#%timeit ext.go(x,1)


times = []
for outer in range(5):
    sys.stdout.write('.');sys.stdout.flush()
    t0 = time.time()
    #ext.go(x, nblock)
    ext.mysum(x)
    times.append(time.time() - t0)

times = [t for t in times if t != min(times) and t != max(times)]
times.sort()
print times
print "mean time", np.mean(times), " sd",np.std(times), " se", np.std(times)/np.sqrt(len(times))

