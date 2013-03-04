package util;
import com.google.common.base.Function;
import com.google.common.collect.*;

import java.util.*;

public class MCMC {

	/**
	    Slice sampling (Neal 2003; MacKay 2003, sec. 29.7)
	
	    logdist: log-density function of target distribution
	    initial: initial state  (D-dim vector)
	    widths: step sizes for expanding the slice (D-dim vector)
	
	    This is my port of Iain Murray's
	    http://homepages.inf.ed.ac.uk/imurray2/teaching/09mlss/slice_sample.m 
	    which in turn derives from MacKay.  Murray notes where he found bugs in
	    MacKay's pseudocode... good sign
	 **/
	public static List<double[]> slice_sample(
			Function<double[], Double> logdist, double[] initial, double[] widths, int niter) {
		boolean step_out = true;
		final int D = initial.length;
		assert widths.length == D;

		double[] state = initial;
		double log_Px = logdist.apply(state);

		List<double[]> history = Lists.newArrayList();

		for (int itr=0; itr < niter; itr++) {
			//	        U.pf("Slice iter %d stats %s log_Px %f\n",itr, Arr.sf("%.3f", state), log_Px);
			//	        if (itr%100==0) { U.pf("."); System.out.flush(); }
			double log_uprime = Math.log(Math.random()) + log_Px;

			//	        # Sweep through axes
			for (int dd=0; dd < D; dd++) {
				double[] 
				       x_l  = Arrays.copyOf(state, D),
				       x_r	= Arrays.copyOf(state, D),
				       xprime = Arrays.copyOf(state, D);
				//	            # Create a horizontal interval (x_l, x_r) enclosing xx
				double r = Math.random();
				x_l[dd] = state[dd] - r*widths[dd];
				x_r[dd] = state[dd] + (1-r)*widths[dd];
				if (step_out) {
					while (logdist.apply(x_l) > log_uprime)
						x_l[dd] -= widths[dd];
					while (logdist.apply(x_r) > log_uprime)
						x_r[dd] += widths[dd];
				}
				//	            # Inner loop:
				//	            # Propose xprimes and shrink interval until good one is found.
				double zz = 0;
				while (true) {
					zz += 1;
					xprime[dd] = Math.random() * (x_r[dd] - x_l[dd]) + x_l[dd];
					log_Px = logdist.apply(xprime);
					if (log_Px > log_uprime) {
						break;
					} else {
						if (xprime[dd] > state[dd]) {
							x_r[dd] = xprime[dd];
						} else if (xprime[dd] < state[dd]) {
							x_l[dd] = xprime[dd];
						} else {
							assert false : "BUG, shrunk to current position and still not acceptable";
						}
					}
				}
				state[dd] = xprime[dd];
			}
			history.add(Arrays.copyOf(state, D));
		}
		return history;
	}
	
	

	static double triangleLP(double x) {
		boolean in_tri = 0<x && x<20;
		if (!in_tri) return -1e100;
		double p = (x* (x<10 ? 1 : 0) + (20-x)*(x>=10 ? 1 : 0)) / 5;
		return Math.log(p);
	}

	/* Visual testing.  Would be nice to Cook-Gelman-Rubin-style QQplot against truth.
		> x=read.table(pipe("grep SLICE out"))$V2
		> plot(x)
		> acf(x)
		> plot(table(round(x)))
	 */
	static void triangleTest() {
		Function<double[],Double> logdist = new Function<double[],Double>() {
			@Override
			public Double apply(double[] input) {
				return triangleLP(input[0]);
			}
		};
		List<double[]> history = MCMC.slice_sample(logdist, new double[]{5}, new double[]{1}, 10000);
		for (double[] h : history) {
			U.p("SLICE " + h[0]);
		}
	}

	//	public static void main(String[] args) { triangleTest(); }



}
