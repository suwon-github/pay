
package skhappydelivery;

public class StoreOrderRejected extends AbstractEvent {

    private Long OrderID;
    private int RejectCode;

	public int getRejectCode() {
		return this.RejectCode;
	}

	public void setRejectCode(int RejectCode) {
		this.RejectCode = RejectCode;
	}

    private String RejectDetail;
    private String OrderStatus;

    public Long getOrderId() {
        return OrderID;
    }

    public void setOrderId(Long OrderID) {
        this.OrderID = OrderID;
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

