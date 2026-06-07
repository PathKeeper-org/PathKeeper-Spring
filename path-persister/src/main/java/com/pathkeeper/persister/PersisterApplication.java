// path-persister/src/main/java/com/pathkeeper/persister/PersisterApplication.java
package com.pathkeeper.persister;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing
public class PersisterApplication {
    public static void main(String[] args) {
        SpringApplication.run(PersisterApplication.class, args);
    }
}