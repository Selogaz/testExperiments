package experiments.model;

import java.math.BigDecimal;

public class orderModel {
        private Long id;
        private String customerName;
        private BigDecimal totalAmount;
        private boolean statusFlag;

        public orderModel(Long id, String customerName, BigDecimal totalAmount) {
            this.id = id;
            this.customerName = customerName;
            this.totalAmount = totalAmount;
            this.statusFlag = false;
        }
}
