sim = within(list(), {
N = 100
J = 100
B = rnorm(J)
# B = c(10,10,1,0,-1)
# X = matrix(as.integer(runif(N*J) < 0.1), ncol=J)
X = matrix(runif(N*J), ncol=J)
score = as.vector(X %*% B)

## logit
p = exp(score) / (1+exp(score))
Y = as.integer(runif(N) < p)

## probit
# Y = as.integer(rnorm(N) + score > 0)

})

write.tsv(sim$X, "sim/X")
write.tsv(sim$Y, "sim/Y")
write.tsv(sim$B, "sim/B")
