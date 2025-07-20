package experiments.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class OrderModel {
    private Long id;
    private String customerName;
    private BigDecimal totalAmount;
    private boolean isProcessed;

    public OrderModel(Long id, String customerName, BigDecimal totalAmount) {
        this.id = id;
        this.customerName = customerName;
        this.totalAmount = totalAmount;
        this.isProcessed = false;
    }
}
