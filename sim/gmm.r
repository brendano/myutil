N = 1000
K = 1

# X = rnorm(N, sd=.1)
# Z = rep(1,N)

mix = runif(K); mix=mix/sum(mix)
means = rnorm(K)
vars = rexp(K)
# mix = c(.5, .5)
# means = c(-2, 2)
# vars  = c(1,1)
Z = sample.int(K,N, replace=TRUE)
X = rep(NA, N)
for (k in 1:K) {
  n = sum(Z==k)
  X[Z==k] = rnorm(n, mean=means[k], sd=sqrt(vars[k]))
}

# qplot(X,colour=Z,data=data.frame(X,Z=factor(Z)))
write.tsv(X, "sim/X")
