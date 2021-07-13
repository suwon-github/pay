package skhappydelivery;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.Table;

import org.springframework.beans.BeanUtils;

@Entity
@Table(name="Pay_table")
public class Pay {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long orderId;
    private Long customerId;
    private int totalPrice;
    private int payMethod;
    private String cardNumber;
    private int deliveryFee;
    private String payStatus;
    private Long storeId;



    @PostPersist
    public void onPostPersist(){
        Payed payed = new Payed();
        BeanUtils.copyProperties(this, payed);
       // payed.publishAfterCommit();
       payed.setOrderId(this.orderId);
       payed.setStoreId(this.storeId);

        System.out.println(" onPostPersist PUBLISH:   " +payed.toString());

        payed.publishAfterCommit();
    }

    @PostUpdate
    public void onPostUpdate(){

        if(this.payStatus=="PAYED"){

            Payed payed = new Payed();
            BeanUtils.copyProperties(this, payed);
           // payed.publishAfterCommit();
    
           payed.setOrderId(this.orderId);
           payed.setTotalPrice(this.totalPrice+this.deliveryFee);
    
            System.out.println(" onPostUpdate PUBLISH:  " +payed.toString());
    
            payed.publishAfterCommit();

        }else if(this.payStatus=="PAY CANCELLED"){

            PayCancelled payCancelled = new PayCancelled();
            BeanUtils.copyProperties(this, payCancelled);
           // payed.publishAfterCommit();
    
           payCancelled.setOrderId(this.orderId);

            System.out.println(" onPostUpdate PUBLISH:  " +payCancelled.toString());
    
            payCancelled.publishAfterCommit();

        }


    }



 

     public Long getStoreId() {
		return this.storeId;
	}

	public void setStoreId(Long storeId) {
		this.storeId = storeId;
	}
 
    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    public Integer getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Integer totalPrice) {
        this.totalPrice = totalPrice;
    }
    public Integer getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(Integer payMethod) {
        this.payMethod = payMethod;
    }
    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
    public Integer getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(Integer deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public String getPayStatus() {
		return this.payStatus;
	}

	public void setPayStatus(String payStatus) {
		this.payStatus = payStatus;
	}


    @Override
	public String toString() {
		return "PayObj [customerId=" + customerId + ", orderId=" + orderId + ", totalPrice=" + totalPrice
				+ ", payMethod=" + payMethod + ", cardNumber=" + cardNumber + ", deliveryFee=" + deliveryFee + ", payStatus=" + payStatus + "]";
	}

}
