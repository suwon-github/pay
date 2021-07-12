
package skhappydelivery;

public class StoreOrderRejected extends AbstractEvent {

    private Long OrderID;
    private Integer RejectCode;
    private String RejectDetail;
    private String OrderStatus;

    public Long getId() {
        return OrderID;
    }

    public void setId(Long OrderID) {
        this.OrderID = OrderID;
    }
    public Integer getRejectCode() {
        return RejectCode;
    }

    public void setRejectCode(Integer RejectCode) {
        this.RejectCode = RejectCode;
    }
    public String getRejectDetail() {
        return RejectDetail;
    }

    public void setRejectDetail(String RejectDetail) {
        this.RejectDetail = RejectDetail;
    }
    public String getOrderStatus() {
        return OrderStatus;
    }

    public void setOrderStatus(String OrderStatus) {
        this.OrderStatus = OrderStatus;
    }
}

