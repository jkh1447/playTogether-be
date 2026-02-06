package com.jkh1447.MyProject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MyProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyProjectApplication.class, args);
	}

}
