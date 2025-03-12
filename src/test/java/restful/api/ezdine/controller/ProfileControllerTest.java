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

import restful.api.ezdine.entity.ProfileEntity;
import restful.api.ezdine.entity.RoleEntity;
import restful.api.ezdine.entity.UserEntity;
import restful.api.ezdine.model.ProfileResponse;
import restful.api.ezdine.model.RegisterProfileRequest;
import restful.api.ezdine.model.UpdateProfileRequest;
import restful.api.ezdine.model.WebResponse;
import restful.api.ezdine.repository.ProfileRepository;
import restful.api.ezdine.repository.RoleRepository;
import restful.api.ezdine.repository.UserRepository;
import restful.api.ezdine.security.JwtUtil;
import restful.api.ezdine.security.SecurityConstants;

import com.fasterxml.jackson.core.type.TypeReference;

@EnableWebMvc
@SpringBootTest
@AutoConfigureMockMvc
public class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProfileRepository profileRepository;

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

    private final String firstname = "Bintang";
    private final String lastname = "Ginanjar";
    private final String address = "Pasirluyu";
    private final String phoneNumber = "081323456789";
    private final String city = "Bandung";
    private final String province = "West Java";
    private final String postalCode = "40254";

    @BeforeEach
    void setUp() {                

        profileRepository.deleteAll();
        userRepository.deleteAll();

        RoleEntity role = roleRepository.findByName("ROLE_ADMIN").orElse(null);

        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(Collections.singletonList(role));        
        userRepository.save(user);
    }

    @Test
    void testRegisterProfileSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RegisterProfileRequest request = new RegisterProfileRequest();        
        request.setFirstname(firstname);
        request.setLastname(lastname);
        request.setAddress(address);
        request.setPhoneNumber(phoneNumber);
        request.setCity(city);
        request.setProvince(province);
        request.setPostalCode(postalCode);

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
                post("/api/profiles")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<ProfileResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
            assertEquals(request.getFirstname(), response.getData().getFirstname());
            assertEquals(request.getLastname(), response.getData().getLastname());
            assertEquals(request.getAddress(), response.getData().getAddress());
            assertEquals(request.getPhoneNumber(), response.getData().getPhoneNumber());
            assertEquals(request.getCity(), response.getData().getCity());
            assertEquals(request.getProvince(), response.getData().getProvince());
            assertEquals(request.getPostalCode(), response.getData().getPostalCode());
        });
    }
    
    @Test
    void testRegisterProfileBlank() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RegisterProfileRequest request = new RegisterProfileRequest();        
        request.setFirstname("");
        request.setLastname(lastname);
        request.setAddress(address);
        request.setPhoneNumber(phoneNumber);
        request.setCity(city);
        request.setProvince(province);
        request.setPostalCode(postalCode);

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
                post("/api/profiles")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<ProfileResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());        
        });
    }

    @Test
    void testRegisterProfileInvalidToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RegisterProfileRequest request = new RegisterProfileRequest();        
        request.setFirstname(firstname);
        request.setLastname(lastname);
        request.setAddress(address);
        request.setPhoneNumber(phoneNumber);
        request.setCity(city);
        request.setProvince(province);
        request.setPostalCode(postalCode);

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
                post("/api/profiles")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<ProfileResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());        
        });
    }

    @Test
    void testRegisterProfileTokenExpired() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RegisterProfileRequest request = new RegisterProfileRequest();        
        request.setFirstname(firstname);
        request.setLastname(lastname);
        request.setAddress(address);
        request.setPhoneNumber(phoneNumber);
        request.setCity(city);
        request.setProvince(province);
        request.setPostalCode(postalCode);

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
                post("/api/profiles")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<ProfileResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());        
        });
    }

    @Test
    void testRegisterProfileNoToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RegisterProfileRequest request = new RegisterProfileRequest();        
        request.setFirstname(firstname);
        request.setLastname(lastname);
        request.setAddress(address);
        request.setPhoneNumber(phoneNumber);
        request.setCity(city);
        request.setProvince(province);
        request.setPostalCode(postalCode);

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + SecurityConstants.JWTexpiration);
        userRepository.save(user);

        mockMvc.perform(
                post("/api/profiles")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))                                               
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<ProfileResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());        
        });
    }

    @Test
    void testGetProfileSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        ProfileEntity profile = new ProfileEntity();
        profile.setUserEntity(user);        
        profile.setFirstname(firstname);
        profile.setLastname(lastname);
        profile.setAddress(address);
        profile.setPhoneNumber(phoneNumber);
        profile.setCity(city);
        profile.setProvince(province);
        profile.setPostalCode(postalCode);
        profileRepository.save(profile);

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
                get("/api/profiles")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<ProfileResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
            assertEquals(profile.getFirstname(), response.getData().getFirstname());
            assertEquals(profile.getLastname(), response.getData().getLastname());
            assertEquals(profile.getAddress(), response.getData().getAddress());
            assertEquals(profile.getPhoneNumber(), response.getData().getPhoneNumber());
            assertEquals(profile.getCity(), response.getData().getCity());
            assertEquals(profile.getProvince(), response.getData().getProvince());
            assertEquals(profile.getPostalCode(), response.getData().getPostalCode());
        });
    }

    @Test
    void testGetProfileInvalidToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        ProfileEntity profile = new ProfileEntity();
        profile.setUserEntity(user);        
        profile.setFirstname(firstname);
        profile.setLastname(lastname);
        profile.setAddress(address);
        profile.setPhoneNumber(phoneNumber);
        profile.setCity(city);
        profile.setProvince(province);
        profile.setPostalCode(postalCode);
        profileRepository.save(profile);

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
                get("/api/profiles")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<ProfileResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testGetProfileTokenExpired() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        ProfileEntity profile = new ProfileEntity();
        profile.setUserEntity(user);        
        profile.setFirstname(firstname);
        profile.setLastname(lastname);
        profile.setAddress(address);
        profile.setPhoneNumber(phoneNumber);
        profile.setCity(city);
        profile.setProvince(province);
        profile.setPostalCode(postalCode);
        profileRepository.save(profile);

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
                get("/api/profiles")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<ProfileResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testGetProfileNoToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        ProfileEntity profile = new ProfileEntity();
        profile.setUserEntity(user);        
        profile.setFirstname(firstname);
        profile.setLastname(lastname);
        profile.setAddress(address);
        profile.setPhoneNumber(phoneNumber);
        profile.setCity(city);
        profile.setProvince(province);
        profile.setPostalCode(postalCode);
        profileRepository.save(profile);

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + SecurityConstants.JWTexpiration);
        userRepository.save(user);    

        mockMvc.perform(
                get("/api/profiles")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                                                                     
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<ProfileResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testUpdateProfileSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        ProfileEntity profile = new ProfileEntity();
        profile.setUserEntity(user);        
        profile.setFirstname(firstname);
        profile.setLastname(lastname);
        profile.setAddress(address);
        profile.setPhoneNumber(phoneNumber);
        profile.setCity(city);
        profile.setProvince(province);
        profile.setPostalCode(postalCode);
        profileRepository.save(profile);

        UpdateProfileRequest request = new UpdateProfileRequest();        
        request.setFirstname(firstname + " updated");
        request.setLastname(lastname + " updated");
        request.setAddress(address + " updated");
        request.setPhoneNumber(phoneNumber + " updated");
        request.setCity(city + " updated");
        request.setProvince(province + " updated");
        request.setPostalCode(postalCode + " updated");

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
                patch("/api/profiles")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<ProfileResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
            assertEquals(request.getFirstname(), response.getData().getFirstname());
            assertEquals(request.getLastname(), response.getData().getLastname());
            assertEquals(request.getAddress(), response.getData().getAddress());
            assertEquals(request.getPhoneNumber(), response.getData().getPhoneNumber());
            assertEquals(request.getCity(), response.getData().getCity());
            assertEquals(request.getProvince(), response.getData().getProvince());
            assertEquals(request.getPostalCode(), response.getData().getPostalCode());
        });
    }

    @Test
    void testUpdateProfileInvalidToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        ProfileEntity profile = new ProfileEntity();
        profile.setUserEntity(user);        
        profile.setFirstname(firstname);
        profile.setLastname(lastname);
        profile.setAddress(address);
        profile.setPhoneNumber(phoneNumber);
        profile.setCity(city);
        profile.setProvince(province);
        profile.setPostalCode(postalCode);
        profileRepository.save(profile);

        UpdateProfileRequest request = new UpdateProfileRequest();        
        request.setFirstname(firstname + " updated");
        request.setLastname(lastname + " updated");
        request.setAddress(address + " updated");
        request.setPhoneNumber(phoneNumber + " updated");
        request.setCity(city + " updated");
        request.setProvince(province + " updated");
        request.setPostalCode(postalCode + " updated");

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
                patch("/api/profiles")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<ProfileResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());        
        });
    }

    @Test
    void testUpdateProfileTokenExpired() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        ProfileEntity profile = new ProfileEntity();
        profile.setUserEntity(user);        
        profile.setFirstname(firstname);
        profile.setLastname(lastname);
        profile.setAddress(address);
        profile.setPhoneNumber(phoneNumber);
        profile.setCity(city);
        profile.setProvince(province);
        profile.setPostalCode(postalCode);
        profileRepository.save(profile);

        UpdateProfileRequest request = new UpdateProfileRequest();        
        request.setFirstname(firstname + " updated");
        request.setLastname(lastname + " updated");
        request.setAddress(address + " updated");
        request.setPhoneNumber(phoneNumber + " updated");
        request.setCity(city + " updated");
        request.setProvince(province + " updated");
        request.setPostalCode(postalCode + " updated");

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
                patch("/api/profiles")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<ProfileResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());        
        });
    }

    @Test
    void testUpdateProfileNoToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        ProfileEntity profile = new ProfileEntity();
        profile.setUserEntity(user);        
        profile.setFirstname(firstname);
        profile.setLastname(lastname);
        profile.setAddress(address);
        profile.setPhoneNumber(phoneNumber);
        profile.setCity(city);
        profile.setProvince(province);
        profile.setPostalCode(postalCode);
        profileRepository.save(profile);

        UpdateProfileRequest request = new UpdateProfileRequest();        
        request.setFirstname(firstname + " updated");
        request.setLastname(lastname + " updated");
        request.setAddress(address + " updated");
        request.setPhoneNumber(phoneNumber + " updated");
        request.setCity(city + " updated");
        request.setProvince(province + " updated");
        request.setPostalCode(postalCode + " updated");

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + SecurityConstants.JWTexpiration);
        userRepository.save(user);        

        mockMvc.perform(
                patch("/api/profiles")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))                                             
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<ProfileResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());        
        });
    }
}
