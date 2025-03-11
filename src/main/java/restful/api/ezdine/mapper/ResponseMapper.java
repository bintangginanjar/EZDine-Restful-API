package restful.api.ezdine.mapper;

import java.util.List;
import java.util.stream.Collectors;

import restful.api.ezdine.entity.CategoryEntity;
import restful.api.ezdine.entity.UserEntity;
import restful.api.ezdine.model.CategoryResponse;
import restful.api.ezdine.model.TokenResponse;
import restful.api.ezdine.model.UserResponse;

public class ResponseMapper {

    public static UserResponse ToUserResponseMapper(UserEntity user) {        
        List<String> roles = user.getRoles().stream().map(p -> p.getName()).toList();

        return UserResponse.builder()                
                .email(user.getEmail())
                .role(roles)
                .build();
    }

    public static TokenResponse ToTokenResponseMapper(UserEntity user, String token, List<String> roles) {
        return TokenResponse.builder()
                .email(user.getEmail())
                .token(token)
                .roles(roles)
                .build();

    }

    public static CategoryResponse ToCategoryResponseMapper(CategoryEntity category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public static List<CategoryResponse> ToCategoryResponseListMapper(List<CategoryEntity> categories) {
        return categories.stream()
                            .map(
                                p -> new CategoryResponse(
                                    p.getId(),
                                    p.getName()
                                )).collect(Collectors.toList());
    }

}
