package BE.ecommerce.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.tikv.common.TiSession;
import org.tikv.kvproto.Kvrpcpb;
import org.tikv.raw.RawKVClient;
import org.tikv.shade.com.google.protobuf.ByteString;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TiKVOrderOperations implements OrderOperations {

    private static final String ORDER_PREFIX = "order:";
    private static final List<String> TERMINAL_STATUSES = List.of("COMPLETED", "CANCELLED");

    private final TiSession tiSession;
    private final ObjectMapper objectMapper;

    public TiKVOrderOperations(TiSession tiSession) {
        this.tiSession = tiSession;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<Map<String, Object>> findOrders(String status, int limit, String cursor) {
        int boundedLimit = Math.min(Math.max(limit, 1), 100);
        List<Map<String, Object>> orders = new ArrayList<>();

        try (RawKVClient client = tiSession.createRawClient()) {
            for (Kvrpcpb.KvPair pair : client.scanPrefix(ByteString.copyFromUtf8(ORDER_PREFIX), boundedLimit + 1, false)) {
                String key = pair.getKey().toStringUtf8();
                if (cursor != null && !cursor.isBlank() && key.compareTo(cursor) <= 0) {
                    continue;
                }

                Map<String, Object> order = readOrder(pair.getValue());
                if (status == null || status.isBlank() || status.equals(order.get("status"))) {
                    orders.add(order);
                }
                if (orders.size() == boundedLimit) {
                    break;
                }
            }
        }
        return orders;
    }

    @Override
    public Map<String, Object> updateStatus(String orderId, String newStatus, String reason) {
        requireStatus(newStatus);
        String keyText = ORDER_PREFIX + orderId;
        ByteString key = ByteString.copyFromUtf8(keyText);

        try (RawKVClient client = tiSession.createRawClient()) {
            ByteString currentValue = client.get(key).orElseThrow(() -> new OrderOperationException("Order not found"));
            Map<String, Object> current = readOrder(currentValue);
            String previousStatus = String.valueOf(current.get("status"));
            validateTransition(previousStatus, newStatus);

            current.put("status", newStatus);
            current.put("updatedAt", Instant.now().toString());
            if (reason != null && !reason.isBlank()) {
                current.put("statusReason", reason);
            }

            ByteString updatedValue = ByteString.copyFromUtf8(writeOrder(current));
            client.compareAndSet(key, java.util.Optional.of(currentValue), updatedValue);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("orderId", orderId);
            result.put("status", newStatus);
            result.put("previousStatus", previousStatus);
            result.put("updatedAt", current.get("updatedAt"));
            return result;
        }
    }

    @Override
    public void cancelOrder(String orderId) {
        updateStatus(orderId, "CANCELLED", "Order cancelled");
    }

    @Override
    public Map<String, Long> countByStatus() {
        Map<String, Long> counts = new LinkedHashMap<>();
        try (RawKVClient client = tiSession.createRawClient()) {
            for (Kvrpcpb.KvPair pair : client.scanPrefix(ByteString.copyFromUtf8(ORDER_PREFIX))) {
                Map<String, Object> order = readOrder(pair.getValue());
                String status = String.valueOf(order.getOrDefault("status", "UNKNOWN"));
                counts.put(status, counts.getOrDefault(status, 0L) + 1L);
            }
        }
        return counts;
    }

    @Override
    public boolean isHealthy() {
        try (RawKVClient client = tiSession.createRawClient()) {
            client.scanPrefix(ByteString.copyFromUtf8(ORDER_PREFIX), 1, false);
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private Map<String, Object> readOrder(ByteString value) {
        try {
            return objectMapper.readValue(value.toStringUtf8(), new TypeReference<>() {
            });
        } catch (Exception exception) {
            throw new OrderOperationException("Invalid order data", exception);
        }
    }

    private String writeOrder(Map<String, Object> order) {
        try {
            return objectMapper.writeValueAsString(order);
        } catch (Exception exception) {
            throw new OrderOperationException("Unable to serialize order", exception);
        }
    }

    private void requireStatus(String status) {
        if (status == null || !List.of("PENDING", "PAID", "SHIPPING", "COMPLETED", "CANCELLED").contains(status)) {
            throw new OrderOperationException("Unknown order status: " + status);
        }
    }

    private void validateTransition(String current, String next) {
        boolean valid = switch (current) {
            case "PENDING" -> next.equals("PAID") || next.equals("CANCELLED");
            case "PAID" -> next.equals("SHIPPING") || next.equals("CANCELLED");
            case "SHIPPING" -> next.equals("COMPLETED") || next.equals("CANCELLED");
            default -> false;
        };
        if (!valid || TERMINAL_STATUSES.contains(current)) {
            throw new OrderOperationException("Cannot transition from " + current + " to " + next);
        }
    }

    public static class OrderOperationException extends RuntimeException {
        public OrderOperationException(String message) {
            super(message);
        }

        public OrderOperationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}