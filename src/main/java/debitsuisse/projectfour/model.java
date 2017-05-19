package debitsuisse.projectfour;

import java.util.Scanner;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;

public class model {
	
	//given data
	HashMap<String,Integer> indices;
	int companies, months;
	String[] names;
	double[][] price;
	//calculated data
	double[][] correlation_matrix;
	double[][] return_month;
	double[] monthly_variance;
	double[] monthly_volatility;
	double[] annual_volatility;
	double[] monthly_avg_return;
	double[] annual_avg_return;

	double monthlyAvgReturn(int i) {
		double d = 0;
		for(int j = 0; j < months-1; ++j) {
			d += return_month[i][j];
		}
		return d / (months-1);
	}

	double monthlyVariance(int i) {
		double var = 0;
		double mar = monthly_avg_return[i];
		for(int j = 0; j < months-1; ++j) {
			double d = return_month[i][j]-mar;
			var += d*d;
		}
		return var / months-1;
	}

	double[][] correlationMatrix() {
		double[][] V = new double[companies][months-1];
		double[][] M = new double[companies][companies];
		for(int i = 0; i < companies; ++i) {
			double mar = monthly_avg_return[i];
			for(int j = 0; j < months-1; ++j)
				V[i][j] = return_month[i][j]-mar;
		}
		for(int i = 0; i < companies; ++i) {
			M[i][i] = 1;
			for(int j = i+1; j < companies; ++j) {
				for(int k = 0; k < months-1; ++k) {
					M[i][j] += V[i][k] * V[j][k];
				}
				M[i][j] /= (months-1)*monthly_volatility[i]*monthly_volatility[j];
				M[j][i] = M[i][j];
			}
		}
		return M;
	}

	double ratio(String c1, String c2, double p) {
		int i=indices.get(c1),j=indices.get(c2);
		return Math.sqrt(p*p*annual_volatility[i] + (1-p)*(1-p)*annual_volatility[j] + 2*p*(1-p)*correlation_matrix[i][j]*annual_volatility[i]*annual_volatility[j]);
	}

	double[] allRatios(String c1, String c2) {
		double[] ans = new double[101];
		for(int i = 0; i <= 100; ++i) {
			ans[i] = ratio(c1,c2,i/100.0);
		}
		return ans;
	}

	double[] gradientDescent(double[] r, double[] v, int steps, double alpha) {
		double[] c = new double[companies-1];
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

	public static void main(String[] args) {
		model m = new model();
	}

	public model() {
		try {
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
				indices.put(names[i],i);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return_month = new double[companies][months-1];
		monthly_avg_return = new double[companies];
		annual_avg_return = new double[companies];
		monthly_variance = new double[companies];
		annual_volatility = new double[companies];
		monthly_volatility = new double[companies];
		for(int i = 0; i < companies; ++i)
			for(int j = 0; j < months-1; ++j)
				return_month[i][j] = (price[i][j+1] - price[i][j])/price[i][j]*100;
		for(int i = 0; i < companies; ++i) {
			monthly_avg_return[i] = monthlyAvgReturn(i);
			annual_avg_return[i] = monthly_avg_return[i]*12;
		}
		for(int i = 0; i < companies; ++i) {
			monthly_variance[i] = monthlyVariance(i);
			monthly_volatility[i] = Math.sqrt(monthly_variance[i]);
			annual_volatility[i] = monthly_volatility[i]*Math.sqrt(12);
		}
		correlation_matrix = correlationMatrix();

		DecimalFormat df = new DecimalFormat(".00");

		for(int i = 0; i < companies; ++i) {
			for(int j = 0; j < companies; ++j) {
				System.out.println(names[i] + ", " + names[j] + " -> " + df.format(correlation_matrix[i][j]));
			}
		}
	}
}