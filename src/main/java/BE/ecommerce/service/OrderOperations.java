package BE.ecommerce.service;

import java.util.List;
import java.util.Map;

public interface OrderOperations {

    List<Map<String, Object>> findOrders(String status, int limit, String cursor);

    Map<String, Object> updateStatus(String orderId, String newStatus, String reason);

    void cancelOrder(String orderId);

    Map<String, Long> countByStatus();

    boolean isHealthy();
}