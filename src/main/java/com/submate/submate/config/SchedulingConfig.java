package com.submate.submate.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

// @Scheduled 작업(매일 스케줄러) 사용 설정
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
