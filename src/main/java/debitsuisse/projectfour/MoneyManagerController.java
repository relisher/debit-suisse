/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package debitsuisse.projectfour;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author arelin
 */
@RestController
public class MoneyManagerController {
    
    exampleClass eC = new exampleClass();
    
    @RequestMapping("/getData")
    public String managerData() {
       return eC.initialize();
    }
    
}
