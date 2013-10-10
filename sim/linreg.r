sim = within(list(), {
  N = 10
  J = 2
  B = rnorm(J)
  X = matrix(rnorm(N*J), ncol=J)
  Y = as.vector(X %*% B) + rnorm(N)
})

write.tsv(sim$X, "sim/X")
write.tsv(sim$Y, "sim/Y")
write.tsv(sim$B, "sim/B")
