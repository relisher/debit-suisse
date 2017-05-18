/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package debitsuisse.projectfour;

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
       return m.test();
    }
    
    @CrossOrigin(origins = "*")
    @RequestMapping(method = {RequestMethod.GET}, value="/getProportion")
    public String stockProportion(@RequestParam(value = "proportion", 
            defaultValue="50.0") Double userCash)
    {
        return eC.initialize() + userCash;
    }
    
    
    
}
