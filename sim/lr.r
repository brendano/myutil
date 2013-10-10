infer_j = function() {
  ret = list()
  system("./java.sh GLM logit 1000 sim/X sim/Y > log")
  d = read.table(pipe("grep '^beta' log | cleanarr"))
  # cat("\n"); system("grep accept log | count")
  ret$samples = d[ , 3:ncol(d) ]   #* 1.6  ## multiplier for probit->logit hack
  ret$d = d
  w = exp(d[,2] - min(d[,2])); w=w/sum(w); ret$w=w
  ret
}

infer_m = function() {
  library(MCMCpack)
  m = MCMCprobit(Y~0+X, data=sim, b0=0, B0=1)

  # m = MCMCregress(Y~0+X, data=sim, b0=0, B0=1, c0=1e6, d0=1e6, burnin=5000, mcmc=10000)
  # m = as.matrix(m[,1:(ncol(m)-1)])

  list(samples=m)
}
infer_g = function() {
  d = read.table(pipe("./java.sh util.GaussianInference sim/X sim/Y | cleanarr"))
  list(samples = d[,2:ncol(d)])
}
                 
infer_emcee = function() {
  ret = list()
  system("python sim/lr_emcee.py > log")
  d = read.table(pipe("grep '^beta' log | cleanarr"))
  names(d)[2:3] = c('chain','t')
  # d = subset(d, t > burnin)
  d = arrange(d,t)
  ret$d = d
  ret$samples = d[ , 4:ncol(d)]
  ret
}

  # lw = d$V2[burnin:nrow(d)]
  # w = exp(lw - min(lw))
  # w = w/sum(w)
  # ef = ewcdf(samples, w)

