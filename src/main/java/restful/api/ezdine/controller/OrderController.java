package restful.api.ezdine.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import restful.api.ezdine.model.OrderResponse;
import restful.api.ezdine.model.RegisterOrderRequest;
import restful.api.ezdine.model.UpdateOrderRequest;
import restful.api.ezdine.model.WebResponse;
import restful.api.ezdine.service.OrderService;

@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @PostMapping(
        path = "/api/orders",        
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<OrderResponse> register(Authentication authentication, 
                                            @RequestBody RegisterOrderRequest request) {

        OrderResponse response = orderService.register(authentication, request);

        return WebResponse.<OrderResponse>builder()
                                        .status(true)
                                        .messages("Category registration success")
                                        .data(response)
                                        .build();      
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @GetMapping(
        path = "/api/orders/{orderId}",                
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<OrderResponse> get(Authentication authentication,                                            
                                            @PathVariable("foodId") String orderId) {

        OrderResponse response = orderService.get(authentication, orderId);

        return WebResponse.<OrderResponse>builder()
                                        .status(true)
                                        .messages("Order fetching success")
                                        .data(response)
                                        .build();      
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @GetMapping(
        path = "/api/users/orders",                
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<List<OrderResponse>> userOrder(Authentication authentication) {

        List<OrderResponse> response = orderService.userOrder(authentication);

        return WebResponse.<List<OrderResponse>>builder()
                                        .status(true)
                                        .messages("Order listing success")
                                        .data(response)
                                        .build();      
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(
        path = "/api/orders",                
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<List<OrderResponse>> list() {

        List<OrderResponse> response = orderService.list();

        return WebResponse.<List<OrderResponse>>builder()
                                        .status(true)
                                        .messages("Order listing success")
                                        .data(response)
                                        .build();      
    }
    
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @PatchMapping(
        path = "/api/orders/{orderId}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<OrderResponse> update(Authentication authentication, 
                                            @RequestBody UpdateOrderRequest request,                                            
                                            @PathVariable("orderId") String orderId) {
        
        request.setId(orderId);

        OrderResponse response = orderService.update(authentication, request, orderId);

        return WebResponse.<OrderResponse>builder()
                                        .status(true)
                                        .messages("Order update success")
                                        .data(response)
                                        .build(); 

    }

}
