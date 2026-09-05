package BE.ecommerce.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tikv.common.TiConfiguration;
import org.tikv.common.TiSession;

import jakarta.annotation.PreDestroy;

@Configuration
public class TiKVConfig {

    @Value("${tikv.pd.endpoints}")
    private String pdEndpoints;

    private TiSession tiSession;

    @Bean
    public TiSession tiSession() {
        // Khởi tạo cấu hình và tạo Session kết nối đến cụm TiKV thông qua PD
        TiConfiguration conf = TiConfiguration.createDefault(pdEndpoints);
        this.tiSession = TiSession.create(conf);
        return this.tiSession;
    }

    @PreDestroy
    public void close() {
        // Đóng session an toàn khi tắt Spring Boot để tránh rò rỉ bộ nhớ
        if (tiSession != null) {
            try {
                tiSession.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}