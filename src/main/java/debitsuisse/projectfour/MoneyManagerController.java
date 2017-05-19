/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package debitsuisse.projectfour;

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
@RestController
public class MoneyManagerController {
    
    model m = new model();

    @CrossOrigin(origins = "*")
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
                .add("mar", Double.toString(m.monthlyAvgReturn(m.getCompanyValue(stock))))
                .add("aar", Double.toString(m.annualAvgReturn(stock)))
                .add("mv", Double.toString(m.monthlyVariance(m.getCompanyValue(stock))))
                .add("av", Double.toString(m.annualVariance(stock)))
                .add("cf", createArrayBuilder).build().toString();
                
    }
    
    @CrossOrigin(origins = "*")
    @RequestMapping(method = {RequestMethod.GET}, value="/getProportion")
    public String stockVolatilityReturn(@RequestParam(value = "proportion", 
            defaultValue="50.0") Double userCash)
    {
        double applRatio = (userCash) / 100.0;
        double income = (m.annualAvgReturn("AAPL") * applRatio) + (m.annualAvgReturn("F") * (1-applRatio));
        return Json.createObjectBuilder()
            .add("aVol", Double.toString(m.ratio("AAPL", "F", applRatio)))
            .add("aRet", Double.toString(income))
            .build()
            .toString();
    }
    
    @CrossOrigin(origins = "*")
    @RequestMapping(method = {RequestMethod.GET}, value="/getAllValues")
    public String overallVolatilityReturn() 
    {  
        return Json.createObjectBuilder()
            .add("aVol", Arrays.toString(m.allRatios("F", "AAPL")))
            .add("aRet", Arrays.toString(m.allReturns("F", "AAPL")))
            .build()
            .toString();        
    }
   
}
