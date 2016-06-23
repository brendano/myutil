import statutil

def poch(N=700):
    for i in range(N):
        for j in range(N):
            print "%d %d %.3f" % (i,j,statutil.poch(i,j))
poch()

# def slice():
