package com.tirmizee;

import com.tirmizee.service.BusinessService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		var context = SpringApplication.run(DemoApplication.class, args);
		var service = context.getBean(BusinessService.class);

		service
				.getProduct()
				.doOnNext(System.out::println)
				.subscribe();
	}

}
