package skhappydelivery;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

 @RestController
 public class PayController {

    
   	/*----  POST-----order 주문 들어옴 */
	@RequestMapping(value="/payed", method=RequestMethod.POST)
	public String Payed(@RequestBody Payed payedObj) throws Exception {
		
		System.out.println("□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□o Payed Controller start "+System.currentTimeMillis()+"□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□");
		System.out. println(" INput orderPlacedObj :  " + payedObj.toString());

    return "payController";

	} //OrderPlaced




 }//classPayController

