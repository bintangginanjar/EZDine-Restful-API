package restful.api.ezdine.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;

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

import restful.api.ezdine.entity.RoleEntity;
import restful.api.ezdine.entity.UserEntity;
import restful.api.ezdine.model.RegisterUserRequest;
import restful.api.ezdine.model.UpdateUserRequest;
import restful.api.ezdine.model.UserResponse;
import restful.api.ezdine.model.WebResponse;
import restful.api.ezdine.repository.RoleRepository;
import restful.api.ezdine.repository.UserRepository;
import restful.api.ezdine.security.JwtUtil;
import restful.api.ezdine.security.SecurityConstants;

@EnableWebMvc
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

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

    @BeforeEach
    void setUp() {
                
        userRepository.deleteAll();

    }

    @Test
    void testRegisterUserSuccess() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest();
        request.setEmail(email);
        request.setPassword(password);
        request.setRole("ROLE_USER");

        mockMvc.perform(
                post("/api/users")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<UserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
            assertEquals(request.getEmail(), response.getData().getEmail());
        });
    }

    @Test
    void testRegisterUserBlank() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest();
        request.setEmail("");
        request.setPassword("");
        request.setRole("ROLE_USER");

        mockMvc.perform(
                post("/api/users")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<UserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testGetUserSuccess() throws Exception {                        
        RoleEntity role = roleRepository.findByName("ROLE_USER").orElse(null);

        UserEntity user = new UserEntity();
        user.setEmail(email);      
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(Collections.singletonList(role));          
        userRepository.save(user);
        
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
                get("/api/users")
                        .accept(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                                             
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<UserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
            assertEquals(email, response.getData().getEmail());
        });
    }

    @Test
    void testGetUserInvalidToken() throws Exception {    
        
        RoleEntity role = roleRepository.findByName("ROLE_ADMIN").orElse(null);

        UserEntity user = new UserEntity();
        user.setEmail(email);      
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(Collections.singletonList(role));          
        userRepository.save(user);
                
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
                get("/api/users")
                        .accept(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                                             
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<UserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());            
        });
    }

    @Test
    void testGetUserTokenExpired() throws Exception {    
        
        RoleEntity role = roleRepository.findByName("ROLE_USER").orElse(null);

        UserEntity user = new UserEntity();
        user.setEmail(email);      
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(Collections.singletonList(role));          
        userRepository.save(user);
                
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
                get("/api/users")
                        .accept(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                                             
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<UserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());            
        });
    }

    @Test
    void testGetUserNoToken() throws Exception {    
        
        RoleEntity role = roleRepository.findByName("ROLE_ADMIN").orElse(null);

        UserEntity user = new UserEntity();
        user.setEmail(email);      
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(Collections.singletonList(role));          
        userRepository.save(user);
                
        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        String mockBearerToken = "Bearer " + mockToken;

        mockMvc.perform(
                get("/api/users")
                        .accept(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                                             
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<UserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());            
        });
    }

    @Test
    void testUpdateUserPasswordSuccess() throws Exception {                        
        RoleEntity role = roleRepository.findByName("ROLE_USER").orElse(null);

        UserEntity user = new UserEntity();
        user.setEmail(email);      
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(Collections.singletonList(role));          
        userRepository.save(user);
        
        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + SecurityConstants.JWTexpiration);
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;

        UpdateUserRequest request = new UpdateUserRequest();
        request.setPassword("123456");

        mockMvc.perform(
                patch("/api/users")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))                    
                        .header("Authorization", mockBearerToken)                                             
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<UserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());                     
        });
    }

    @Test
    void testUpdateUserPasswordInvalidToken() throws Exception {                    
        RoleEntity role = roleRepository.findByName("ROLE_ADMIN").orElse(null);

        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(Collections.singletonList(role));        
        userRepository.save(user);
        
        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + SecurityConstants.JWTexpiration);
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken + "a";

        UpdateUserRequest request = new UpdateUserRequest();
        request.setPassword("123456");

        mockMvc.perform(
                patch("/api/users")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))                    
                        .header("Authorization", mockBearerToken)                                             
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<UserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());                     
        });
    }

    @Test
    void testUpdateUserPasswordNoToken() throws Exception {        
        RoleEntity role = roleRepository.findByName("ROLE_USER").orElse(null);

        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(Collections.singletonList(role));        
        userRepository.save(user);
        
        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        String mockBearerToken = "Bearer " + mockToken;

        UpdateUserRequest request = new UpdateUserRequest();
        request.setPassword("123456");

        mockMvc.perform(
                patch("/api/users")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))                    
                        .header("Authorization", mockBearerToken)                                             
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<UserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());                     
        });
    }

    @Test
    void testUpdateUserPasswordTokenExpired() throws Exception {        
        RoleEntity role = roleRepository.findByName("ROLE_USER").orElse(null);

        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(Collections.singletonList(role));        
        userRepository.save(user);
        
        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() - SecurityConstants.JWTexpiration);
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;

        UpdateUserRequest request = new UpdateUserRequest();
        request.setPassword("123456");

        mockMvc.perform(
                patch("/api/users")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))                    
                        .header("Authorization", mockBearerToken)                                             
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<UserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());                     
        });
    }
}
