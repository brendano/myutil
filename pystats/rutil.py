from rpy import r


def plot_wrapper(f, clobber=('xlab','ylab','main')):
  # Because rpy.r calls throw number crap everywhere
  def _f(*a, **k):
    for arg in clobber:
      k[arg] = k.get(arg,'')
    ret = getattr(r, f)(*a, **k)
    return None
  return _f

hist = plot_wrapper('hist')
lines = plot_wrapper('lines')
boxplot = plot_wrapper('boxplot')
plot = plot_wrapper('plot')
#qqnorm = wrapper('qqnorm')
qqplot = plot_wrapper('qqplot')
acf = plot_wrapper('acf',['main'])

