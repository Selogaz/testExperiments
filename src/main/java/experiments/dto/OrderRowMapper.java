package experiments.dto;

import experiments.model.OrderModel;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderRowMapper implements RowMapper<OrderModel> {
    @Override
    public OrderModel mapRow(ResultSet rs, int rowNum) throws SQLException {
        OrderModel order = new OrderModel();
        order.setId(rs.getLong("id"));
        order.setCustomerName(rs.getString("customer_name"));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setProcessed(rs.getBoolean("is_processed"));
        return order;
    }
}
