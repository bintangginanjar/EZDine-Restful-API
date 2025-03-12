package restful.api.ezdine.service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import restful.api.ezdine.entity.OrderEntity;
import restful.api.ezdine.entity.UserEntity;
import restful.api.ezdine.mapper.ResponseMapper;
import restful.api.ezdine.model.OrderResponse;
import restful.api.ezdine.model.RegisterOrderRequest;
import restful.api.ezdine.model.UpdateOrderRequest;
import restful.api.ezdine.repository.OrderRepository;
import restful.api.ezdine.repository.UserRepository;

@Service
public class OrderService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    ValidationService validationService;

    public OrderService(UserRepository userRepository, OrderRepository orderRepository,
            ValidationService validationService) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.validationService = validationService;
    }

    @Transactional
    public OrderResponse register(Authentication authentication, RegisterOrderRequest request) {
        validationService.validate(request);

        UserEntity user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Date date = new Date();

        OrderEntity order = new OrderEntity();
        order.setUserEntity(user);
        order.setOrderId(UUID.randomUUID().toString());
        order.setDate(date.toString());
        order.setSubTotal(request.getSubTotal());
        order.setTax(request.getTax());
        order.setTotalPrice(request.getTotalPrice());
        order.setStatus(request.getStatus());
        orderRepository.save(order);

        return ResponseMapper.ToOrderResponseMapper(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse get(Authentication authentication, String strOrderId) {
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

        return ResponseMapper.ToOrderResponseMapper(order);                    
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> userOrder(Authentication authentication) {        
        UserEntity user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<OrderEntity> orders = orderRepository.findAllByUserEntity(user);

        return ResponseMapper.ToOrderResponseListMapper(orders);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> list() {                
        List<OrderEntity> orders = orderRepository.findAll();

        return ResponseMapper.ToOrderResponseListMapper(orders);
    }


    @Transactional
    public OrderResponse update(Authentication authentication, UpdateOrderRequest request, String strOrderId) {
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

        if (Objects.nonNull(request.getSubTotal())) {
            order.setSubTotal(request.getSubTotal());
        }

        if (Objects.nonNull(request.getTax())) {
            order.setTax(request.getTax());
        }

        if (Objects.nonNull(request.getTotalPrice())) {
            order.setTotalPrice(request.getTotalPrice());
        }

        if (Objects.nonNull(request.getStatus())) {
            order.setStatus(request.getStatus());
        }

        orderRepository.save(order);

        return ResponseMapper.ToOrderResponseMapper(order);
    }
}
