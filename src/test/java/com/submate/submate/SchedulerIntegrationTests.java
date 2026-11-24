package com.submate.submate;

import com.submate.submate.entity.Subscription;
import com.submate.submate.entity.User;
import com.submate.submate.repository.CategoryRepository;
import com.submate.submate.repository.SubscriptionRepository;
import com.submate.submate.repository.UserRepository;
import com.submate.submate.scheduler.DailyScheduler;
import com.submate.submate.service.ExchangeRateService;
import com.submate.submate.service.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "RUN_SCHEDULER_MYSQL_TESTS", matches = "true")
class SchedulerIntegrationTests {

    @Autowired private MockMvc mvc;
    @Autowired private DailyScheduler scheduler;
    @Autowired private JwtService jwt;
    @Autowired private UserRepository users;
    @Autowired private CategoryRepository categories;
    @Autowired private SubscriptionRepository subscriptions;
    @MockitoBean private ExchangeRateService exchangeRates;

    @Test
    void recordsTodaysPaymentImmediatelyAndCountsItOnceAfterDailyRun() throws Exception {
        LocalDate today = today();
        String authorization = authorization(createUser());

        mvc.perform(post("/api/subscriptions").header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"category":"OTT","serviceName":"Example","planType":"PAID","currency":"KRW",
                                 "amount":11990,"billingCycle":"YEARLY","paymentMonth":%d,"billingDay":%d}
                                """.formatted(today.getMonthValue(), today.getDayOfMonth())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.startDate").value(today.toString()));

        payments(authorization, today, today)
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].amountKrw").value(11990));
        scheduler.runDaily();
        payments(authorization, today, today).andExpect(jsonPath("$.length()").value(1));

        mvc.perform(get("/api/reports/monthly").header("Authorization", authorization)
                        .param("year", String.valueOf(today.getYear()))
                        .param("month", String.valueOf(today.getMonthValue())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmountKrw").value(11990));
    }

    @Test
    void startupRecoversPaymentsBeforeCancellationAndExpiresOnlyEndedTrials() throws Exception {
        LocalDate today = today();
        LocalDate previousPayment = today.minusYears(1);
        User user = createUser();
        var category = categories.findById(1).orElseThrow();
        Subscription paid = new Subscription(user, category, null, "Canceled plan", "PAID", "KRW",
                new BigDecimal("9000"), "YEARLY", today.getDayOfMonth(), previousPayment, null, null, null);
        paid.changeStatus("CANCEL_SCHEDULED", today);
        subscriptions.save(paid);

        Subscription endedTrial = new Subscription(user, category, null, "Ended trial", "TRIAL",
                null, null, null, null, null, today.minusDays(1), null, null);
        Subscription lastDayTrial = new Subscription(user, category, null, "Last day trial", "TRIAL",
                null, null, null, null, null, today, null, null);
        subscriptions.saveAll(java.util.List.of(endedTrial, lastDayTrial));

        scheduler.onStartup();
        scheduler.onStartup();

        Subscription canceled = subscriptions.findById(paid.getId()).orElseThrow();
        assertEquals("CANCELED", canceled.getStatus());
        assertEquals(today, canceled.getCancelDate());
        assertEquals("CANCELED", subscriptions.findById(endedTrial.getId()).orElseThrow().getStatus());
        assertEquals("ACTIVE", subscriptions.findById(lastDayTrial.getId()).orElseThrow().getStatus());
        payments(authorization(user), previousPayment, today)
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].paidDate").value(previousPayment.toString()));
    }

    private ResultActions payments(String authorization, LocalDate from, LocalDate to) throws Exception {
        return mvc.perform(get("/api/payments").header("Authorization", authorization)
                        .param("from", from.toString()).param("to", to.toString()))
                .andExpect(status().isOk());
    }

    private User createUser() {
        return users.save(new User(UUID.randomUUID() + "@example.test", "unused", "test"));
    }

    private String authorization(User user) {
        return "Bearer " + jwt.generateToken(user.getId());
    }

    private LocalDate today() {
        return LocalDate.now(ZoneId.of("Asia/Seoul"));
    }
}
