/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package debitsuisse.projectfour;

import javax.json.Json;
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
    
    exampleClass eC = new exampleClass();
    
    model m = new model();
    
    @RequestMapping("/getData")
    public String managerData() {
       return "test";
    }
    
    @CrossOrigin(origins = "*")
    @RequestMapping(method = {RequestMethod.GET}, value="/getProportion")
    public String stockProportion(@RequestParam(value = "proportion", 
            defaultValue="50.0") Double userCash)
    {
        double applRatio = (100.0 - userCash) / 100.0;
        double income = (m.annualAvgReturn("AAPL") * applRatio) + (m.annualAvgReturn("F") * (1-applRatio));
        return Json.createObjectBuilder()
            .add("aVol", Double.toString(m.ratio("AAPL", "F", applRatio)))
            .add("aRet", Double.toString(income))
            .build()
            .toString();
    }
    
    
    
}
