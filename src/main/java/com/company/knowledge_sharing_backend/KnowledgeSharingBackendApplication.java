package com.company.knowledge_sharing_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
@EnableJpaAuditing
public class KnowledgeSharingBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(KnowledgeSharingBackendApplication.class, args);
		System.out.println("Knowledge Sharing Backend Application Started Successfully!");
	}

}
