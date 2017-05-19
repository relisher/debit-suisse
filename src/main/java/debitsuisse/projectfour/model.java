package debitsuisse.projectfour;

import java.util.Scanner;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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
    double[] annual_variance;

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
    
    public String[] getNames() {
        return names;
    }

	public double monthlyVariance(int i) {
		double var = 0;
		double mar = monthly_avg_return[i];
		for(int j = 0; j < months-1; ++j) {
			double d = return_month[i][j]-mar;
			var += d*d;
		}
		return var / (months-1);
	}
        
    public double[][] getCorrelationMatrix() {
        return correlation_matrix;
    }
    
    public double monthlyVolatility(String c1) {
        return monthly_volatility[getCompanyValue(c1)];
    }
    
    public double annualVolatility(String c1) {
        return annual_volatility[getCompanyValue(c1)];
    }
    
    public int getCompanyValue(String c1) {
        return indices.get(c1);
    }

	double[][] correlationMatrix() {
		double[][] V = new double[companies][months-1];
		double[][] M = new double[companies][companies];
		for(int i = 0; i < companies; ++i) {
			double mar = monthly_avg_return[i];
			for(int j = 0; j < months-1; ++j) {
				V[i][j] = return_month[i][j]-mar;
			}
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

	//calculate the volatility given your portfolio consists of
	//p of company c1 and (1-p) of company c2
	double ratio(String c1, String c2, double p) {
        int i=indices.get(c1),j=indices.get(c2);
        return Math.sqrt(p*p*annual_volatility[i]*annual_volatility[i] + 
                (1-p)*(1-p)*annual_volatility[j]*annual_volatility[j] + 
                2*p*(1-p)*correlation_matrix[i][j]*
                        annual_volatility[i]*annual_volatility[j])*100;
    }

    //return paired volatility for all percentages 0,1,2,...,99,100
	double[] allRatios(String c1, String c2) {
		double[] ans = new double[101];
		for(int i = 0; i <= 100; ++i) {
			ans[i] = ratio(c1,c2,i/100.0);
		}
		return ans;
	}
    
	//paired returns for all percentages 0,1,2,...,99,100
    double[] allReturns(String c1, String c2) {
        double[] ans = new double[101];
        for(int i = 0; i <= 100; i++) {
            ans[i] = (annualAvgReturn(c1) * i) + (annualAvgReturn(c2) * (100-i));
        }
        return ans;
    }

    //the return value of a specific weighting c

	double weightingReturn(double[] c) {
		double ret = 0;
		for(int i = 0; i < companies; ++i) {
			ret += annual_avg_return[i]*c[i];
		}
		return ret;
	}
 
        //the volatility of a specific weighting c
	public double weightingVolatility(double[] c) {
		return Math.sqrt(weightingVariance(c));
	}
        
	//does the weighting fit the constraints?
	private boolean weightingOk(double[] c, double cutoff) {
		double payoff = 0;
		double w = 1;
		for(int i = 0; i < companies-1; ++i) {
			w -= c[i];
			if(c[i] < 0 || w < 0) return false;
			payoff += annual_avg_return[i]*c[i];
		}
		payoff += w*annual_avg_return[companies-1];
		return payoff >= cutoff;
	}

	//the variance of a specific weighting c
	private double weightingVariance(double[] c) {
		double var = 0;
		double w = 1;
		for(int i = 0; i < companies-1; ++i) {
			var += c[i]*c[i]*annual_variance[i];
			w -= c[i];
		}
		var += w*w*annual_variance[companies-1];
		for(int i = 1; i < companies-1; ++i) {
			for(int j = 0; j < i; ++j) {
				var += 2*c[i]*c[j]*Math.sqrt(annual_variance[i]*annual_variance[j])*correlation_matrix[i][j];
			}
		}
		for(int j = 0; j < companies-1; ++j) {
			var += 2*c[j]*w*Math.sqrt(annual_variance[j]*annual_variance[companies-1])*correlation_matrix[j][companies-1];
		}
		return var;
	}

	double[] gradientDescent(double cutoff, int steps, double alpha) {
		double  p = 0.4;
		double[] c = new double[companies-1],cs = new double[companies-1];
		//find feasible solution
		int mx = 0;
		for(int i = 1; i < annual_avg_return.length; ++i) {
			if(annual_avg_return[mx] < annual_avg_return[i]) {
				mx = i;
			}
		}
		if(annual_avg_return[mx] < cutoff) {
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
		} while(!weightingOk(c,cutoff));

		//local search
		double best_var = weightingVariance(c);
		for(int i = steps; i > 0; --i) {
			double rate = alpha;
			for(int j = 0; j < companies-1; ++j) {
				double wt = c[j], w = 1;
				for(int k = 0; k < companies-1; ++k) {
					w -= c[k];
					if(k==i) continue;
					wt += c[k]*correlation_matrix[j][k]*annual_volatility[k];
				}
				wt -= w*correlation_matrix[j][companies-1]*annual_volatility[companies-1];
				wt *= annual_volatility[j];

				cs[j] = c[j] + rate*(-p*wt + (1-p)*(Math.random()*2-1));
			}
			if(weightingOk(cs,cutoff)) {
				double var = weightingVariance(cs);
				if(var < best_var) {
					best_var = var;
					c = cs;
				}
			}
		}
		return c;
	}

	double[] learnModel(double cutoff) {
		double[] c = new double[companies-1],d=new double[companies-1];
		double c_var = weightingVariance(c);
		c[0] = -1;
		for(int i = 0; i < 2000; ++i) {
			d = gradientDescent(cutoff,500,0.8);
			double cw_var = weightingVariance(d);
			if(c[0] == -1 || cw_var < c_var) {
				c_var = cw_var;
				c = d;
			}
		}
                d = new double[companies];
                d[companies-1] = 1;
		for(int i = 0; i < companies-1; ++i) {
            d[i] = c[i];
            d[companies-1] -= d[i];
        }
		return d;
	}

	public static void main(String[] args) {
		model m = new model();
	}

	public model() {
		try {
			Scanner s = new Scanner(new File("data 2.txt"));
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
		//precalculate lots of info
		return_month = new double[companies][months-1];
		monthly_avg_return = new double[companies];
		annual_avg_return = new double[companies];
		monthly_variance = new double[companies];
		annual_volatility = new double[companies];
		annual_variance = new double[companies];
		monthly_volatility = new double[companies];
		for(int i = 0; i < companies; ++i)
			for(int j = 0; j < months-1; ++j)
				return_month[i][j] = (price[i][j+1] - price[i][j])/price[i][j];
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
	}
}