package restful.api.ezdine.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;
import java.util.Date;
import java.util.List;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import restful.api.ezdine.entity.OrderEntity;
import restful.api.ezdine.entity.RoleEntity;
import restful.api.ezdine.entity.UserEntity;
import restful.api.ezdine.model.OrderResponse;
import restful.api.ezdine.model.RegisterOrderRequest;
import restful.api.ezdine.model.UpdateOrderRequest;
import restful.api.ezdine.model.WebResponse;
import restful.api.ezdine.repository.OrderRepository;
import restful.api.ezdine.repository.RoleRepository;
import restful.api.ezdine.repository.UserRepository;
import restful.api.ezdine.security.JwtUtil;
import restful.api.ezdine.security.SecurityConstants;

import com.fasterxml.jackson.core.type.TypeReference;

@EnableWebMvc
@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private OrderRepository orderRepository;

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

    @BeforeEach
    void setUp() {                

        orderRepository.deleteAll();
        userRepository.deleteAll();

        RoleEntity role = roleRepository.findByName("ROLE_ADMIN").orElse(null);

        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(Collections.singletonList(role));        
        userRepository.save(user);
    }

    @Test
    void testRegisterOrderSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RegisterOrderRequest request = new RegisterOrderRequest();
        request.setSubTotal(subTotal);
        request.setTax(tax);
        request.setTotalPrice(totalPrice);
        request.setStatus(status);

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
                post("/api/orders")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<OrderResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
            assertEquals(request.getSubTotal(), response.getData().getSubTotal());
            assertEquals(request.getTax(), response.getData().getTax());
            assertEquals(request.getTotalPrice(), response.getData().getTotalPrice());
            assertEquals(request.getStatus(), response.getData().getStatus());
        });
    }

    @Test
    void testRegisterOrderBlank() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RegisterOrderRequest request = new RegisterOrderRequest();
        request.setSubTotal(null);
        request.setTax(null);
        request.setTotalPrice(null);
        request.setStatus("");

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
                post("/api/orders")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<OrderResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testRegisterOrderInvalidToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RegisterOrderRequest request = new RegisterOrderRequest();
        request.setSubTotal(subTotal);
        request.setTax(tax);
        request.setTotalPrice(totalPrice);
        request.setStatus(status);

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
                post("/api/orders")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<OrderResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());            
        });
    }

    @Test
    void testRegisterOrderTokenExpired() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RegisterOrderRequest request = new RegisterOrderRequest();
        request.setSubTotal(subTotal);
        request.setTax(tax);
        request.setTotalPrice(totalPrice);
        request.setStatus(status);

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
                post("/api/orders")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<OrderResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());            
        });
    }

    @Test
    void testRegisterOrderNoToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RegisterOrderRequest request = new RegisterOrderRequest();
        request.setSubTotal(subTotal);
        request.setTax(tax);
        request.setTotalPrice(totalPrice);
        request.setStatus(status);

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + SecurityConstants.JWTexpiration);
        userRepository.save(user);        

        mockMvc.perform(
                post("/api/orders")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))                                             
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<OrderResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());            
        });
    }

    @Test
    void testGetOrderSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

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
                get("/api/orders/" + order.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<OrderResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
            assertEquals(order.getSubTotal(), response.getData().getSubTotal());
            assertEquals(order.getTax(), response.getData().getTax());
            assertEquals(order.getTotalPrice(), response.getData().getTotalPrice());
            assertEquals(order.getStatus(), response.getData().getStatus());            
        });
    }

    @Test
    void testGetOrderBadId() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

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
                get("/api/orders/" + order.getId() + "a")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<OrderResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());          
        });
    }

    @Test
    void testGetOrderNotFound() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

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
                get("/api/orders/99999")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
                WebResponse<OrderResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());          
        });
    }

    @Test
    void testGetOrderInvalidToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

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
                get("/api/orders/" + order.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<OrderResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());          
        });
    }

    @Test
    void testGetOrderTokenExpired() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

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
                get("/api/orders/" + order.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<OrderResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());          
        });
    }

    @Test
    void testGetOrderNoToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

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

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + SecurityConstants.JWTexpiration);
        userRepository.save(user);        

        mockMvc.perform(
                get("/api/orders/" + order.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                                                                 
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<OrderResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());          
        });
    }

    @Test
    void testGetUserOrderSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

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
                get("/api/users/orders")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<List<OrderResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());            
        });
    }    

    @Test
    void testGetUserOrderInvalidToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

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
                get("/api/users/orders")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<List<OrderResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());          
        });
    }

    @Test
    void testGetUserOrderTokenExpired() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

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
                get("/api/users/orders")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<List<OrderResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());          
        });
    }

    @Test
    void testGetUserOrderNoToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

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

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + SecurityConstants.JWTexpiration);
        userRepository.save(user);        

        mockMvc.perform(
                get("/api/users/orders")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                                                                 
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<List<OrderResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());          
        });
    }

    @Test
    void testGetListOrderSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

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
                get("/api/orders")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<List<OrderResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());            
        });
    }    

    @Test
    void testGetListOrderInvalidToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

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
                get("/api/orders")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<List<OrderResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());          
        });
    }

    @Test
    void testGetListOrderTokenExpired() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

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
                get("/api/orders")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<List<OrderResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());          
        });
    }

    @Test
    void testGetListOrderNoToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

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

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + SecurityConstants.JWTexpiration);
        userRepository.save(user);        

        mockMvc.perform(
                get("/api/orders")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                                                                 
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<List<OrderResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());          
        });
    }

    @Test
    void testGetListOrderBadRole() throws Exception {        
        RoleEntity role = roleRepository.findByName("ROLE_USER").orElse(null);

        UserEntity user = userRepository.findByEmail(email).orElse(null);
        user.setRoles(Collections.singletonList(role));        
        userRepository.save(user);

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
                get("/api/orders")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<List<OrderResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());          
        });
    }

    @Test
    void testUpdateOrderSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

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

        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setSubTotal(subTotal + 10);
        request.setTax(tax + 0.35);
        request.setTotalPrice(totalPrice + 10);
        request.setStatus("Paid");

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
                patch("/api/orders/" + order.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<OrderResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
            assertEquals(request.getSubTotal(), response.getData().getSubTotal());
            assertEquals(request.getTax(), response.getData().getTax());
            assertEquals(request.getTotalPrice(), response.getData().getTotalPrice());
            assertEquals(request.getStatus(), response.getData().getStatus());
        });
    }

    @Test
    void testUpdateOrderBadId() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

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

        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setSubTotal(subTotal + 10);
        request.setTax(tax + 0.35);
        request.setTotalPrice(totalPrice + 10);
        request.setStatus("Paid");

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
                patch("/api/orders/" + order.getId() + "a")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<OrderResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());            
        });
    }

    @Test
    void testUpdateOrderNotFound() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

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

        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setSubTotal(subTotal + 10);
        request.setTax(tax + 0.35);
        request.setTotalPrice(totalPrice + 10);
        request.setStatus("Paid");

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
                patch("/api/orders/99999")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
                WebResponse<OrderResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());            
        });
    }

    @Test
    void testUpdateOrderInvalidToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

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

        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setSubTotal(subTotal + 10);
        request.setTax(tax + 0.35);
        request.setTotalPrice(totalPrice + 10);
        request.setStatus("Paid");

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
                patch("/api/orders/" + order.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<OrderResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());            
        });
    }

    @Test
    void testUpdateOrderTokenExpired() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

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

        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setSubTotal(subTotal + 10);
        request.setTax(tax + 0.35);
        request.setTotalPrice(totalPrice + 10);
        request.setStatus("Paid");

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
                patch("/api/orders/" + order.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<OrderResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());            
        });
    }

    @Test
    void testUpdateOrderNoToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

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

        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setSubTotal(subTotal + 10);
        request.setTax(tax + 0.35);
        request.setTotalPrice(totalPrice + 10);
        request.setStatus("Paid");

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + SecurityConstants.JWTexpiration);
        userRepository.save(user);

        mockMvc.perform(
                patch("/api/orders/" + order.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))                                               
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<OrderResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());            
        });
    }
}
