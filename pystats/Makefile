include Makefile.inc

MODULES := statutil.pyx

default: lightspeed_util.o $(patsubst %.pyx,%.so,$(MODULES))

%.html : %.pyx
	cython -a $< -o $@
	open $@

%.c : %.pyx
	cython $< -o $@

%.o : %.c
	$(CC) -c $(CFLAGS) $(PY) $< -o $@

%.so : %.o
	$(CC) $(CFLAGS) $(LDFLAGS) lightspeed_util.o $< -o $@

TO_CLEAN += $(patsubst %.pyx,%.o,$(MODULES))
TO_CLEAN += $(patsubst %.pyx,%.so,$(MODULES))
TO_CLEAN += $(patsubst %.pyx,%.c,$(MODULES))
TO_CLEAN += $(patsubst %.pyx,%.html,$(MODULES))

clean:
	rm -f $(TO_CLEAN) *.o

