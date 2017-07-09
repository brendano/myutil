.SECONDARY:

CC := gcc 

CFLAGS  += $(shell pkg-config --cflags python)
LDFLAGS += $(shell pkg-config --libs python)
SP = $(shell python -c "from distutils.sysconfig import get_python_lib; print(get_python_lib())")
CFLAGS += -I$(SP)/numpy/core/include

LDFLAGS += -shared -pthread 
CFLAGS += -pthread -fwrapv -fno-strict-aliasing -fPIC -fopenmp -O2
CFLAGS += -DPYREX_WITHOUT_ASSERTIONS

ifneq (,$(findstring blacklight,$(shell hostname)))
  CFLAGS += -I$(SP)/numpy-1.6.1-py2.7-linux-x86_64.egg/numpy/core/include
  CFLAGS += -I/usr/local/packages/python/2.7.1/include/python2.7
  LDFLAGS += -L/usr/local/packages/python/2.7.1/lib $(GSL_LIB) -L$(ATLAS_LIB)
  CFLAGS += $(GSL_INC) -I$(ATLAS_INC)
  LDFLAGS += -lgsl -lcblas -lgslcblas
endif

# vim: ft=make
