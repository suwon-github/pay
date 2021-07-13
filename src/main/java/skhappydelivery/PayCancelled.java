
package skhappydelivery;

public class PayCancelled extends AbstractEvent {

    private Long OrderID;
    private Integer TotalPrice;
    private Long PayMethod;
    private Integer CardNumber;
    private Integer DeliveryFee;

    public Long getOrderId() {
        return OrderID;
    }

    public void setOrderId(Long OrderID) {
        this.OrderID = OrderID;
    }
    public Integer getTotalPrice() {
        return TotalPrice;
    }

    public void setTotalPrice(Integer TotalPrice) {
        this.TotalPrice = TotalPrice;
    }
    public Long getPayMethod() {
        return PayMethod;
    }

    public void setPayMethod(Long PayMethod) {
        this.PayMethod = PayMethod;
    }
    public Integer getCardNumber() {
        return CardNumber;
    }

    public void setCardNumber(Integer CardNumber) {
        this.CardNumber = CardNumber;
    }
    public Integer getDeliveryFee() {
        return DeliveryFee;
    }

    public void setDeliveryFee(Integer DeliveryFee) {
        this.DeliveryFee = DeliveryFee;
    }
}

