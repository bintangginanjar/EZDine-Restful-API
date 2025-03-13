package restful.api.ezdine.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import restful.api.ezdine.model.OrderItemResponse;
import restful.api.ezdine.model.RegisterOrderItemRequest;
import restful.api.ezdine.model.UpdateOrderItemRequest;
import restful.api.ezdine.model.WebResponse;
import restful.api.ezdine.service.OrderItemService;

@RestController
public class OrderItemController {

    @Autowired
    private OrderItemService orderItemService;

    public OrderItemController(OrderItemService orderItemService) {
        this.orderItemService = orderItemService;
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @PostMapping(
        path = "/api/orders/{orderId}/items",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<OrderItemResponse> register(Authentication authentication,
                                            @PathVariable("orderId") String orderId,                                            
                                            @RequestBody RegisterOrderItemRequest request) {

        request.setOrderId(orderId);

        OrderItemResponse response = orderItemService.register(authentication, orderId, request);

        return WebResponse.<OrderItemResponse>builder()
                                        .status(true)
                                        .messages("Item registration success")
                                        .data(response)
                                        .build();      
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @GetMapping(
        path = "/api/orders/{orderId}/items/{itemId}",                
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<OrderItemResponse> get(Authentication authentication,
                                            @PathVariable("orderId") String orderId,
                                            @PathVariable("itemId") String itemId) {

        OrderItemResponse response = orderItemService.get(authentication, orderId, itemId);

        return WebResponse.<OrderItemResponse>builder()
                                        .status(true)
                                        .messages("Item fetching success")
                                        .data(response)
                                        .build();      
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @GetMapping(
        path = "/api/orders/{orderId}/items",                
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<List<OrderItemResponse>> list(Authentication authentication,
                                            @PathVariable("orderId") String orderId) {

        List<OrderItemResponse> response = orderItemService.list(authentication, orderId);

        return WebResponse.<List<OrderItemResponse>>builder()
                                        .status(true)
                                        .messages("Item listing success")
                                        .data(response)
                                        .build();      
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @PatchMapping(
        path = "/api/orders/{orderId}/items/{itemId}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<OrderItemResponse> register(Authentication authentication,
                                            @PathVariable("orderId") String orderId,
                                            @PathVariable("itemId") String itemId,                                            
                                            @RequestBody UpdateOrderItemRequest request) {

        request.setOrderId(orderId);
        request.setItemId(itemId);

        OrderItemResponse response = orderItemService.update(authentication, orderId, itemId, request);

        return WebResponse.<OrderItemResponse>builder()
                                        .status(true)
                                        .messages("Item update success")
                                        .data(response)
                                        .build();      
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping(
        path = "/api/orders/{orderId}/foods/{itemId}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<String> delete(Authentication authentication,
                                        @PathVariable("categoryId") String categoryId,
                                        @PathVariable("foodId") String foodId) {

        orderItemService.delete(authentication, categoryId, foodId);

        return WebResponse.<String>builder()
                                        .status(true)
                                        .messages("Food delete success")
                                        .build();      
    }
}
