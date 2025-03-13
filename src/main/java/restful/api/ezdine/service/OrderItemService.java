package restful.api.ezdine.service;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import restful.api.ezdine.entity.FoodEntity;
import restful.api.ezdine.entity.OrderEntity;
import restful.api.ezdine.entity.OrderItemEntity;
import restful.api.ezdine.entity.UserEntity;
import restful.api.ezdine.mapper.ResponseMapper;
import restful.api.ezdine.model.OrderItemResponse;
import restful.api.ezdine.model.RegisterOrderItemRequest;
import restful.api.ezdine.model.UpdateOrderItemRequest;
import restful.api.ezdine.repository.FoodRepository;
import restful.api.ezdine.repository.OrderItemRepository;
import restful.api.ezdine.repository.OrderRepository;
import restful.api.ezdine.repository.UserRepository;

@Service
public class OrderItemService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

    @Autowired
    FoodRepository foodRepository;

    @Autowired
    ValidationService validationService;

    public OrderItemService(UserRepository userRepository, OrderRepository orderRepository,
            OrderItemRepository orderItemRepository, FoodRepository foodRepository,
            ValidationService validationService) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.foodRepository = foodRepository;
        this.validationService = validationService;          
    }

    @Transactional
    public OrderItemResponse register(Authentication authentication, String strOrderId, RegisterOrderItemRequest request) {
        validationService.validate(request);

        Integer orderId = 0;

        try {
            orderId = Integer.parseInt(strOrderId);       
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad request");
        }

        UserEntity user = userRepository.findByEmail(authentication.getName())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        OrderEntity order = orderRepository.findFirstByUserEntityAndId(user, orderId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        FoodEntity food = foodRepository.findFirstById(Integer.parseInt(request.getFoodId()))
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Food not found"));

        OrderItemEntity item = new OrderItemEntity();
        item.setFoodEntity(food);
        item.setOrderEntity(order);
        item.setQuantity(request.getQuantity());
        item.setSubTotal(request.getSubTotal());
        orderItemRepository.save(item);

        return ResponseMapper.ToOrderItemResponseMapper(item);
    }

    @Transactional(readOnly = true)
    public OrderItemResponse get(Authentication authentication, String strOrderId, String strItemId) {
        Integer orderId = 0;
        Integer itemId = 0;

        try {
            orderId = Integer.parseInt(strOrderId);
            itemId = Integer.parseInt(strItemId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad request");
        }

        UserEntity user = userRepository.findByEmail(authentication.getName())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        OrderEntity order = orderRepository.findFirstByUserEntityAndId(user, orderId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        OrderItemEntity item = orderItemRepository.findFirstByOrderEntityAndId(order, itemId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

        return ResponseMapper.ToOrderItemResponseMapper(item);
    }

    @Transactional(readOnly = true)
    public List<OrderItemResponse> list(Authentication authentication, String strOrderId) {
        Integer orderId = 0;

        try {
            orderId = Integer.parseInt(strOrderId);       
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad request");
        }

        UserEntity user = userRepository.findByEmail(authentication.getName())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        OrderEntity order = orderRepository.findFirstByUserEntityAndId(user, orderId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
                            
        List<OrderItemEntity> items = orderItemRepository.findAllByOrderEntity(order);

        return ResponseMapper.ToOrderItemResponseListMapper(items);
    }

    @Transactional
    public OrderItemResponse update(Authentication authentication, String strOrderId, String strItemId, UpdateOrderItemRequest request) {
        validationService.validate(request);

        Integer orderId = 0;
        Integer itemId = 0;

        try {
            orderId = Integer.parseInt(strOrderId);
            itemId = Integer.parseInt(strItemId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad request");
        }

        UserEntity user = userRepository.findByEmail(authentication.getName())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        OrderEntity order = orderRepository.findFirstByUserEntityAndId(user, orderId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        OrderItemEntity item = orderItemRepository.findFirstByOrderEntityAndId(order, itemId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

        if (Objects.nonNull(request.getQuantity())) {
            item.setQuantity(request.getQuantity());
        }

        if (Objects.nonNull(request.getSubTotal())) {
            item.setSubTotal(request.getSubTotal());
        }

        orderItemRepository.save(item);

        return ResponseMapper.ToOrderItemResponseMapper(item);
    }

    @Transactional
    public void delete(Authentication authentication, String strOrderId, String strItemId) {
        Integer orderId = 0;
        Integer itemId = 0;

        try {
            orderId = Integer.parseInt(strOrderId);
            itemId = Integer.parseInt(strItemId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad request");
        }

        UserEntity user = userRepository.findByEmail(authentication.getName())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        OrderEntity order = orderRepository.findFirstByUserEntityAndId(user, orderId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        OrderItemEntity item = orderItemRepository.findFirstByOrderEntityAndId(order, itemId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

        try {
            orderItemRepository.delete(item);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Delete item failed");
        } 
    }
}
