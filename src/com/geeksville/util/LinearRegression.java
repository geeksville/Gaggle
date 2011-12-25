package com.geeksville.util;

import java.util.ArrayDeque;
import java.util.Queue;

public class LinearRegression {

	static class Sample {
		public long x;
		public float y;

		public Sample(long x, float y) {
			this.x = x;
			this.y = y;
		}
	}

	private Queue<Sample> samples = new ArrayDeque<Sample>();

	// / Invariants
	long sumx;
	double sumy;

	private long xspan = 2 * 1000; // typically milliseconds

	public float getXspan() {
		return xspan;
	}

	// / We will keep only the most recent yspan interval around
	public void setXspan(long xspan) {
		this.xspan = xspan;
	}

	public void addSample(long x, float y) {
		synchronized (samples) {
			{
				Sample s = new Sample(x, y);
				sumx += x;
				sumy += y;
				samples.add(s);
			}

			// Cull old entries
			long oldest = x - xspan;
			while (samples.peek().x < oldest) {
				Sample s = samples.remove();
				sumx -= s.x;
				sumy -= s.y;
			}
		}
	}

	public float getSlope() {
		synchronized (samples) {
			double xbar = sumx / samples.size();
			double ybar = sumy / samples.size();
			double xxbar = 0.0, xybar = 0.0;
			for (Sample s : samples) {
				xxbar += (s.x - xbar) * (s.x - xbar);
				xybar += (s.x - xbar) * (s.y - ybar);
			}
			double beta1 = xybar / xxbar;

			return (float) beta1;
		}
	}

	/*
	 * int MAXN = 1000; int n = 0; double[] x = new double[MAXN]; double[] y =
	 * new double[MAXN];
	 * 
	 * // first pass: read in data, compute xbar and ybar double sumx = 0.0,
	 * sumy = 0.0, sumx2 = 0.0; while(!StdIn.isEmpty()) { x[n] =
	 * StdIn.readDouble(); y[n] = StdIn.readDouble(); sumx += x[n]; sumx2 +=
	 * x[n] * x[n]; sumy += y[n]; n++; } double xbar = sumx / n; double ybar =
	 * sumy / n;
	 * 
	 * // second pass: compute summary statistics double xxbar = 0.0, yybar =
	 * 0.0, xybar = 0.0; for (int i = 0; i < n; i++) { xxbar += (x[i] - xbar) *
	 * (x[i] - xbar); yybar += (y[i] - ybar) * (y[i] - ybar); xybar += (x[i] -
	 * xbar) * (y[i] - ybar); } double beta1 = xybar / xxbar; double beta0 =
	 * ybar - beta1 * xbar;
	 * 
	 * // print results System.out.println("y   = " + beta1 + " * x + " +
	 * beta0);
	 * 
	 * // analyze results int df = n - 2; double rss = 0.0; // residual sum of
	 * squares double ssr = 0.0; // regression sum of squares for (int i = 0; i
	 * < n; i++) { double fit = beta1*x[i] + beta0; rss += (fit - y[i]) * (fit -
	 * y[i]); ssr += (fit - ybar) * (fit - ybar); } double R2 = ssr / yybar;
	 * double svar = rss / df; double svar1 = svar / xxbar; double svar0 =
	 * svar/n + xbar*xbar*svar1; System.out.println("R^2                 = " +
	 * R2); System.out.println("std error of beta_1 = " + Math.sqrt(svar1));
	 * System.out.println("std error of beta_0 = " + Math.sqrt(svar0)); svar0 =
	 * svar * sumx2 / (n * xxbar); System.out.println("std error of beta_0 = " +
	 * Math.sqrt(svar0));
	 * 
	 * System.out.println("SSTO = " + yybar); System.out.println("SSE  = " +
	 * rss); System.out.println("SSR  = " + ssr);
	 */
}
