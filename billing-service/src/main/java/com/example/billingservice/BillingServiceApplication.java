package com.example.billingservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import net.minidev.json.annotate.JsonIgnore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.config.Projection;
import org.springframework.hateoas.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.*;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
class Bill{
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private Date billingDate;
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private Long customerID;
  @Transient
  private Customer customer;
  @OneToMany(mappedBy = "bill")
  private Collection<ProductItem> productItems;
}

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
class ProductItem{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long productId;
    @Transient
    private Product product;
    private double price;
    private double quantity;
    @ManyToOne
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Bill bill;
}
@RepositoryRestResource
interface BillRepository extends JpaRepository<Bill, Long> {}
@Projection(name ="fullBill", types = Bill.class)
interface BillProjection{
    public Long getId();
    public Long getBillingDate();
    public Long customerId();
    public Collection<ProductItem> getProductItems();
}
@RepositoryRestResource
interface ProductItemRepository extends JpaRepository<ProductItem, Long>{}

@Getter @Setter
class Customer {
    private Long id;
    private String name;
    private String email;
}

@FeignClient(name = "CUSTOMER-SERVICE")
interface CustomerService{
   @GetMapping("/customers/{id}")
    public Customer findCustomerById(@PathVariable(name = "id") Long id);
}
@Getter @Setter
class Product {
    private Long id;
    private String name;
    private Double price;
}

@FeignClient(name = "INVENTORY-SERVICE")
interface InventoryService{
    @GetMapping("/products/{id}")
    public Product findCustomerById(@PathVariable(name = "id") Long id);
    @GetMapping("/products")
    public PagedModel<Product> findAllProducts();
}
@SpringBootApplication
@EnableFeignClients
public class BillingServiceApplication {


    public static void main(String[] args) {
        SpringApplication.run(BillingServiceApplication.class, args);
    }
    @Bean
    CommandLineRunner start(BillRepository billRepository,
                            ProductItemRepository productItemRepository,
                            CustomerService customerService,
                            InventoryService inventoryService){
        return args -> {
            Customer customer1 = customerService.findCustomerById(1L);
            System.out.println("**************************************************");
            System.out.println("Email" + customer1.getId());
            System.out.println("Name" + customer1.getName());
            System.out.println("Email" + customer1.getEmail());
            System.out.println("**************************************************");
            Bill bill  = billRepository.save(new Bill(null, new Date(), customer1.getId(), null, null));
            PagedModel<Product> products = inventoryService.findAllProducts();
            products.getContent().forEach(p-> {
                productItemRepository.save(new ProductItem(null, p.getId(), null,p.getPrice(), 30, bill));
            });
        };
    }

}
@RestController @RequiredArgsConstructor
class BillRestController{

    final BillRepository billRepository;
    final ProductItemRepository productItemRepository;
    final CustomerService customerService;
    final InventoryService inventoryService;


    @GetMapping("/fullBill/{id}")
    public Bill getBill(@PathVariable(name = "id") Long id){
        if (billRepository.findById(id).isPresent()){
            Bill bill = billRepository.findById(id).get();
            bill.setCustomer(customerService.findCustomerById(bill.getCustomerID()));
            bill.getProductItems().forEach(productItem -> {
                productItem.setProduct(inventoryService.findCustomerById(productItem.getProductId()));
            });
            return bill;

        }
        else {
            return null;
        }

    }




}
