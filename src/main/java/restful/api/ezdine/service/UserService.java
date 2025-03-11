package restful.api.ezdine.service;

import java.util.Collections;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.extern.slf4j.Slf4j;
import restful.api.ezdine.entity.RoleEntity;
import restful.api.ezdine.entity.UserEntity;
import restful.api.ezdine.mapper.ResponseMapper;
import restful.api.ezdine.model.RegisterUserRequest;
import restful.api.ezdine.model.UpdateUserRequest;
import restful.api.ezdine.model.UserResponse;
import restful.api.ezdine.repository.RoleRepository;
import restful.api.ezdine.repository.UserRepository;

@Service
@Slf4j
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private ValidationService validationService;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, 
                        PasswordEncoder passwordEncoder, ValidationService validationService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.validationService = validationService;
    }

    @Transactional
    public UserResponse register(RegisterUserRequest request) {
        validationService.validate(request);

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already registered");
        }

        RoleEntity role = roleRepository.findByName(request.getRole()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Roles not found"));

        UserEntity user = new UserEntity();
        user.setEmail(request.getEmail());        
        user.setPassword(passwordEncoder.encode(request.getPassword()));        
        user.setRoles(Collections.singletonList(role));     

        userRepository.save(user);        

        return ResponseMapper.ToUserResponseMapper(user);
    }

    @Transactional(readOnly = true)
    public UserResponse get(Authentication authentication) {

        log.info("CURRENT NAME {}", authentication.getName());

        UserEntity user = userRepository.findByEmail(authentication.getName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));                    

        return ResponseMapper.ToUserResponseMapper(user);
    }

    @Transactional
    public UserResponse update(Authentication authentication, UpdateUserRequest request) {
        validationService.validate(request);

        UserEntity user = userRepository.findByEmail(authentication.getName())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
            
        if (Objects.nonNull(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        userRepository.save(user);        

        return ResponseMapper.ToUserResponseMapper(user);

    }
}
