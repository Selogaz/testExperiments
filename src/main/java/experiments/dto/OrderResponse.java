package experiments.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderResponse {
    private Long id;
    private String customerName;
    private BigDecimal totalAmount;
    private boolean isProcessed;

    public OrderResponse(Long id, String customerName, BigDecimal totalAmount, boolean isProcessed) {
        this.id = id;
        this.customerName = customerName;
        this.totalAmount = totalAmount;
        this.isProcessed = isProcessed;
    }
}
