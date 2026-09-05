package BE.ecommerce.controller;

import BE.ecommerce.service.OrderOperations;
import BE.ecommerce.service.TiKVOrderOperations.OrderOperationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class MissingOrderController {

    private final OrderOperations orderOperations;

    public MissingOrderController(OrderOperations orderOperations) {
        this.orderOperations = orderOperations;
    }

    @GetMapping
    public ResponseEntity<?> listOrders(@RequestParam(required = false) String status,
                                        @RequestParam(defaultValue = "50") int limit,
                                        @RequestParam(required = false) String cursor) {
        var data = orderOperations.findOrders(status, limit, cursor);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("pagination", Map.of("cursor", data.isEmpty() ? cursor : "order:" + data.get(data.size() - 1).get("orderId"),
                "hasMore", data.size() == Math.min(Math.max(limit, 1), 100), "limit", Math.min(Math.max(limit, 1), 100)));
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<?> updateStatus(@PathVariable String orderId, @RequestBody Map<String, String> request) {
        try {
            var data = orderOperations.updateStatus(orderId, request.get("newStatus"), request.get("reason"));
            return ResponseEntity.ok(Map.of("success", true, "data", data, "message", "Order status updated"));
        } catch (OrderOperationException exception) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", exception.getMessage()));
        }
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> deleteOrder(@PathVariable String orderId) {
        try {
            orderOperations.cancelOrder(orderId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Order cancelled"));
        } catch (OrderOperationException exception) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", exception.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> stats() {
        return ResponseEntity.ok(Map.of("success", true, "data", orderOperations.countByStatus()));
    }
}