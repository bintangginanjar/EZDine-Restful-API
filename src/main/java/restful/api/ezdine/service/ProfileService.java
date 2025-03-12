package restful.api.ezdine.service;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import restful.api.ezdine.entity.ProfileEntity;
import restful.api.ezdine.entity.UserEntity;
import restful.api.ezdine.mapper.ResponseMapper;
import restful.api.ezdine.model.ProfileResponse;
import restful.api.ezdine.model.RegisterProfileRequest;
import restful.api.ezdine.model.UpdateProfileRequest;
import restful.api.ezdine.repository.ProfileRepository;
import restful.api.ezdine.repository.UserRepository;

@Service
public class ProfileService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ValidationService validationService;

    public ProfileService(UserRepository userRepository, ProfileRepository profileRepository,
            ValidationService validationService) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.validationService = validationService;
    }

    @Transactional
    public ProfileResponse register(Authentication authentication, RegisterProfileRequest request) {
        validationService.validate(request);

        UserEntity user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        ProfileEntity profile = new ProfileEntity();
        profile.setUserEntity(user);
        profile.setFirstname(request.getFirstname());
        profile.setLastname(request.getLastname());
        profile.setAddress(request.getAddress());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setCity(request.getCity());
        profile.setProvince(request.getProvince());
        profile.setPostalCode(request.getPostalCode());
        profileRepository.save(profile);

        return ResponseMapper.ToProfileResponseMapper(profile);
    }

    @Transactional(readOnly = true)
    public ProfileResponse get(Authentication authentication) {

        UserEntity user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    
        ProfileEntity profile = profileRepository.findFirstByUserEntity(user)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        return ResponseMapper.ToProfileResponseMapper(profile);
    }

    @Transactional
    public ProfileResponse update(Authentication authentication, UpdateProfileRequest request) {

        UserEntity user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    
        ProfileEntity profile = profileRepository.findFirstByUserEntity(user)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        if (Objects.nonNull(request.getFirstname())) {
            profile.setFirstname(request.getFirstname());
        }

        if (Objects.nonNull(request.getLastname())) {
            profile.setLastname(request.getLastname());
        }

        if (Objects.nonNull(request.getAddress())) {
            profile.setAddress(request.getAddress());
        }

        if (Objects.nonNull(request.getPhoneNumber())) {
            profile.setPhoneNumber(request.getPhoneNumber());
        }

        if (Objects.nonNull(request.getCity())) {
            profile.setCity(request.getCity());
        }

        if (Objects.nonNull(request.getProvince())) {
            profile.setProvince(request.getProvince());
        }

        if (Objects.nonNull(request.getPostalCode())) {
            profile.setPostalCode(request.getPostalCode());
        }

        return ResponseMapper.ToProfileResponseMapper(profile);
    }

}
