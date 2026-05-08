package com.cinq.vestaboard.configuration.properties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(
        {VestaBoardProperties.class,
        VestaBoardVbmlProperties.class}
)
class PropertiesConfig {
}
