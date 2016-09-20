package com.example;

import com.google.common.collect.Lists;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@EnableBinding(Source.class)
@EnableDiscoveryClient
@IntegrationComponentScan
@EnableCircuitBreaker
@EnableZuulProxy
@SpringBootApplication
public class GatewayApplication {

	@Bean
	@LoadBalanced
	RestTemplate restTemplate(){
		return new RestTemplate();
	}

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}
}

@Component
class CLR implements CommandLineRunner{

	@Resource
	ApplicationContext applicationContext;

	@Override
	public void run(String... args) throws Exception {
//		Stream.of(applicationContext.getBeanDefinitionNames()).forEach(System.out::println);
//
//		UserChannels channels = applicationContext.getBean(UserChannels.class);
//		System.out.println(channels);
	}
}

interface UserChannels {

	@Output
	MessageChannel output();
}

@RestController
@RequestMapping("users")
class UserServiceRestController {

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	Source channels;

	public Collection<String> namesFallback(){
		return Lists.newArrayList();
	}

	@RequestMapping(method = RequestMethod.POST)
	public void write(@RequestBody User user){
		channels.output().send(MessageBuilder.withPayload(user.getName()).build());
	}



	@HystrixCommand(fallbackMethod = "namesFallback")
	@RequestMapping("names")
	public Collection<String> names(){

		ParameterizedTypeReference<Resources<User>> ptr = new ParameterizedTypeReference<Resources<User>>() {};

		ResponseEntity<Resources<User>> entity = restTemplate.exchange("http://user-service/users", HttpMethod.GET, null, ptr);
		return entity.getBody().getContent().stream().map(User::getName).collect(Collectors.toList());
	}

}

class User{
	private String name;

	public String getName() {
		return name;
	}
}
