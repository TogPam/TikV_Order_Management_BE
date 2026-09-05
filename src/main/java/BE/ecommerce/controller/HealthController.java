package BE.ecommerce.controller;

import BE.ecommerce.service.OrderOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    private final OrderOperations orderOperations;

    public HealthController(OrderOperations orderOperations) {
        this.orderOperations = orderOperations;
    }

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> health() {
        boolean healthy = orderOperations.isHealthy();
        return ResponseEntity.status(healthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("status", healthy ? "UP" : "DOWN", "tikv", healthy ? "UP" : "DOWN"));
    }
}