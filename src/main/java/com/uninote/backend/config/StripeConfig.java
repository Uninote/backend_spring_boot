package com.uninote.backend.config;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.stripe.Stripe;

@Configuration
public class StripeConfig {

    private static final Logger logger = LoggerFactory.getLogger(StripeConfig.class);

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;

        if (stripeSecretKey != null && !stripeSecretKey.isBlank()) {
            logger.info("Stripe API key set successfully.");
        } else {
            logger.warn("Stripe API key is missing or empty. Check environment variable STRIPE_SECRET_KEY.");
        }
    }
}
