package util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Stack;

import com.google.common.base.Function;

/**
 * Inference algorithms for first-order discrete sequence models.
 * Takes factor scores as inputs.  (It's your job to compute them.)
 * These are intended for: hmm, crf, memm, and other variants.
 * 
 * notation conventions
 * T: length of chain. t=0..(T-1)
 * K: number of latent classes.  k=0..(K-1)
 * All factors are in exp space -- because we use per-timestep renormalization, which is much faster than log-space arithmetic.  See Rabiner 1989.
 * (Thus all factor inputs are non-negative.  we could use negative values for NA's or hard-constraint-zeros or something.)
 * 
 * We have no notion of special start or stop states.  you have to handle that yourself: build them into the first or last factor tables yourself.
 * We have no notion of learning.  This might be a subroutine for that.
 * 
 * @author brendano
 */
public class ChainInfer {
	
	/**
	 * Viterbi algorithm
	 * @param obsFactors:  size T x K;  (t, class)
	 * @param transFactors:  size K x K;  (class@t, class@{t+1})
	 *            ... note the algorithm can be extended to size (T-1) x K x K, there is commented out code for this
	 * @return discrete sequence length T, each value in {0..(K-1)}
	 */
	static int[] viterbi(double[][] obsFactors, double[][] transFactors) {
		if (obsFactors.length==0) return new int[0];
		final int T = obsFactors.length;
		final int K = obsFactors[0].length;
		
		// viterbi and backpointer tables
		double[][] V = new double[T][K];
		int[][] backs = new int[T][K];  // backs[0] will go unused
		for (int k=0; k<K; k++)
			V[0][k] = obsFactors[0][k];
		
		double[] scores = new double[K];
		
		for (int t=1; t<T; t++) {
			for (int k=0; k<K; k++) {
				for (int prev=0; prev<K; prev++) {
//					scores[prev] = V[t-1][prev] * transFactors[t-1][prev][k] * obsFactors[t][k];
					scores[prev] = V[t-1][prev] * transFactors[prev][k] * obsFactors[t][k];
				}
				int best_prev = Arr.argmax(scores);
				double best_score = scores[best_prev];
				V[t][k] = best_score;
				backs[t][k] = best_prev;
			}
		}
		
		int[] path = new int[T];
		path[T-1] = Arr.argmax(V[T-1]);
		for (int t=T-2; t>=0; t--) {
			path[t] = backs[t+1][path[t+1]];
		}
		
		return path;
	}
	
	static int[] exhaustiveSearch(final double[][] obsFactors, final double[][] transFactors) {
		final int T = obsFactors.length, K = obsFactors[0].length;
		final int[][] best = new int[][]{ null };
		final double[] bestScore = new double[]{ Double.NEGATIVE_INFINITY };
		exhaustiveCalls(T,K, new Function<int[],Object>() {
			@Override
			public Object apply(int[] ys) {
				double s = computeSolutionScore(ys, obsFactors, transFactors);
				if (s > bestScore[0]) {
					best[0] = Arr.copy(ys);
					bestScore[0] = s;
				}
				return null;
			}
		}
		);
		return best[0];
	}

	/** score is unnorm logprob. assume EXP scaled factor potentials though (since that's more convenient for the other algos in this file). */
	static double computeSolutionScore(int[] ys, double[][] obsFactors, double[][] transFactors) {
		int T = ys.length;
		double score = 0;
		for (int t=0; t<T; t++) {
			score += Math.log(obsFactors[t][ys[t]]);
			if (t<T-1)
				score += Math.log(transFactors[ys[t]][ys[t+1]]);
		}
		return score;
	}
	
//	/** this would be super easy with a python generator */
//	public static Iterable<int[]> exhaustiveIterator(int T, int K) {
//		assert T > 0 : "harder to code the T=0 case correctly, don't use";
//		assert K > 0;
//		Deque<int[]> stack = new ArrayDeque();
//		return new Iterable<int[]>() {
//			@Override
//			public Iterator<int[]> iterator() {
//				return new Iterator<int[]>() {
//
//					@Override
//					public boolean hasNext() {
//						return false;
//					}
//
//					@Override
//					public int[] next() {
//						// TODO Auto-generated method stub
//						return null;
//					}
//
//					@Override
//					public void remove() {
//						throw new RuntimeException("unimpemented");
//					}
//					
//				}
//			}
//			
//		};
//	}
//	
//	static class AgendaBasedIterable<T> implements Iterable<T> {
//
//		@Override
//		public Iterator<T> iterator() {
//			// TODO Auto-generated method stub
//			return null;
//		}
//		
//	}
	
	/**
	 * WARNING, this RESUSES the same callback array.  if you want to store it, need to copy it!!
	 *  would be nicer to invert this into a guava-friendly iterator form. how?
	 * need an explicit agenda structure, i think.
	 * function recursion is just so much easier to write. python generators make it easy to switch between the modes but alas.
	 */
	public static void exhaustiveCalls(int T, int K, Function<int[], Object> callback) {
		int[] values = Arr.repInts(-1, T);
		exhaustiveCallsRecurs(0, T, K, values, callback);
	}

	/** note this overwrite values[] */
	static void exhaustiveCallsRecurs(final int t, final int T, final int K, int[] values, Function<int[], Object> callback) {
		if (t==T) {
			callback.apply(values);
		} else {
			for (int k=0; k<K; k++) {
				values[t] = k;
				exhaustiveCallsRecurs(t+1, T, K, values, callback);
			}
		}
	}
	
	// testing: ensure this gives the correct number of outputs: ./java.sh util.ChainInfer | wc -l 
//	public static void main(String[] args) { testExhaustiveCalls(); }
	
	static void testExhaustiveCalls() {
		exhaustiveCalls(3, 3, new Function<int[],Object>() {
			@Override
			public Object apply(int[] values) {
				U.p(values);
				return null;
			}
		}
		);
	}
	
	/** My test procedure.  make sure the algos agree.
	 * 
~/myutil/chains % Rscript sim_chains.r && ../java.sh util.ChainInfer obsF transF
Loading required package: lattice
EXHAUS	[0, 1, 1, 1, 0]
VITERBI	[0, 1, 1, 1, 0]
	 */
	static void testInference(String[] args) {
		double[][] obsF = Arr.readDoubleMatrix(args[0]);
		double[][] transF= Arr.readDoubleMatrix(args[1]);
		U.pf("EXHAUS\t");  U.p(exhaustiveSearch(obsF,transF));
		U.pf("VITERBI\t");  U.p(viterbi(obsF,transF));
	}
	public static void main(String[] args) { testInference(args); }
	
	/**
	 * Forward algorithm
	 * @param obsFactors
	 * @param transFactors
	 * @return
	 */
	public static ForwardTables forward(double[][] obsFactors, double[][] transFactors) {
		final int T = obsFactors.length;
		final int K = obsFactors[0].length;
		ForwardTables f = new ForwardTables();
		f.probs = new double[T][K];
		f.incrementalNormalizers = new double[T];
		double Z;

		f.probs[0] = Arr.copy(obsFactors[0]);
		Z = Arr.sum(f.probs[0]);
		Arr.multiplyInPlace(f.probs[0], 1.0/Z);
		f.incrementalNormalizers[0] = Z;
		
		for (int t=1; t<T; t++) {
			for (int k=0; k<K; k++) {
				f.probs[t][k] = obsFactors[t][k];
				for (int prev=0; prev<K; prev++) {
//					f.probs[t][k] *= f.probs[t-1][prev] * transFactors[t-1][prev][k];
					f.probs[t][k] *= f.probs[t-1][prev] * transFactors[prev][k];
				}
			}
			Z = Arr.sum(f.probs[t]);
			Arr.multiplyInPlace(f.probs[t], 1/Z);
			f.incrementalNormalizers[t] = Z;
		}
		
		return f;
	}

	/** convention for this data structure:
	 * 'probs' is normalized for each timestep!
	 * and 'incrementalNormalizers' lets you reconstruct the true forward probs, if desired.
	 */
	static class ForwardTables {
		/** size T x K */
		public double[][] probs;
		/** size T */
		public double[] incrementalNormalizers;
	}
	
	public static double[][] backward(ForwardTables f, double[][] transFactors) {
		double[][] labelMarginals = Arr.copy(f.probs);
		final int T = f.probs.length;
		if (T==0) return labelMarginals;
		final int K = f.probs[0].length;

		for (int t=T-2; t>=0; t--) {
			for (int k=0; k<K; k++) {
				for (int next=0; next<K; next++) {
					labelMarginals[t][k] *= transFactors[k][next]; // um this might be wrong
				}
			}
		}
		return labelMarginals;
	}
	
	static class Marginals {
		/** size T x K: p(y_t) */
		double[][] labelMarginals;
		/** size (T-1) x K x K:  p(y_t, y_t+1) */
		double[][][] pairMarginals;
	}

	/**
	 * @return vector of marginals: for each t, p(y_t | x_1 .. x_T).
	 *
	 * @param obsFactors
	 * @param transFactors
	 */
	public static double[][] forwardBackward(double[][] obsFactors, double[][] transFactors) {
		ForwardTables f = forward(obsFactors, transFactors);
		return backward(f, transFactors);
	}

}
