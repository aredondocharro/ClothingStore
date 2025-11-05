package com.aredondocharro.ClothingStore.notification.infrastructure.config;


import org.springframework.boot.context.properties.ConfigurationProperties;


import java.time.Duration;


@ConfigurationProperties(prefix = "app.mail.outbox")
public class OutboxMailProperties {
    /** How many messages to pick per tick */
    private int batchSize = 25;
    /** Max delivery attempts before marking as FAILED */
    private int maxAttempts = 6;
    /** Poll interval for the scheduler */
    private Duration pollInterval = Duration.ofSeconds(10);
    /** Base delay for backoff */
    private Duration baseDelay = Duration.ofSeconds(60);
    /** Exponential multiplier */
    private double multiplier = 3.0;


    public int getBatchSize() { return batchSize; }
    public void setBatchSize(int batchSize) { this.batchSize = batchSize; }


    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }


    public Duration getPollInterval() { return pollInterval; }
    public void setPollInterval(Duration pollInterval) { this.pollInterval = pollInterval; }


    public Duration getBaseDelay() { return baseDelay; }
    public void setBaseDelay(Duration baseDelay) { this.baseDelay = baseDelay; }


    public double getMultiplier() { return multiplier; }
    public void setMultiplier(double multiplier) { this.multiplier = multiplier; }
}