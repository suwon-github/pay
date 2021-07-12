package skhappydelivery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import skhappydelivery.config.kafka.KafkaProcessor;

@Service
public class PolicyHandler{
    @Autowired PayRepository payRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrderCanceled_PayCancel(@Payload OrderCanceled orderCanceled){

        if(!orderCanceled.validate()) return;

        System.out.println("\n\n##### listener PayCancel : " + orderCanceled.toJson() + "\n\n");

        // Sample Logic //
        Pay pay = new Pay();
        payRepository.save(pay);
            
    }
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
