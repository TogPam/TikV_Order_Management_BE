package BE.ecommerce.controller;

import BE.ecommerce.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody Map<String, Object> request) {
        try {
            String orderId = request.get("orderId").toString();
            String customerName = request.get("customerName").toString();
            double amount = Double.parseDouble(request.get("amount").toString());

            orderService.createOrder(orderId, customerName, amount);
            return ResponseEntity.status(201).body("Order saved to TiKV cluster successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<String> getOrder(@PathVariable String orderId) {
        Optional<String> orderJson = orderService.getOrder(orderId);
        return orderJson.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body("Order not found"));
    }
}