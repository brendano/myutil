burnin = 1
lastiter = nrow(allruns[[1]]$samples)
stopifnot(burnin < lastiter)
# lastiter = burnin+1000

runs = llply(allruns, function(x) {
  if ( !is.null(x$t) && !is.null(x$d)) {
    ## weirder multichain case, we don't really handle correctly yet
    x$d = subset(x$d, t > burnin)
  }
  x$samples = x$samples[(burnin+1):lastiter,]
  x$w = x$w[(burnin+1):lastiter]
  x
})

printf("burnin %d numsamples %d\n", burnin, nrow(runs[[1]]$samples))

## run-level stats

m = ldply(runs, function(x) {
  postmean = colMeans(x$samples)
  # postmean = sapply(1:ncol(x$samples), function(j)
  #             weighted.mean(x$samples[,j], x$w))
  data.frame(
       mse = mean((postmean-x$B)**2)
  )
})

printf("mean mse: %g (%.2g)\n", mean(m$mse), sd(m$mse) / sqrt(nrow(m) * ncol(runs[[1]]$samples)))


## variable-level stats

r = ldply(runs, function(x) {
  J = ncol(x$samples)
  ldply(1:J, function(j) {
    s = x$samples[,j]
    # s = s[seq(1,length(s),5)]
    n = length(s)
    s1 = s[ 1 : floor(n*0.1) ]
    s2 = s[ ceiling(n*0.5) : n ]
    ef = ecdf(s)
    # ef = ewcdf(s, x$w)
    data.frame(
      geweke_z = as.vector(geweke.diag(mcmc(s))$z),
      # ks_p = ks.test(s1, s2)$p.value,
      mean1=mean(s1), mean2=mean(s2),
      q = ef(x$B[j])
    )})
},.progress='text')

# par(mfrow=c(2,2),mar=c(4,2,1,1))
plot(seq(0,1,length.out=nrow(r)), sort(r$q), ylim=c(0,1), main="posterior quantile uniformity")
abline(a=0,b=1,col='blue')

printf("Posterior quantile meandiff: %.3g\n", qqstats(r$q, seq(0,1,length=nrow(r)))$meandiff)
cat("Posterior quantiles are uniform?\n")
print(ks.test(r$q, punif)$p.value)

# plot(r$mean1, r$mean2, main="geweke means"); abline(a=0,b=1,col='blue')
# 
# # cat("Geweke bins KS pvalues\n")
# # print(quantile(r$ks_p,c(0,.01,.05,.1, .5,1)))
#     
# qqnorm(r$geweke_z, main="geweke z normality"); abline(a=0,b=1,col='blue')
# cat("Geweke Z-scores normal?\n")
# print(shapiro.test(r$geweke_z)$p.value)
# 
