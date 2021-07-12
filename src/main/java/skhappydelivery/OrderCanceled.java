
package skhappydelivery;

public class OrderCanceled extends AbstractEvent {

    private Long OrderID;
    private String OrderStatus;

    public Long getId() {
        return OrderID;
    }

    public void setId(Long OrderID) {
        this.OrderID = OrderID;
    }
    public String getOrderStatus() {
        return OrderStatus;
    }

    public void setOrderStatus(String OrderStatus) {
        this.OrderStatus = OrderStatus;
    }
}

