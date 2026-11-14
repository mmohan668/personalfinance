package com.pf.mailer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration;


@SpringBootApplication(scanBasePackages = {"com.pf.common.config.vault", "com.pf.mailer"},
        exclude = {
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class,
                DataSourceTransactionManagerAutoConfiguration.class
        })
public class EmailerApplication {

    public static void main(String[] args) {

        SpringApplication.run(EmailerApplication.class, args);
    }

}
