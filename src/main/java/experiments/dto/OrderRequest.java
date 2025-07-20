package experiments.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class OrderRequest {
    private String customerName;
    private BigDecimal totalAmount;
}
