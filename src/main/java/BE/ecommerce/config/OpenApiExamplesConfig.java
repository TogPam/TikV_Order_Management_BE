package BE.ecommerce.config;

import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.Operation;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class OpenApiExamplesConfig {

    private static final String JSON = "application/json";

    @Bean
    public OpenApiCustomizer orderApiExamples() {
        return openApi -> {
            Operation login = operation(openApi, "/api/auth/login", "post");
            if (login != null) {
            login.setRequestBody(jsonRequestBody("LoginRequest", Map.of(
                "username", "customer1",
                "password", "123456")));
            login.setResponses(singleResponse("200", "Login successful", Map.of(
                "accessToken", "eyJhbGciOiJIUzI1NiJ9.example.signature",
                "tokenType", "Bearer")));
            }

            Operation register = operation(openApi, "/api/auth/register", "post");
            if (register != null) {
            register.setRequestBody(jsonRequestBody("RegisterRequest", Map.of(
                "username", "newcustomer",
                "password", "123456")));
            register.setResponses(singleResponse("201", "Registration successful", Map.of(
                "message", "User registered successfully!")));
            }

            Operation createOrder = operation(openApi, "/api/orders", "post");
            if (createOrder != null) {
                createOrder.setRequestBody(jsonRequestBody("CreateOrderRequest", createOrderRequest()));
                createOrder.setResponses(singleResponse("201", "Order created", orderCreatedResponse()));
            }

            Operation getOrder = operation(openApi, "/api/orders/{orderId}", "get");
            if (getOrder != null) {
                getOrder.setResponses(singleResponse("200", "Order details", orderResponse()));
                addPathExample(getOrder, "orderId", "ORD-a1b2c3d4");
            }

            Operation listOrders = operation(openApi, "/api/orders", "get");
            if (listOrders != null) {
                listOrders.setResponses(singleResponse("200", "Order list", orderListResponse()));
                addParameterExample(listOrders, "status", "PENDING");
                addParameterExample(listOrders, "limit", 50);
                addParameterExample(listOrders, "cursor", "order:ORD-001");
            }

            Operation updateStatus = operation(openApi, "/api/orders/{orderId}/status", "patch");
            if (updateStatus != null) {
                updateStatus.setRequestBody(jsonRequestBody("UpdateStatusRequest", Map.of(
                        "newStatus", "PAID",
                        "reason", "Payment confirmed via bank transfer")));
                updateStatus.setResponses(singleResponse("200", "Status updated", Map.of(
                        "success", true,
                        "data", Map.of(
                                "orderId", "ORD-a1b2c3d4",
                                "status", "PAID",
                                "previousStatus", "PENDING",
                                "updatedAt", "2026-09-01T10:35:00Z"),
                        "message", "Order status updated")));
                addPathExample(updateStatus, "orderId", "ORD-a1b2c3d4");
            }

            Operation deleteOrder = operation(openApi, "/api/orders/{orderId}", "delete");
            if (deleteOrder != null) {
                deleteOrder.setResponses(singleResponse("200", "Order cancelled", Map.of(
                        "success", true,
                        "message", "Order cancelled")));
                addPathExample(deleteOrder, "orderId", "ORD-a1b2c3d4");
            }

            Operation stats = operation(openApi, "/api/orders/stats", "get");
            if (stats != null) {
                stats.setResponses(singleResponse("200", "Order statistics", Map.of(
                        "success", true,
                        "data", Map.of(
                                "PENDING", 12,
                                "PAID", 8,
                                "SHIPPING", 5,
                                "COMPLETED", 30,
                                "CANCELLED", 3))));
            }

            Operation health = operation(openApi, "/api/health", "get");
            if (health != null) {
                health.setResponses(singleResponse("200", "TiKV is available", Map.of(
                        "status", "UP", "tikv", "UP")));
            }
        };
    }

    private Operation operation(io.swagger.v3.oas.models.OpenAPI openApi, String path, String method) {
        if (openApi.getPaths() == null || openApi.getPaths().get(path) == null) {
            return null;
        }
        return switch (method) {
            case "get" -> openApi.getPaths().get(path).getGet();
            case "post" -> openApi.getPaths().get(path).getPost();
            case "patch" -> openApi.getPaths().get(path).getPatch();
            case "delete" -> openApi.getPaths().get(path).getDelete();
            default -> null;
        };
    }

    private RequestBody jsonRequestBody(String schemaName, Object exampleValue) {
        Schema<?> schema = new ObjectSchema().name(schemaName);
        return new RequestBody()
                .required(true)
                .content(new Content().addMediaType(JSON, new MediaType()
                        .schema(schema)
                        .example(exampleValue)));
    }

    private ApiResponses singleResponse(String code, String description, Object exampleValue) {
        return new ApiResponses().addApiResponse(code, new ApiResponse()
                .description(description)
                .content(new Content().addMediaType(JSON, new MediaType()
                        .schema(new ObjectSchema())
                        .example(exampleValue))));
    }

    private void addPathExample(Operation operation, String name, Object value) {
        addParameterExample(operation, name, value);
    }

    private void addParameterExample(Operation operation, String name, Object value) {
        if (operation.getParameters() != null) {
            operation.getParameters().stream()
                    .filter(parameter -> name.equals(parameter.getName()))
                    .forEach(parameter -> parameter.setExample(value));
        }
    }

    private Map<String, Object> createOrderRequest() {
        return Map.of(
                "orderId", "ORD-a1b2c3d4",
                "customerName", "Nguyen Van A",
                "amount", 25000000,
                "items", List.of(Map.of(
                        "productId", "PROD-001",
                        "name", "Laptop Dell XPS",
                        "quantity", 1,
                        "price", 25000000)),
                "shippingAddress", "123 Nguyen Hue, Q1, HCM");
    }

    private Map<String, Object> orderResponse() {
        return new LinkedHashMap<>(Map.of(
                "success", true,
                "data", orderData(),
                "message", "Order retrieved successfully"));
    }

    private Map<String, Object> orderCreatedResponse() {
        return new LinkedHashMap<>(Map.of(
                "success", true,
                "data", orderData(),
                "message", "Order created successfully"));
    }

    private Map<String, Object> orderData() {
        return new LinkedHashMap<>(Map.of(
                "orderId", "ORD-a1b2c3d4",
                "customerName", "Nguyen Van A",
                "amount", 25000000,
                "status", "PENDING",
                "createdAt", "2026-09-01T10:30:00Z"));
    }

    private Map<String, Object> orderListResponse() {
        return Map.of(
                "success", true,
                "data", List.of(
                        Map.of("orderId", "ORD-001", "status", "PENDING", "amount", 500000),
                        Map.of("orderId", "ORD-002", "status", "PAID", "amount", 1200000)),
                "pagination", Map.of(
                        "cursor", "order:ORD-002",
                        "hasMore", true,
                        "limit", 50));
    }
}