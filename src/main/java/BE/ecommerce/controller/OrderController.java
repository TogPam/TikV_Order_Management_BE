package BE.ecommerce.controller;

import BE.ecommerce.service.OrderService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    // Trả về trực tiếp Content-Type là JSON
    @GetMapping(value = "/{orderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getOrder(@PathVariable String orderId) {
        String orderJson = orderService.getOrder(orderId);
        
        if (orderJson != null) {
            // Trả về thẳng chuỗi JSON (Spring Boot sẽ tự động parse thành object JSON chuẩn trên Postman)
            return ResponseEntity.ok(orderJson);
        }
        return ResponseEntity.status(404).body("{\"error\": \"Order not found\"}");
    }
}