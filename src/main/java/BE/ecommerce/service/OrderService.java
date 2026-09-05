package BE.ecommerce.service;

import org.springframework.stereotype.Service;
import org.tikv.common.TiSession;
import org.tikv.raw.RawKVClient;
import org.tikv.shade.com.google.protobuf.ByteString;

import java.util.Optional;

@Service
public class OrderService {

    private final TiSession tiSession;

    public OrderService(TiSession tiSession) {
        this.tiSession = tiSession;
    }

    // Tạo đơn hàng mới
    public void createOrder(String orderId, String customerName, double amount) throws Exception {
        // Tạo RawKVClient từ TiSession
        try (RawKVClient client = tiSession.createRawClient()) {
            String keyStr = "order:" + orderId;
            
                long createdAt = System.currentTimeMillis();
                String json = "{"
                    + "\"orderId\":\"" + escapeJson(orderId) + "\","
                    + "\"customerName\":\"" + escapeJson(customerName) + "\","
                    + "\"amount\":" + amount + ","
                    + "\"status\":\"PENDING\","
                    + "\"createdAt\":" + createdAt
                    + "}";

            // Chuyển đổi Key và Value sang ByteString của TiKV
            ByteString key = ByteString.copyFromUtf8(keyStr);
            ByteString value = ByteString.copyFromUtf8(json);

            // Thực hiện ghi dữ liệu xuống node TiKV
            client.put(key, value);
        }
    }

    // Truy vấn đơn hàng
    public Optional<String> getOrder(String orderId) {
        try (RawKVClient client = tiSession.createRawClient()) {
            ByteString key = ByteString.copyFromUtf8("order:" + orderId);
            
            // Đọc dữ liệu từ TiKV
            Optional<ByteString> value = client.get(key);

            if (value != null && !value.isEmpty()) {
                return Optional.of(value.toString());
            }
            return Optional.empty();
        }
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}