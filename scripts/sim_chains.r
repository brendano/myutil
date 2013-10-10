T = 5
K = 2
obsF = matrix(rexp(T*K), nrow=T)
transF=matrix(rexp(K*K), nrow=K)
write.tsv(obsF, "obsF")
write.tsv(transF, "transF")

