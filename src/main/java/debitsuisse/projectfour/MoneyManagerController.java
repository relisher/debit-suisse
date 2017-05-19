/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package debitsuisse.projectfour;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author arelin
 */

@CrossOrigin(origins = "*")
@RestController
public class MoneyManagerController {
    
    model m = new model();
    
    @RequestMapping(method = {RequestMethod.GET}, value="/getStockNames")
    public String stockNames() {
        String[] names = m.getNames();
        JsonArrayBuilder createArrayBuilder = Json.createArrayBuilder();
        for(String s : names) {
            createArrayBuilder.add(Json.createObjectBuilder().add("name", s));
        }
        return Json.createObjectBuilder().add("companies", createArrayBuilder)
                .build().toString();      
    }
    
    @RequestMapping(method = {RequestMethod.GET}, value="/getSingleMethod")
    public String byStockBasicInformation(@RequestParam(value = "company", 
            defaultValue="AAPL") String stock) {
        
        JsonArrayBuilder createArrayBuilder = Json.createArrayBuilder();
        String[] names = m.getNames();
        double[][] correlationMatrix = m.getCorrelationMatrix();
        for(int i = 0; i < names.length; i++) {
            JsonObjectBuilder add = 
                    Json.createObjectBuilder().add(names[i], 
                            correlationMatrix[m.getCompanyValue(stock)][i]);
            createArrayBuilder.add(add);

        }
        
        return Json.createObjectBuilder()
                .add("mar", Double.toString(m.monthlyAvgReturn(m.getCompanyValue(stock))*100))
                .add("aar", Double.toString(m.annualAvgReturn(stock)*100))
                .add("mv", Double.toString(m.monthlyVolatility(stock)*100))
                .add("av", Double.toString(m.annualVolatility(stock)*100))
                .add("cf", createArrayBuilder).build().toString();
                
    }
    
    @RequestMapping(method = {RequestMethod.GET}, value="/getProportion")
    public String stockVolatilityReturn(@RequestParam(value = "proportion", 
            defaultValue="50.0") Double userCash, 
            @RequestParam(value = "companyOne", 
            defaultValue="AAPL") String companyOne, 
            @RequestParam(value = "companyTwo", 
            defaultValue="F") String companyTwo)
    {
        double applRatio = (userCash) / 100.0;
        double income = (m.annualAvgReturn(companyOne) * applRatio) + 
                (m.annualAvgReturn(companyTwo) * (1-applRatio))*100;
        return Json.createObjectBuilder()
            .add("aVol", Double.toString(m.ratio(companyOne, companyTwo, applRatio)))
            .add("aRet", Double.toString(income))
            .build()
            .toString();
    }
    
    @RequestMapping(method = {RequestMethod.GET}, value="/getAllValues")
    public String overallVolatilityReturn(@RequestParam(value = "companyOne", 
            defaultValue="AAPL") String companyOne, 
            @RequestParam(value = "companyTwo", 
            defaultValue="F") String companyTwo) 
    {  
        return Json.createObjectBuilder()
            .add("aVol", Arrays.toString(m.allRatios(companyTwo, companyOne)))
            .add("aRet", Arrays.toString(m.allReturns(companyTwo, companyOne)))
            .build()
            .toString();        
    }
    
    @RequestMapping(method = {RequestMethod.GET}, value="/getSemiRandomizedPortfolio")
    public String highReturnLowVolatility(@RequestParam(value = "cutoff",
            defaultValue = "0.12") Double cutoff) {
        double[] learnModel = m.learnModel(cutoff);
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        String[] names = m.getNames();
        JsonObjectBuilder jsonObject = Json.createObjectBuilder();
        
        for(int i = 0; i < learnModel.length; i++) {
            arrayBuilder.add(Json.createObjectBuilder().add(names[i], 
                            learnModel[i]*100));    
        }
        NumberFormat formatter = new DecimalFormat("#0.00");  
        double avgProfit = m.weightingReturn(learnModel);
        return jsonObject.add("aar", avgProfit*100)
                .add("av", m.weightingVolatility(learnModel)*100)
                .add("ep", formatter.format(avgProfit * 10000000))
                .add("pu", arrayBuilder)
                .build().toString();
    }
    
   
}
