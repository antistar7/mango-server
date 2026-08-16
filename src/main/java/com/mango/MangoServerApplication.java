package com.mango;

import com.mango.content.Content;
import com.mango.content.ContentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MangoServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MangoServerApplication.class, args);
	}

//	@Bean
//	CommandLineRunner init(ContentRepository repository) {
//		return args -> {
//			if (repository.count() == 0) {
//				repository.save(
//						new Content(
//								"입 짧다",
//								"食が細いね",
//								"많이 못 먹는다는 뜻이에요. 관련 상황에서 가볍게 말할 수 있어요."
//						)
//				);
//
//				repository.save(
//						new Content(
//								"맛있다",
//								"美味しいね",
//								"음식이나 맛이 좋을 때 사용하는 표현이에요."
//						)
//				);
//			}
//		};
//	}
}