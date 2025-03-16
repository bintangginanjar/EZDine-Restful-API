package restful.api.ezdine.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;
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

import restful.api.ezdine.entity.CategoryEntity;
import restful.api.ezdine.entity.FoodEntity;
import restful.api.ezdine.entity.RoleEntity;
import restful.api.ezdine.entity.UserEntity;
import restful.api.ezdine.model.FoodResponse;
import restful.api.ezdine.model.RegisterFoodRequest;
import restful.api.ezdine.model.UpdateFoodRequest;
import restful.api.ezdine.model.WebResponse;
import restful.api.ezdine.repository.CategoryRepository;
import restful.api.ezdine.repository.FoodRepository;
import restful.api.ezdine.repository.RoleRepository;
import restful.api.ezdine.repository.UserRepository;
import restful.api.ezdine.security.JwtUtil;
import restful.api.ezdine.security.SecurityConstants;

import com.fasterxml.jackson.core.type.TypeReference;

@EnableWebMvc
@SpringBootTest
@AutoConfigureMockMvc
public class FoodControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

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

    private final String categoryName = "appetizer";

    private final String foodCode = "spaghetti";
    private final String foodName = "spaghetti bolognese";
    private final Double foodPrice = 20.0;
    private final Integer foodStock = 0;

    @BeforeEach
    void setUp() {                

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
    }

    @Test
    void testRegisterFoodSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        CategoryEntity category = categoryRepository.findByName(categoryName).orElse(null);

        RegisterFoodRequest request = new RegisterFoodRequest();
        request.setCode(foodCode);
        request.setName(foodName);
        request.setPrice(foodPrice);
        request.setStock(foodStock);                

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
                post("/api/categories/" + category.getId() + "/foods")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<FoodResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
            
            assertEquals(category.getId(), response.getData().getCategoryId());
            assertEquals(request.getCode(), response.getData().getCode());
            assertEquals(request.getName(), response.getData().getName());
            assertEquals(request.getPrice(), response.getData().getPrice());
            assertEquals(request.getStock(), response.getData().getStock());            
        });
    }

    @Test
    void testRegisterFoodDuplicate() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        CategoryEntity category = categoryRepository.findByName(categoryName).orElse(null);

        FoodEntity food = new FoodEntity();
        food.setCode(foodCode);
        food.setName(foodName);
        food.setPrice(foodPrice);
        food.setStock(foodStock);
        food.setCategoryEntity(category);
        foodRepository.save(food);

        RegisterFoodRequest request = new RegisterFoodRequest();
        request.setCode(foodCode);
        request.setName(foodName);
        request.setPrice(foodPrice);
        request.setStock(foodStock);                

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
                post("/api/categories/" + category.getId() + "/foods")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<FoodResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());                       
        });
    }

    @Test
    void testRegisterFoodBlank() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        CategoryEntity category = categoryRepository.findByName(categoryName).orElse(null);

        RegisterFoodRequest request = new RegisterFoodRequest();
        request.setCode("");
        request.setName("");
        request.setPrice(null);
        request.setStock(null);                

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
                post("/api/categories/" + category.getId() + "/foods")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<FoodResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());                        
        });
    }

    @Test
    void testRegisterFoodBadCategory() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        //CategoryEntity category = categoryRepository.findByName(categoryName).orElse(null);

        RegisterFoodRequest request = new RegisterFoodRequest();
        request.setCode(foodCode);
        request.setName(foodName);
        request.setPrice(foodPrice);
        request.setStock(foodStock);                

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
                post("/api/categories/1a/foods")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<FoodResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());             
        });
    }

    @Test
    void testRegisterFoodCategoryNotFound() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        //CategoryEntity category = categoryRepository.findByName(categoryName).orElse(null);

        RegisterFoodRequest request = new RegisterFoodRequest();
        request.setCode(foodCode);
        request.setName(foodName);
        request.setPrice(foodPrice);
        request.setStock(foodStock);                

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
                post("/api/categories/999999/foods")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
                WebResponse<FoodResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());             
        });
    }

    @Test
    void testRegisterFoodInvalidToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        CategoryEntity category = categoryRepository.findByName(categoryName).orElse(null);

        RegisterFoodRequest request = new RegisterFoodRequest();
        request.setCode(foodCode);
        request.setName(foodName);
        request.setPrice(foodPrice);
        request.setStock(foodStock);                

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
                post("/api/categories/" + category.getId() + "/foods")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<FoodResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());                        
        });
    }

    @Test
    void testRegisterFoodTokenExpired() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        CategoryEntity category = categoryRepository.findByName(categoryName).orElse(null);

        RegisterFoodRequest request = new RegisterFoodRequest();
        request.setCode(foodCode);
        request.setName(foodName);
        request.setPrice(foodPrice);
        request.setStock(foodStock);                

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
                post("/api/categories/" + category.getId() + "/foods")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<FoodResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());                        
        });
    }

    @Test
    void testRegisterFoodNoToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        CategoryEntity category = categoryRepository.findByName(categoryName).orElse(null);

        RegisterFoodRequest request = new RegisterFoodRequest();
        request.setCode(foodCode);
        request.setName(foodName);
        request.setPrice(foodPrice);
        request.setStock(foodStock);                

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + SecurityConstants.JWTexpiration);
        userRepository.save(user);        

        mockMvc.perform(
                post("/api/categories/" + category.getId() + "/foods")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))                                              
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<FoodResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());                        
        });
    }

    @Test
    void testRegisterFoodBadRole() throws Exception {
        RoleEntity role = roleRepository.findByName("ROLE_USER").orElse(null);

        UserEntity user = userRepository.findByEmail(email).orElse(null);
        user.setRoles(Collections.singletonList(role));        
        userRepository.save(user);
        
        CategoryEntity category = categoryRepository.findByName(categoryName).orElse(null);

        RegisterFoodRequest request = new RegisterFoodRequest();
        request.setCode(foodCode);
        request.setName(foodName);
        request.setPrice(foodPrice);
        request.setStock(foodStock);                

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + SecurityConstants.JWTexpiration);
        userRepository.save(user);        

        mockMvc.perform(
                post("/api/categories/" + category.getId() + "/foods")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))                                              
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<FoodResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());                        
        });
    }

    @Test
    void testGetFoodSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        CategoryEntity category = categoryRepository.findByName(categoryName).orElse(null);

        FoodEntity food = new FoodEntity();
        food.setCode(foodCode);
        food.setName(foodName);
        food.setPrice(foodPrice);
        food.setStock(foodStock);
        food.setCategoryEntity(category);
        foodRepository.save(food);

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
                get("/api/categories/" + category.getId() + "/foods/" + food.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<FoodResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
                        
            assertEquals(food.getCode(), response.getData().getCode());
            assertEquals(food.getName(), response.getData().getName());
            assertEquals(food.getPrice(), response.getData().getPrice());
            assertEquals(food.getStock(), response.getData().getStock());            
        });
    }

    @Test
    void testGetFoodBadCategory() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        CategoryEntity category = categoryRepository.findByName(categoryName).orElse(null);

        FoodEntity food = new FoodEntity();
        food.setCode(foodCode);
        food.setName(foodName);
        food.setPrice(foodPrice);
        food.setStock(foodStock);
        food.setCategoryEntity(category);
        foodRepository.save(food);

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
                get("/api/categories/" + category.getId() + "a/foods/" + food.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<FoodResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());            
        });
    }

    @Test
    void testGetFoodCategoryNotFound() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        CategoryEntity category = categoryRepository.findByName(categoryName).orElse(null);

        FoodEntity food = new FoodEntity();
        food.setCode(foodCode);
        food.setName(foodName);
        food.setPrice(foodPrice);
        food.setStock(foodStock);
        food.setCategoryEntity(category);
        foodRepository.save(food);

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
                get("/api/categories/999999/foods/" + food.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
                WebResponse<FoodResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());            
        });
    }

    @Test
    void testGetFoodBadFood() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        CategoryEntity category = categoryRepository.findByName(categoryName).orElse(null);

        FoodEntity food = new FoodEntity();
        food.setCode(foodCode);
        food.setName(foodName);
        food.setPrice(foodPrice);
        food.setStock(foodStock);
        food.setCategoryEntity(category);
        foodRepository.save(food);

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
                get("/api/categories/" + category.getId() + "/foods/" + food.getId() + "a")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<FoodResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());            
        });
    }

    @Test
    void testGetFoodNotFound() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        CategoryEntity category = categoryRepository.findByName(categoryName).orElse(null);

        FoodEntity food = new FoodEntity();
        food.setCode(foodCode);
        food.setName(foodName);
        food.setPrice(foodPrice);
        food.setStock(foodStock);
        food.setCategoryEntity(category);
        foodRepository.save(food);

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
                get("/api/categories/" + category.getId() + "/foods/99999")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
                WebResponse<FoodResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());            
        });
    }

    @Test
    void testGetFoodInvalidToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        CategoryEntity category = categoryRepository.findByName(categoryName).orElse(null);

        FoodEntity food = new FoodEntity();
        food.setCode(foodCode);
        food.setName(foodName);
        food.setPrice(foodPrice);
        food.setStock(foodStock);
        food.setCategoryEntity(category);
        foodRepository.save(food);

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
                get("/api/categories/" + category.getId() + "/foods/" + food.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<FoodResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());                        
        });
    }

    @Test
    void testGetFoodTokenExpired() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        CategoryEntity category = categoryRepository.findByName(categoryName).orElse(null);

        FoodEntity food = new FoodEntity();
        food.setCode(foodCode);
        food.setName(foodName);
        food.setPrice(foodPrice);
        food.setStock(foodStock);
        food.setCategoryEntity(category);
        foodRepository.save(food);

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
                get("/api/categories/" + category.getId() + "/foods/" + food.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<FoodResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());                        
        });
    }

    @Test
    void testGetFoodNoToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        CategoryEntity category = categoryRepository.findByName(categoryName).orElse(null);

        FoodEntity food = new FoodEntity();
        food.setCode(foodCode);
        food.setName(foodName);
        food.setPrice(foodPrice);
        food.setStock(foodStock);
        food.setCategoryEntity(category);
        foodRepository.save(food);

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + SecurityConstants.JWTexpiration);
        userRepository.save(user);        

        mockMvc.perform(
                get("/api/categories/" + category.getId() + "/foods/" + food.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                                                                    
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<FoodResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());                        
        });
    }

    @Test
    void testUpdateFoodSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        CategoryEntity category = categoryRepository.findByName(categoryName).orElse(null);

        FoodEntity food = new FoodEntity();
        food.setCode(foodCode);
        food.setName(foodName);
        food.setPrice(foodPrice);
        food.setStock(foodStock);
        food.setCategoryEntity(category);
        foodRepository.save(food);
        
        UpdateFoodRequest request = new UpdateFoodRequest();
        request.setCode(foodCode + " updated");
        request.setName(foodName + " updated");
        request.setPrice(foodPrice + 5);
        request.setStock(foodStock + 10);        

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
                patch("/api/categories/" + category.getId() + "/foods/" + food.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<FoodResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
            
            assertEquals(category.getId(), response.getData().getCategoryId());
            assertEquals(request.getCode(), response.getData().getCode());
            assertEquals(request.getName(), response.getData().getName());
            assertEquals(request.getPrice(), response.getData().getPrice());
            assertEquals(request.getStock(), response.getData().getStock());            
        });
    }

    @Test
    void testUpdateFoodDuplicate() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        CategoryEntity category = categoryRepository.findByName(categoryName).orElse(null);

        FoodEntity food = new FoodEntity();
        food.setCode(foodCode);
        food.setName(foodName);
        food.setPrice(foodPrice);
        food.setStock(foodStock);
        food.setCategoryEntity(category);
        foodRepository.save(food);

        FoodEntity dessert = new FoodEntity();
        dessert.setCode("dessert");
        dessert.setName("dessert");
        dessert.setPrice(foodPrice);
        dessert.setStock(foodStock);
        dessert.setCategoryEntity(category);
        foodRepository.save(dessert);
        
        UpdateFoodRequest request = new UpdateFoodRequest();
        request.setCode("dessert");
        request.setName("dessert");
        request.setPrice(foodPrice + 5);
        request.setStock(foodStock + 10);        

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
                patch("/api/categories/" + category.getId() + "/foods/" + food.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<FoodResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());                       
        });
    }

    @Test
    void testUpdateFoodBadCategory() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        CategoryEntity category = categoryRepository.findByName(categoryName).orElse(null);

        FoodEntity food = new FoodEntity();
        food.setCode(foodCode);
        food.setName(foodName);
        food.setPrice(foodPrice);
        food.setStock(foodStock);
        food.setCategoryEntity(category);
        foodRepository.save(food);
        
        UpdateFoodRequest request = new UpdateFoodRequest();
        request.setCode(foodCode + " updated");
        request.setName(foodName + " updated");
        request.setPrice(foodPrice + 5);
        request.setStock(foodStock + 10);        

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
                patch("/api/categories/" + category.getId() + "a/foods/" + food.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<FoodResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());                    
        });
    }

    @Test
    void testUpdateFoodCategoryNotFound() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        CategoryEntity category = categoryRepository.findByName(categoryName).orElse(null);

        FoodEntity food = new FoodEntity();
        food.setCode(foodCode);
        food.setName(foodName);
        food.setPrice(foodPrice);
        food.setStock(foodStock);
        food.setCategoryEntity(category);
        foodRepository.save(food);
        
        UpdateFoodRequest request = new UpdateFoodRequest();
        request.setCode(foodCode + " updated");
        request.setName(foodName + " updated");
        request.setPrice(foodPrice + 5);
        request.setStock(foodStock + 10);        

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
                patch("/api/categories/11111/foods/" + food.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
                WebResponse<FoodResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());                    
        });
    }

    @Test
    void testUpdateFoodBadId() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        CategoryEntity category = categoryRepository.findByName(categoryName).orElse(null);

        FoodEntity food = new FoodEntity();
        food.setCode(foodCode);
        food.setName(foodName);
        food.setPrice(foodPrice);
        food.setStock(foodStock);
        food.setCategoryEntity(category);
        foodRepository.save(food);
        
        UpdateFoodRequest request = new UpdateFoodRequest();
        request.setCode(foodCode + " updated");
        request.setName(foodName + " updated");
        request.setPrice(foodPrice + 5);
        request.setStock(foodStock + 10);        

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
                patch("/api/categories/" + category.getId() + "/foods/" + food.getId() + "a")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<FoodResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());                    
        });
    }

    @Test
    void testUpdateFoodNotFound() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        CategoryEntity category = categoryRepository.findByName(categoryName).orElse(null);

        FoodEntity food = new FoodEntity();
        food.setCode(foodCode);
        food.setName(foodName);
        food.setPrice(foodPrice);
        food.setStock(foodStock);
        food.setCategoryEntity(category);
        foodRepository.save(food);
        
        UpdateFoodRequest request = new UpdateFoodRequest();
        request.setCode(foodCode + " updated");
        request.setName(foodName + " updated");
        request.setPrice(foodPrice + 5);
        request.setStock(foodStock + 10);        

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
                patch("/api/categories/" + category.getId() + "/foods/11111")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
                WebResponse<FoodResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());                    
        });
    }

    @Test
    void testUpdateFoodInvalidToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        CategoryEntity category = categoryRepository.findByName(categoryName).orElse(null);

        FoodEntity food = new FoodEntity();
        food.setCode(foodCode);
        food.setName(foodName);
        food.setPrice(foodPrice);
        food.setStock(foodStock);
        food.setCategoryEntity(category);
        foodRepository.save(food);
        
        UpdateFoodRequest request = new UpdateFoodRequest();
        request.setCode(foodCode + " updated");
        request.setName(foodName + " updated");
        request.setPrice(foodPrice + 5);
        request.setStock(foodStock + 10);        

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
                patch("/api/categories/" + category.getId() + "/foods/" + food.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<FoodResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());           
        });
    }

    @Test
    void testUpdateFoodTokenExpired() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        CategoryEntity category = categoryRepository.findByName(categoryName).orElse(null);

        FoodEntity food = new FoodEntity();
        food.setCode(foodCode);
        food.setName(foodName);
        food.setPrice(foodPrice);
        food.setStock(foodStock);
        food.setCategoryEntity(category);
        foodRepository.save(food);
        
        UpdateFoodRequest request = new UpdateFoodRequest();
        request.setCode(foodCode + " updated");
        request.setName(foodName + " updated");
        request.setPrice(foodPrice + 5);
        request.setStock(foodStock + 10);        

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
                patch("/api/categories/" + category.getId() + "/foods/" + food.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<FoodResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());           
        });
    }

    @Test
    void testUpdateFoodNoToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        CategoryEntity category = categoryRepository.findByName(categoryName).orElse(null);

        FoodEntity food = new FoodEntity();
        food.setCode(foodCode);
        food.setName(foodName);
        food.setPrice(foodPrice);
        food.setStock(foodStock);
        food.setCategoryEntity(category);
        foodRepository.save(food);
        
        UpdateFoodRequest request = new UpdateFoodRequest();
        request.setCode(foodCode + " updated");
        request.setName(foodName + " updated");
        request.setPrice(foodPrice + 5);
        request.setStock(foodStock + 10);        

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + SecurityConstants.JWTexpiration);
        userRepository.save(user);        

        mockMvc.perform(
                patch("/api/categories/" + category.getId() + "/foods/" + food.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))                                              
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<FoodResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());           
        });
    }

    @Test
    void testUpdateFoodBadRole() throws Exception {
        RoleEntity role = roleRepository.findByName("ROLE_USER").orElse(null);
        CategoryEntity category = categoryRepository.findByName(categoryName).orElse(null);

        UserEntity user = userRepository.findByEmail(email).orElse(null);
        user.setRoles(Collections.singletonList(role));        
        userRepository.save(user);

        FoodEntity food = new FoodEntity();
        food.setCode(foodCode);
        food.setName(foodName);
        food.setPrice(foodPrice);
        food.setStock(foodStock);
        food.setCategoryEntity(category);
        foodRepository.save(food);
        
        UpdateFoodRequest request = new UpdateFoodRequest();
        request.setCode(foodCode + " updated");
        request.setName(foodName + " updated");
        request.setPrice(foodPrice + 5);
        request.setStock(foodStock + 10);        

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
                patch("/api/categories/" + category.getId() + "/foods/" + food.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<FoodResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());           
        });
    }

    @Test
    void testDeleteFoodSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        CategoryEntity category = categoryRepository.findByName(categoryName).orElse(null);

        FoodEntity food = new FoodEntity();
        food.setCode(foodCode);
        food.setName(foodName);
        food.setPrice(foodPrice);
        food.setStock(foodStock);
        food.setCategoryEntity(category);
        foodRepository.save(food);

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
                delete("/api/categories/" + category.getId() + "/foods/" + food.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());            
        });
    }

    @Test
    void testDeleteFoodBadCategory() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        CategoryEntity category = categoryRepository.findByName(categoryName).orElse(null);

        FoodEntity food = new FoodEntity();
        food.setCode(foodCode);
        food.setName(foodName);
        food.setPrice(foodPrice);
        food.setStock(foodStock);
        food.setCategoryEntity(category);
        foodRepository.save(food);

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
                delete("/api/categories/" + category.getId() + "a/foods/" + food.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());            
        });
    }

    @Test
    void testDeleteFoodCategoryNotFound() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        CategoryEntity category = categoryRepository.findByName(categoryName).orElse(null);

        FoodEntity food = new FoodEntity();
        food.setCode(foodCode);
        food.setName(foodName);
        food.setPrice(foodPrice);
        food.setStock(foodStock);
        food.setCategoryEntity(category);
        foodRepository.save(food);

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
                delete("/api/categories/999999/foods/" + food.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
                WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());            
        });
    }

    @Test
    void testDeleteFoodBadFood() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        CategoryEntity category = categoryRepository.findByName(categoryName).orElse(null);

        FoodEntity food = new FoodEntity();
        food.setCode(foodCode);
        food.setName(foodName);
        food.setPrice(foodPrice);
        food.setStock(foodStock);
        food.setCategoryEntity(category);
        foodRepository.save(food);

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
                delete("/api/categories/" + category.getId() + "/foods/" + food.getId() + "a")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());            
        });
    }

    @Test
    void testDeleteFoodNotFound() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        CategoryEntity category = categoryRepository.findByName(categoryName).orElse(null);

        FoodEntity food = new FoodEntity();
        food.setCode(foodCode);
        food.setName(foodName);
        food.setPrice(foodPrice);
        food.setStock(foodStock);
        food.setCategoryEntity(category);
        foodRepository.save(food);

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
                delete("/api/categories/" + category.getId() + "/foods/99999")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
                WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());            
        });
    }

    @Test
    void testDeleteFoodInvalidToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        CategoryEntity category = categoryRepository.findByName(categoryName).orElse(null);

        FoodEntity food = new FoodEntity();
        food.setCode(foodCode);
        food.setName(foodName);
        food.setPrice(foodPrice);
        food.setStock(foodStock);
        food.setCategoryEntity(category);
        foodRepository.save(food);

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
                delete("/api/categories/" + category.getId() + "/foods/" + food.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());                        
        });
    }

    @Test
    void testDeleteFoodTokenExpired() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        CategoryEntity category = categoryRepository.findByName(categoryName).orElse(null);

        FoodEntity food = new FoodEntity();
        food.setCode(foodCode);
        food.setName(foodName);
        food.setPrice(foodPrice);
        food.setStock(foodStock);
        food.setCategoryEntity(category);
        foodRepository.save(food);

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
                delete("/api/categories/" + category.getId() + "/foods/" + food.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());                        
        });
    }

    @Test
    void testDeleteFoodNoToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        CategoryEntity category = categoryRepository.findByName(categoryName).orElse(null);

        FoodEntity food = new FoodEntity();
        food.setCode(foodCode);
        food.setName(foodName);
        food.setPrice(foodPrice);
        food.setStock(foodStock);
        food.setCategoryEntity(category);
        foodRepository.save(food);

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + SecurityConstants.JWTexpiration);
        userRepository.save(user);        

        mockMvc.perform(
                delete("/api/categories/" + category.getId() + "/foods/" + food.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                                                                    
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());                        
        });
    }

    @Test
    void testDeleteFoodBadRole() throws Exception {
        RoleEntity role = roleRepository.findByName("ROLE_USER").orElse(null);
        CategoryEntity category = categoryRepository.findByName(categoryName).orElse(null);

        UserEntity user = userRepository.findByEmail(email).orElse(null);
        user.setRoles(Collections.singletonList(role));        
        userRepository.save(user);

        FoodEntity food = new FoodEntity();
        food.setCode(foodCode);
        food.setName(foodName);
        food.setPrice(foodPrice);
        food.setStock(foodStock);
        food.setCategoryEntity(category);
        foodRepository.save(food);       

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
                delete("/api/categories/" + category.getId() + "/foods/" + food.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());           
        });
    }
}
