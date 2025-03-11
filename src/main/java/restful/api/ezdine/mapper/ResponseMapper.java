package restful.api.ezdine.mapper;

import java.util.List;

import restful.api.ezdine.entity.UserEntity;
import restful.api.ezdine.model.UserResponse;

public class ResponseMapper {

    public static UserResponse ToUserResponseMapper(UserEntity user) {        
        List<String> roles = user.getRoles().stream().map(p -> p.getName()).toList();

        return UserResponse.builder()                
                .email(user.getEmail())
                .role(roles)
                .build();
    }

}
