package debitsuisse.projectfour;

import java.util.Scanner;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class model {
	
	//given data
	Map<String,Integer> indices = new HashMap<>();;
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
        
    public double annualAvgReturn(String c1) {
        return annual_avg_return[indices.get(c1)];
    }

	double monthlyVariance(int i) {
		double var = 0;
		double mar = monthly_avg_return[i];
		for(int j = 0; j < months-1; ++j) {
			double d = return_month[i][j]-mar;
			var += d*d;
		}
		return var / (months-1);
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
            return Math.sqrt(p*p*annual_volatility[i]*annual_volatility[i] + 
                    (1-p)*(1-p)*annual_volatility[j]*annual_volatility[j] + 
                    2*p*(1-p)*correlation_matrix[i][j]*
                            annual_volatility[i]*annual_volatility[j]);
        }

	double[] allRatios(String c1, String c2) {
		double[] ans = new double[101];
		for(int i = 0; i <= 100; ++i) {
			ans[i] = ratio(c1,c2,i/100.0);
		}
		return ans;
	}
        
        double[] allReturns(String c1, String c2) {
            double[] ans = new double[101];
            for(int i = 0; i <= 100; i++) {
                ans[i] = (annualAvgReturn(c1) * i/100.0) + (annualAvgReturn(c2) * (100-1)/100.0);
            }
            return ans;
        }

	private double sample_return(double[] r, double[] c) {
		double ret = 0, w = 1;
		for(int i = 0; i < companies-1; ++i) {
			w -= c[i];
			ret += r[i]*c[i];
		}
		return ret+w*r[companies-1];
	}

	private boolean sample_ok(double[] r, double[] c, double cutoff) {
		double payoff = 0;
		double w = 1;
		for(int i = 0; i < companies-1; ++i) {
			w -= c[i];
			if(c[i] < 0 || w < 0) return false;
			payoff += r[i]*c[i];
		}
		payoff += w*r[companies-1];
		return payoff >= cutoff;
	}

	private double sample_variance(double[] v, double[] c, double cutoff) {
		double var = 0;
		double w = 1;
		for(int i = 0; i < companies-1; ++i) {
			var += c[i]*c[i]*v[i];
			w -= c[i];
		}
		var += w*w*v[companies-1];
		for(int i = 1; i < companies-1; ++i) {
			for(int j = 0; j < i; ++j) {
				var += 2*c[i]*c[j]*Math.sqrt(v[i]*v[j])*correlation_matrix[i][j];
			}
		}
		for(int j = 0; j < companies-1; ++j)
			var += 2*c[j]*w*Math.sqrt(v[j]*v[companies-1])*correlation_matrix[j][companies-1];
		return var;
	}

	double[] gradientDescent(double[] r, double[] v, double cutoff, int steps, double alpha) {
		double[] c = new double[companies-1],cs = new double[companies-1];
		//find feasible solution
		int mx = 0;
		for(int i = 1; i < r.length; ++i) {
			if(r[mx] < r[i])
				mx = i;
		}
		if(r[mx] < cutoff) {
			//no solutions exist
			Arrays.fill(c,-1);
			return c;
		}
		//find random starting sample
		do {
			double w = Math.random(),s = Math.random();
			for(int i = 0; i < companies-1; ++i) {
				c[i] = Math.random();
				w += c[i];
			}
			for(int i = 0; i < companies-1; ++i) {
				c[i] *= s/w;
			}
		} while(!sample_ok(r,c,cutoff));


		//local search
		double best_var = sample_variance(v,c,cutoff);
		for(int i = steps; i > 0; --i) {
			double rate = alpha;
			for(int j = 0; j < companies-1; ++j)
				cs[j] = c[j] + rate*(Math.random()*2-1);
			if(sample_ok(r,cs,cutoff)) {
				double var = sample_variance(v,cs,cutoff);
				if(var < best_var) {
					best_var = var;
					c = cs;
				}
			}
		}
		return c;
	}

	double[] learn_model(double cutoff) {
		double[] c = new double[companies-1],cw;
		double c_var = sample_variance(annual_variance,c,cutoff);
		c[0] = -1;
		for(int i = 0; i < 2000; ++i) {
			cw = gradientDescent(annual_avg_return,annual_variance,cutoff,1000,0.05);
			double cw_var = sample_variance(annual_variance,cw,cutoff);
			// System.out.println("Volatility: " + Math.sqrt(cw_var));
			if(c[0] == -1 || cw_var < c_var) {
				c_var = cw_var;
				c = cw;
			}
		}
		System.out.println(Arrays.toString(c));
		double sm = 0;
		for(int i = 0; i < c.length; ++i) sm += c[i];
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
		annual_variance = new double[companies];
		monthly_volatility = new double[companies];
		for(int i = 0; i < companies; ++i)
			for(int j = 0; j < months-1; ++j)
				return_month[i][j] = (price[i][j+1] - price[i][j])/price[i][j]*100;
		for(int i = 0; i < companies; ++i) {
			monthly_avg_return[i] = monthlyAvgReturn(i);
			annual_avg_return[i] = Math.pow(1+monthly_avg_return[i],12)-1;
		}
		for(int i = 0; i < companies; ++i) {
			monthly_variance[i] = monthlyVariance(i);
			monthly_volatility[i] = Math.sqrt(monthly_variance[i]);
			annual_variance[i] = monthly_variance[i]*12;
			annual_volatility[i] = Math.sqrt(annual_variance[i]);
		}
		correlation_matrix = correlationMatrix();

		learn_model(0.12);
	}
}