
package skhappydelivery;

public class Payed  extends AbstractEvent  {

    private Long orderId;
    private int TotalPrice;
    private int PayMethod;
    private String CardNumber;
    private int DeliveryFee;
    private Long customerId;
    private Long storeId;

	public Long getStoreId() {
		return this.storeId;
	}

	public void setStoreId(Long storeId) {
		this.storeId = storeId;
	}

	public Long getCustomerId() {
		return this.customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}

    public Long getOrderId() {
        return this.orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public int getTotalPrice() {
        return TotalPrice;
    }

    public void setTotalPrice(int totalprice) {
        this.TotalPrice = totalprice;
    }
    public int getPayMethod() {
        return PayMethod;
    }

    public void setPayMethod(int PayMethod) {
        this.PayMethod = PayMethod;
    }

	public String getCardNumber() {
		return this.CardNumber;
	}

	public void setCardNumber(String CardNumber) {
		this.CardNumber = CardNumber;
	}

    public int getDeliveryFee() {
        return DeliveryFee;
    }

    public void setDeliveryFee(int DeliveryFee) {
        this.DeliveryFee = DeliveryFee;
    }


    @Override
	public String toString() {
		return "PayedObj [orderId=" + orderId + ", TotalPrice=" + TotalPrice + ", PayMethod=" + PayMethod
				+ ", CardNumber=" + CardNumber + ", DeliveryFee=" + DeliveryFee + "]";
	}
}//classPayed

