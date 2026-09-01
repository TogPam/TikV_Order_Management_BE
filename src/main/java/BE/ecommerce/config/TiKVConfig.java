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

    @Bean
    public TiSession tiSession() {
        TiConfiguration conf = TiConfiguration.createDefault(pdEndpoints);
        return TiSession.create(conf);
    }
}