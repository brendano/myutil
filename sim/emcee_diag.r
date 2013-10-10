# cat("Per-variable effective sizes.. these are overestimates..\n")
# es = daply(d, .(chain), function(x) {
#   m = mcmc(x[,4:ncol(d)])
#   effectiveSize(m)
# })
# print(colSums(es))
# print(colMeans(es))


# ev = ddply(d, .(chain), function(x) {
#   ldply(1:J, function(j) {
#     s = x[,3+j]
#     s = window(s, start=burnin+1)
#     data.frame(j=j, var=spectrum0.ar(s)$spec / length(s))
#   })
# })

nsamp = max(d$t)
frac1end   = floor((nsamp - burnin) * 0.1) + burnin
frac2start = floor((nsamp - burnin) * 0.5) + burnin
printf("%d to %d, versus %d to %d\n", burnin+1, frac1end, frac2start, nsamp)
rt = ldply(1:J, function(j) {
  start_values = subset(d, t > burnin & t <= frac1end)[,3+j]
  end_values   = subset(d, t >= frac2start)[,3+j]
  mean1 = mean(start_values)
  mean2 = mean(end_values)
  data.frame(
    mean1=mean1, mean2=mean2
    # ttest_pval = t.test(start_values, end_values)$p.value
    # ks_pval   = ks.test(start_values, end_values)$p.value
  )
})
print(rt)

