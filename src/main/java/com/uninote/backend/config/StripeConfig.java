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
    public StripeConfig() {
        logger.info("StripeConfig constructor called - Configuration is being loaded!");
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
        logger.error("Stripe API key set successfully. hey");
        if (stripeSecretKey != null && !stripeSecretKey.isBlank()) {
            logger.error("Stripe API key set successfully.");
        } else {
            logger.error("Stripe API key is missing or empty. Check environment variable STRIPE_SECRET_KEY.");
        }
    }
}
