package com.taxcompliance.config;

import com.taxcompliance.constant.ApplicationConstants;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI taxGapComplianceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Tax Gap Detection & Compliance Validation API")
                        .description("Backend service foundation for tax auditors to validate compliance workflows, tax gap detection, audit trails, and reporting.")
                        .version(ApplicationConstants.API_VERSION));
    }
}
