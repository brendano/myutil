source("sim/gmm.r")
print(qplot(X,colour=factor(Z),data=data.frame(X,Z)))

system("./java.sh DPGMM sim/X 1 | tee log | grep asdfafsd")
e=read.table(pipe("awk '/iter/{i=$NF} i && /^data / {print i,FILENAME,$0}' log | cleanarr"))

x=data.frame(0,'','',0:(length(X)-1),X,sprintf("truth%d",Z))
names(x)=names(e)
e=rbind(e,x)
e$iter=factor(e$V1)

print(
      qplot(V5,colour=factor(V6),data=e)+facet_wrap(~iter)
      )

