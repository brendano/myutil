execfile("simlda.py")
import sys,time
import numpy as np
import lda
sys.path.insert(0,'..')

#MODE = sys.argv[1]
MODE = 'cgs'

def make_model():
    m = lda.LDA(N_topic = K, phi_prior = beta[0], theta_prior=alpha[0], mode=MODE)
    m.bind_data(data.tok_d,data.tok_w,V,D)
    return m

def infer(mm):
    lda.cgs_ser(mm)
    #lda.cgs_prange(mm)


times = []
for outer in range(7):
  sys.stdout.write('.');sys.stdout.flush()
  #random.seed(42); np.random.seed(42)
  mm = make_model()

  t0 = time.time()

  for i in range(100):
      infer(mm)

  times.append(time.time() - t0)

times = [t for t in times if t != min(times) and t != max(times)]
times.sort()
print times
print "mean time", np.mean(times), " sd",np.std(times), " se", np.std(times)/np.sqrt(len(times))

