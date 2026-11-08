package com.pf.scheduler;

import com.pf.scheduler.recurring.AsyncRecurringTransactionClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class SchedulerApplicationTests {

    @MockitoBean
    private AsyncRecurringTransactionClient asyncRecurringTransactionClient;

    @Test
    void contextLoads() {
    }

}
