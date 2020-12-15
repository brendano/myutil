LDFLAGS := -shared -lpython2.7 -pthread -lgsl -lcblas -lgslcblas

# homebrew
ifneq (,$(findstring Darwin,$(shell uname)))
  PY += -I/usr/local/include/python2.7 -I/usr/local/lib/python2.7/site-packages/numpy/core/include
endif

# blacklight
ifneq (,$(findstring blacklight,$(shell hostname)))
  PY += -I/usr/local/packages/python/2.7.1/lib/python2.7/site-packages/numpy-1.6.1-py2.7-linux-x86_64.egg/numpy/core/include -I/usr/local/packages/python/2.7.1/include/python2.7
  LDFLAGS += -L/usr/local/packages/python/2.7.1/lib

  LDFLAGS += -L/usr/local/packages/gsl-1.14/lib -L$(ATLAS_LIB)
  PY += -I/usr/local/packages/gsl-1.14/include -I$(ATLAS_INC)
endif

.SECONDARY:

CFLAGS := -pthread -fwrapv -fno-strict-aliasing -fPIC -fopenmp -O2
# CC := gcc
CC := gcc -DPYREX_WITHOUT_ASSERTIONS

MODULES := lda.pyx ext.pyx

default: $(patsubst %.pyx,%.so,$(MODULES))

%.html : %.pyx
	cython -a $< -o $@
	open $@

%.c : %.pyx
	cython $< -o $@

%.o : %.c
	$(CC) -c $(CFLAGS) $(PY) $< -o $@

%.so : %.o
	$(CC) $(CFLAGS) $(LDFLAGS) $< -o $@

TO_CLEAN += $(patsubst %.pyx,%.o,$(MODULES))
TO_CLEAN += $(patsubst %.pyx,%.so,$(MODULES))
TO_CLEAN += $(patsubst %.pyx,%.c,$(MODULES))
TO_CLEAN += $(patsubst %.pyx,%.html,$(MODULES))

clean:
	rm -f $(TO_CLEAN)

tags:
	ctags **/*.{py,pyx}
