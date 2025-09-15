package com.poxju.proksi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ProksiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProksiApplication.class, args);
	}

}
 