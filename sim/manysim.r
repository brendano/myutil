source("sim/lr.r")
library(coda)
library(spatstat)
library(Matching)
set.seed(10)

allruns = timeit(llply(1:10, function(itr) {
  source("sim/logreg.r")
  # source("sim/linreg.r")
  out = infer_j()
  ret = sim
  for (name in names(out)) {
    ret[[name]] = out[[name]]
  }
  ret
}, .progress='text'))

source("sim/manysim_an.r")
