package BE.ecommerce.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.tikv.common.TiSession;
import org.tikv.raw.RawKVClient;
import org.tikv.shade.com.google.protobuf.ByteString;

import java.util.HashMap;
import java.util.Map;

@Service
public class OrderService {

    private final TiSession tiSession;
    private final ObjectMapper objectMapper;

    public OrderService(TiSession tiSession) {
        this.tiSession = tiSession;
        this.objectMapper = new ObjectMapper();
    }

    public void createOrder(String orderId, String customerName, double amount) throws Exception {
        try (RawKVClient client = tiSession.createRawClient()) {
            String keyStr = "order:" + orderId;
            
            Map<String, Object> orderData = new HashMap<>();
            orderData.put("orderId", orderId);
            orderData.put("customerName", customerName);
            orderData.put("amount", amount);
            orderData.put("status", "PENDING");
            orderData.put("createdAt", System.currentTimeMillis());

            ByteString key = ByteString.copyFromUtf8(keyStr);
            ByteString value = ByteString.copyFromUtf8(objectMapper.writeValueAsString(orderData));

            client.put(key, value);
        }
    }

    // Trả về trực tiếp String JSON hoặc null, không dùng Optional bọc ngoài
    public String getOrder(String orderId) {
    try (RawKVClient client = tiSession.createRawClient()) {
        ByteString key = ByteString.copyFromUtf8("order:" + orderId);
        
        // client.get() trả về Optional<ByteString>, ta dùng .orElse(null) để bóc tách
        ByteString value = client.get(key).orElse(null);

        if (value != null && !value.isEmpty()) {
            return value.toStringUtf8();
        }
        return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}