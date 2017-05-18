import java.util.Scanner;
import java.io.File;
import java.io.IOException;

public class Main {

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

	static double annualVolatility(int i) {
		return Math.sqrt(monthlyVariance(i)*12);
	}

	static double[][] covarianceMatrix() {
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
			}
			M[j][i] = M[i][j];
		}
		return M;
	}

	double gradientDescent(double[] ) {
		
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
	}
}