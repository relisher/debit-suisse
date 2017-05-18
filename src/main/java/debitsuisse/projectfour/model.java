import java.util.Scanner;
import java.io.File;
import java.io.IOException;

public class model {

	static int companies, months;
	static String[] names;
	static double[][] price;

	static double returnMonth(int i, int j) {
		return (price[i][j+1] - price[i][j])/price[i][j]*100;
	}

	static double monthlyAvgReturn(int i) {
		double d = 0;
		for(int j = 0; j < months-1; ++j) {
			d += returnMonth(i,j);
		}
		return d / (months-1);
	}

	static double annualAvgReturn(int i) {
		return monthlyAvgReturn(i)*12;
	}

	static double monthlyVariance(int i) {
		double var = 0;
		double mar = monthlyAvgReturn(i);
		for(int j = 0; j < months-1; ++j) {
			double d = returnMonth(i,j)-mar;
			var += d*d;
		}
		return var / months-1;
	}

	static double monthlyVolatility(int i) {
		return Math.sqrt(monthlyVariance(i));
	}

	static double annualVariance(int i) {
		return monthlyVariance(i)*12;
	}

	static double annualVolatility(int i) {
		return Math.sqrt(annualVariance(i));
	}

	static double[][] correlationMatrix() {
		double[] vol = new double[company];
		for(int i = 0; i < company; ++i)
			vol[i] = monthlyVolatility(i);
		double[][] V = new double[companies][months-1];
		double[][] M = new double[companies][companies];
		for(int i = 0; i < companies; ++i) {
			double mar = monthlyAvgReturn(i);
			for(int j = 0; j < months-1; ++j)
				V[i][j] = returnMonth(i,j)-mar;
		}
		for(int i = 0; i < companies; ++i) {
			M[i][i] = 1;
			for(int j = i+1; j < companies; ++j) {
				for(int k = 0; k < months-1; ++k) {
					M[i][j] += V[i][k] * V[j][k];
				}
				M[i][j] /= vol[i]*vol[j];
				M[j][i] = M[i][j];
			}
		}
		return M;
	}

	static double get_w(double[] c) {
		double w = 1;
		for(double r : c)
			w -= r;
		return w;
	}

	static void project(double[] c) {
		for(int i = 0; i < companies-1; ++i)
			if(c[i] < 0)
				c[i] = 0;
		double sm = 0;
		for(int i = 0; i < companies-1; ++i)
			sm += c[i];
		if(sm > 1)
			for(int i = 0; i < companies-1; ++i)
				c[i] /= sm;
	}

	static double[] gradientDescent(double[] r, double[] v, int steps, double alpha) {
		//generate initial configuration
		double[] c = new double[companies-1];
		// double s = Math.random();
		// for(int i = 0; i < n-1; ++i) {
		// 	c[i] = Math.random();
		// 	s += c[i];
		// }
		// for(int i = 0; i < n-1; ++i)
		// 	c[i] /= s;
		for(; steps>0; --steps) {
			double w = get_w(c);
			for(int j = 0; j < companies-1; ++j)
				w -= c[j];
			for(int j = 0; j < companies-1; ++j) {
				c[j] += alpha*(c[j]*v[j] - w*v[companies-1]);
				System.out.println(j + " : " + alpha*(c[j]*v[j] - w*v[companies-1]));
			}
			project(c);
		}
		return c;
	}

	public static void main(String[] args) throws Exception {
		Scanner s = new Scanner(new File("data.txt"));
		companies = s.nextInt();
		months = s.nextInt();

		names = new String[companies];
		price = new double[companies][months];

		for(int i = 0; i < companies; ++i) {
			for(int j = 0; j < months; ++j) {
				names[i] = s.next();
				s.next();
				price[i][j] = s.nextDouble();
			}
		}

		double[] r = new double[companies],v = new double[companies];
		for(int i = 0; i < companies; ++i) {
			r[i] = annualAvgReturn(i);
			v[i] = annualVariance(i);
		}

		double[] c = gradientDescent(r,v,1,0.01);
		for(int i = 0; i < companies-1; ++i) {
			System.out.println(c[i]);
		}
		System.out.println(get_w(c));
	}
}