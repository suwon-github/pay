package skhappydelivery;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import skhappydelivery.config.kafka.KafkaProcessor;

@Service
public class PolicyHandler{
    @Autowired 
    private PayRepository payRepository;
	
    @Autowired
    private PayService payService;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrderCanceled_PayCancel(@Payload OrderCanceled orderCanceled){

        if(!orderCanceled.validate()) return;
        
        System.out.println("\n\n##### listener PayCancel : " + orderCanceled.toString() + "\n\n");

        try {
			Optional<Pay> tempObj =  payRepository.findById(orderCanceled.getOrderId());

			Pay payObj = new Pay();

			if(tempObj.isPresent()){
				payObj = tempObj.get();		
			}else{
				System.out.println("NO PAY data" );
			}

			payObj.setPayStatus("ORDERCANCELLED");

			payRepository.save(payObj);
	
			System.out.println(" PAYLIST data all :  " + payRepository.findAll().toString());
	
			System.out.println("ORDERCANCELLED SUCCESS");
			
		} catch (Exception e) {

            System.out.println("\n\n##### listener PayCancel ERROR \n\n");
		
		}




    }//wheneverOrderCanceled_PayCancel

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverStoreOrderRejected_PayCancel(@Payload StoreOrderRejected storeOrderRejected){

        if(!storeOrderRejected.validate()) return;

        System.out.println("\n\n##### listener PayCancel : " + storeOrderRejected.toJson() + "\n\n");

        // Sample Logic //
        Pay pay = new Pay();
        payRepository.save(pay);
            
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
