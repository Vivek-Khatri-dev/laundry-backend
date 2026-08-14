package com.dawsons.laundry.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

// ============================================================
// Enables @Async so slow, non-critical work (currently: sending
// order/payment/ready-for-collection emails) runs on a background
// thread instead of blocking the HTTP request — and, previously,
// blocking the @Transactional bill-save/status-update methods for
// however long the Gmail SMTP handshake took (up to several
// seconds, worse on a slow connection).
//
// A small dedicated pool is used (rather than the default
// SimpleAsyncTaskExecutor, which spins up an unbounded number of
// threads) so a run of slow/stuck email sends can't exhaust
// server resources.
// ============================================================
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(AsyncConfig.class);

    @Override
    @Bean(name = "mailTaskExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("mail-async-");
        executor.initialize();
        return executor;
    }

    // Async methods return void, so exceptions can't propagate back to the
    // caller — EmailService already catches and logs its own failures, but
    // this is a safety net so nothing fails silently if that ever changes.
    @Override
    public org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) ->
                logger.error("Uncaught exception in async method '{}': {}", method.getName(), ex.getMessage(), ex);
    }
}