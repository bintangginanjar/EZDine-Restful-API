package restful.api.ezdine.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import restful.api.ezdine.entity.CategoryEntity;
import restful.api.ezdine.entity.FoodEntity;
import restful.api.ezdine.entity.OrderEntity;
import restful.api.ezdine.entity.RoleEntity;
import restful.api.ezdine.entity.UserEntity;
import restful.api.ezdine.model.OrderItemResponse;
import restful.api.ezdine.model.RegisterOrderItemRequest;
import restful.api.ezdine.model.WebResponse;
import restful.api.ezdine.repository.CategoryRepository;
import restful.api.ezdine.repository.FoodRepository;
import restful.api.ezdine.repository.OrderItemRepository;
import restful.api.ezdine.repository.OrderRepository;
import restful.api.ezdine.repository.RoleRepository;
import restful.api.ezdine.repository.UserRepository;
import restful.api.ezdine.security.JwtUtil;
import restful.api.ezdine.security.SecurityConstants;

@EnableWebMvc
@SpringBootTest
@AutoConfigureMockMvc
public class OrderItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private final String email = "test@gmail.com";
    private final String password = "rahasia";

    private final Integer subTotal = 100;
    private final Double tax = 10.0;
    private final Integer totalPrice = 110;
    private final String status = "waiting payment";

    private final String categoryName = "appetizer";

    private final String foodCode = "spaghetti";
    private final String foodName = "spaghetti bolognese";
    private final Integer foodPrice = 20;
    private final Integer foodStock = 0;

    private final Integer itemQuantity = 2;
    private final Integer itemSubTotal = 100;

    @BeforeEach
    void setUp() {                

        orderItemRepository.deleteAll();
        orderRepository.deleteAll();        
        foodRepository.deleteAll();
        categoryRepository.deleteAll();        
        userRepository.deleteAll();

        RoleEntity role = roleRepository.findByName("ROLE_ADMIN").orElse(null);

        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(Collections.singletonList(role));        
        userRepository.save(user);

        CategoryEntity category = new CategoryEntity();
        category.setName(categoryName);
        category.setUserEntity(user);
        categoryRepository.save(category);

        FoodEntity food = new FoodEntity();
        food.setCode(foodCode);
        food.setName(foodName);
        food.setPrice(foodPrice);
        food.setStock(foodStock);
        food.setCategoryEntity(category);
        foodRepository.save(food);
    }

    @Test
    void testRegisterItemSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        FoodEntity food = foodRepository.findByName(foodName).orElse(null);

        Date date = new Date();

        OrderEntity order = new OrderEntity();
        order.setUserEntity(user);
        order.setOrderId(UUID.randomUUID().toString());
        order.setDate(date.toString());
        order.setSubTotal(subTotal);
        order.setTax(tax);
        order.setTotalPrice(totalPrice);
        order.setStatus(status);
        orderRepository.save(order);

        RegisterOrderItemRequest request = new RegisterOrderItemRequest();
        request.setOrderId(Integer.toString(order.getId())); 
        request.setFoodId(Integer.toString(food.getId()));
        request.setQuantity(itemQuantity);
        request.setSubTotal(itemSubTotal);

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + SecurityConstants.JWTexpiration);
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;

        mockMvc.perform(
                post("/api/orders/" + order.getId() + "/items")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<OrderItemResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());            
            assertEquals(order.getId(), response.getData().getOrderId());
            assertEquals(request.getFoodId(), Integer.toString(response.getData().getFoodId()));
            assertEquals(food.getName(), response.getData().getFoodName());
            assertEquals(request.getQuantity(), response.getData().getQuantity());            
            assertEquals(request.getSubTotal(), response.getData().getSubTotal());
        });
    }

    @Test
    void testRegisterItemBlank() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        FoodEntity food = foodRepository.findByName(foodName).orElse(null);

        Date date = new Date();

        OrderEntity order = new OrderEntity();
        order.setUserEntity(user);
        order.setOrderId(UUID.randomUUID().toString());
        order.setDate(date.toString());
        order.setSubTotal(subTotal);
        order.setTax(tax);
        order.setTotalPrice(totalPrice);
        order.setStatus(status);
        orderRepository.save(order);

        RegisterOrderItemRequest request = new RegisterOrderItemRequest();
        request.setOrderId(Integer.toString(order.getId())); 
        request.setFoodId(Integer.toString(food.getId()));
        request.setQuantity(null);
        request.setSubTotal(null);

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + SecurityConstants.JWTexpiration);
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;

        mockMvc.perform(
                post("/api/orders/" + order.getId() + "/items")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<OrderItemResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());            
        });
    }

    @Test
    void testRegisterItemBadOrder() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        FoodEntity food = foodRepository.findByName(foodName).orElse(null);

        Date date = new Date();

        OrderEntity order = new OrderEntity();
        order.setUserEntity(user);
        order.setOrderId(UUID.randomUUID().toString());
        order.setDate(date.toString());
        order.setSubTotal(subTotal);
        order.setTax(tax);
        order.setTotalPrice(totalPrice);
        order.setStatus(status);
        orderRepository.save(order);

        RegisterOrderItemRequest request = new RegisterOrderItemRequest();
        request.setOrderId(Integer.toString(order.getId())); 
        request.setFoodId(Integer.toString(food.getId()));
        request.setQuantity(null);
        request.setSubTotal(null);

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + SecurityConstants.JWTexpiration);
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;

        mockMvc.perform(
                post("/api/orders/" + order.getId() + "a/items")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<OrderItemResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());            
        });
    }

    @Test
    void testRegisterItemOrderNotFound() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        FoodEntity food = foodRepository.findByName(foodName).orElse(null);

        Date date = new Date();

        OrderEntity order = new OrderEntity();
        order.setUserEntity(user);
        order.setOrderId(UUID.randomUUID().toString());
        order.setDate(date.toString());
        order.setSubTotal(subTotal);
        order.setTax(tax);
        order.setTotalPrice(totalPrice);
        order.setStatus(status);
        orderRepository.save(order);

        RegisterOrderItemRequest request = new RegisterOrderItemRequest();
        request.setOrderId(Integer.toString(order.getId())); 
        request.setFoodId(Integer.toString(food.getId()));
        request.setQuantity(null);
        request.setSubTotal(null);

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + SecurityConstants.JWTexpiration);
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;

        mockMvc.perform(
                post("/api/orders/999999/items")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<OrderItemResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());            
        });
    }

    @Test
    void testRegisterItemInvalidToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        FoodEntity food = foodRepository.findByName(foodName).orElse(null);

        Date date = new Date();

        OrderEntity order = new OrderEntity();
        order.setUserEntity(user);
        order.setOrderId(UUID.randomUUID().toString());
        order.setDate(date.toString());
        order.setSubTotal(subTotal);
        order.setTax(tax);
        order.setTotalPrice(totalPrice);
        order.setStatus(status);
        orderRepository.save(order);

        RegisterOrderItemRequest request = new RegisterOrderItemRequest();
        request.setOrderId(Integer.toString(order.getId())); 
        request.setFoodId(Integer.toString(food.getId()));
        request.setQuantity(itemQuantity);
        request.setSubTotal(itemSubTotal);

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + SecurityConstants.JWTexpiration);
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken + "a";

        mockMvc.perform(
                post("/api/orders/" + order.getId() + "/items")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<OrderItemResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());            
        });
    }

    @Test
    void testRegisterItemTokenExpired() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        FoodEntity food = foodRepository.findByName(foodName).orElse(null);

        Date date = new Date();

        OrderEntity order = new OrderEntity();
        order.setUserEntity(user);
        order.setOrderId(UUID.randomUUID().toString());
        order.setDate(date.toString());
        order.setSubTotal(subTotal);
        order.setTax(tax);
        order.setTotalPrice(totalPrice);
        order.setStatus(status);
        orderRepository.save(order);

        RegisterOrderItemRequest request = new RegisterOrderItemRequest();
        request.setOrderId(Integer.toString(order.getId())); 
        request.setFoodId(Integer.toString(food.getId()));
        request.setQuantity(itemQuantity);
        request.setSubTotal(itemSubTotal);

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() - SecurityConstants.JWTexpiration);
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;

        mockMvc.perform(
                post("/api/orders/" + order.getId() + "/items")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<OrderItemResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());            
        });
    }

    @Test
    void testRegisterItemNoToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        FoodEntity food = foodRepository.findByName(foodName).orElse(null);

        Date date = new Date();

        OrderEntity order = new OrderEntity();
        order.setUserEntity(user);
        order.setOrderId(UUID.randomUUID().toString());
        order.setDate(date.toString());
        order.setSubTotal(subTotal);
        order.setTax(tax);
        order.setTotalPrice(totalPrice);
        order.setStatus(status);
        orderRepository.save(order);

        RegisterOrderItemRequest request = new RegisterOrderItemRequest();
        request.setOrderId(Integer.toString(order.getId())); 
        request.setFoodId(Integer.toString(food.getId()));
        request.setQuantity(itemQuantity);
        request.setSubTotal(itemSubTotal);

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + SecurityConstants.JWTexpiration);
        userRepository.save(user);        

        mockMvc.perform(
                post("/api/orders/" + order.getId() + "/items")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))                                            
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<OrderItemResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());            
        });
    }
}
