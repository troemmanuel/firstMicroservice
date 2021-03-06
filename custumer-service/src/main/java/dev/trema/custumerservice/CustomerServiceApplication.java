package dev.trema.custumerservice;

import dev.trema.custumerservice.models.Customer;
import dev.trema.custumerservice.repositories.CustomerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.repository.config.RepositoryConfiguration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;

@SpringBootApplication
public class CustomerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerServiceApplication.class, args); }
        @Bean
        CommandLineRunner start(CustomerRepository customerRepository, RepositoryRestConfiguration repositoryRestConfiguration){
         return args -> {
             repositoryRestConfiguration.exposeIdsFor(Customer.class);
             customerRepository.save(new Customer(null, "Enset", "contact@enset-media.ma"));
             customerRepository.save(new Customer(null, "FSTM", "contact@fstma.ma"));
             customerRepository.save(new Customer(null, "ENSAM", "contact@ensam.ma"));
             customerRepository.findAll().forEach(System.out::println);
         };
        };


}
