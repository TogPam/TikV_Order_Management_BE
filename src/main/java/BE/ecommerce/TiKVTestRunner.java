package BE.ecommerce;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.tikv.common.TiSession;
import org.tikv.raw.RawKVClient;
import org.tikv.shade.com.google.protobuf.ByteString;

@Component
public class TiKVTestRunner implements CommandLineRunner {

    private final TiSession tiSession;

    public TiKVTestRunner(TiSession tiSession) {
        this.tiSession = tiSession;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("====== KIỂM TRA KẾT NỐI TIKV ======");
        try (RawKVClient client = tiSession.createRawClient()) {
            ByteString key = ByteString.copyFromUtf8("test:connection");
            ByteString value = ByteString.copyFromUtf8("Hello TiKV from Spring Boot!");
            
            // Ghi dữ liệu
            client.put(key, value);
            System.out.println("Đã ghi thành công key: test:connection");

            // Đọc dữ liệu
            ByteString fetchedValue = client.get(key);
            System.out.println("Đọc dữ liệu từ TiKV: " + fetchedValue.toStringUtf8());
            System.out.println("===================================");
        } catch (Exception e) {
            System.err.println("Kết nối TiKV thất bại: " + e.getMessage());
        }
    }
}