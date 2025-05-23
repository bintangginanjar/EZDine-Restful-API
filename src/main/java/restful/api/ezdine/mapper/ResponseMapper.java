package restful.api.ezdine.mapper;

import java.util.List;
import java.util.stream.Collectors;

import restful.api.ezdine.entity.CategoryEntity;
import restful.api.ezdine.entity.FoodEntity;
import restful.api.ezdine.entity.OrderEntity;
import restful.api.ezdine.entity.OrderItemEntity;
import restful.api.ezdine.entity.ProfileEntity;
import restful.api.ezdine.entity.UserEntity;
import restful.api.ezdine.model.CategoryResponse;
import restful.api.ezdine.model.FoodResponse;
import restful.api.ezdine.model.OrderItemResponse;
import restful.api.ezdine.model.OrderResponse;
import restful.api.ezdine.model.ProfileResponse;
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

    public static FoodResponse ToFoodResponseMapper(FoodEntity food) {
        Integer categoryId = food.getCategoryEntity().getId();

        return FoodResponse.builder()
                .id(food.getId())
                .categoryId(categoryId)
                .categoryName(food.getCategoryEntity().getName())
                .code(food.getCode())
                .name(food.getName())
                .description(food.getDescription())
                .price(food.getPrice())
                .stock(food.getStock())
                .photoUrl(food.getPhotoUrl())
                .build();
    }

    public static List<FoodResponse> ToFoodResponseListMapper(List<FoodEntity> foods) {
        return foods.stream()
                    .map(
                        p -> new FoodResponse(
                            p.getId(),
                            p.getCategoryEntity().getId(),
                            p.getCategoryEntity().getName(),
                            p.getCode(),
                            p.getName(),
                            p.getDescription(),
                            p.getPrice(),
                            p.getStock(),
                            p.getPhotoUrl()
                        )).collect(Collectors.toList());
    }

    public static ProfileResponse ToProfileResponseMapper(ProfileEntity profile) {
        return ProfileResponse.builder()
                .id(profile.getId())
                .firstname(profile.getFirstname())
                .lastname(profile.getLastname())
                .address(profile.getAddress())
                .phoneNumber(profile.getPhoneNumber())
                .city(profile.getCity())
                .province(profile.getProvince())
                .postalCode(profile.getPostalCode())
                .build();
    }

    public static OrderResponse ToOrderResponseMapper(OrderEntity order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderId(order.getOrderId())    
                .date(order.getDate())            
                .subTotal(order.getSubTotal())        
                .tax(order.getTax())        
                .totalPrice(order.getTotalPrice())    
                .status(order.getStatus())
                .build();
    }

    public static List<OrderResponse> ToOrderResponseListMapper(List<OrderEntity> orders) {
        return orders.stream()
                        .map(
                            p -> new OrderResponse(
                                p.getId(),
                                p.getOrderId(),
                                p.getDate(),
                                p.getSubTotal(),
                                p.getTax(),
                                p.getTotalPrice(),
                                p.getStatus()
                            )).collect(Collectors.toList());
    }

    public static OrderItemResponse ToOrderItemResponseMapper(OrderItemEntity item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .orderId(item.getOrderEntity().getId())
                .foodId(item.getFoodEntity().getId())
                .foodName(item.getFoodEntity().getName())
                .quantity(item.getQuantity())
                .subTotal(item.getSubTotal())
                .build();
    }

    public static List<OrderItemResponse> ToOrderItemResponseListMapper(List<OrderItemEntity> items) {
        return items.stream()
                        .map(
                            p -> new OrderItemResponse(
                                p.getId(),
                                p.getOrderEntity().getId(),
                                p.getFoodEntity().getId(),
                                p.getFoodEntity().getName(),
                                p.getQuantity(),
                                p.getSubTotal()
                            )).collect(Collectors.toList());        
    }
}
