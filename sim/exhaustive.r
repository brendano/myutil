betalim = c(-2,2)
step = .1
gridlines = seq(betalim[1], betalim[2], step)
g = expand.grid(gridlines, gridlines)
g=g[order(g[,1],g[,2]),]
scores = as.matrix(g) %*% t(X)
probs = exp(scores) / (1 + exp(scores))
lls = apply(log(1-probs[,Y==0]), 1, sum)
lls = lls + apply(log(probs[,Y==1]), 1, sum)
lls = lls - 0.5 * apply(g**2, 1, sum)
