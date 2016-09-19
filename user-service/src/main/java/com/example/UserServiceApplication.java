package com.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Collection;
import java.util.stream.Stream;

@SpringBootApplication
@EnableDiscoveryClient
public class UserServiceApplication {

	@Bean
	CommandLineRunner commandLineRunner(UserRepository userRepository) {
		return args -> {
			Stream.of("Rocky" , "Merlin" , "Dongdong" , "Alex").forEach(s -> userRepository.save(new User(s)));
		};
	}


	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}
}

@RefreshScope
@RestController
class MessageRestController {

	@Value("${message}")
	private String message;

	@RequestMapping("/message")
	public String message(){
		return this.message;
	}

}


@RepositoryRestResource
interface UserRepository extends JpaRepository<User,Long>{

	@RestResource(path = "by-name")
	Collection<User> findByName(@Param("name") String name);

}


@Entity
class User{

	@Id @GeneratedValue
	private Long id;
	private String name;

	public User() {
	}

	public User(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "User{" +
				"id=" + id +
				", name='" + name + '\'' +
				'}';
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
