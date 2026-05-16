package br.appLogin.appLogin.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud_name}")
    private String cloudName;

    @Value("${cloudinary.api_key}")
    private String apiKey;

    @Value("${cloudinary.api_secret}")
    private String apiSecret;

    @Bean
    Cloudinary cloudinary() {

        Map<String, String> values = new HashMap<>();

        values.put("cloud_name", cloudName);
        values.put("api_key", apiKey);
        values.put("api_secret", apiSecret);

        return new Cloudinary(values);
    }
}