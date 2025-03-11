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

import restful.api.ezdine.model.FoodResponse;
import restful.api.ezdine.model.RegisterFoodRequest;
import restful.api.ezdine.model.UpdateFoodRequest;
import restful.api.ezdine.model.WebResponse;
import restful.api.ezdine.service.FoodService;

@RestController
public class FoodController {

    @Autowired
    private FoodService foodService;

    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(
        path = "/api/categories/{categoryId}/foods",        
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<FoodResponse> register(Authentication authentication,
                                            @PathVariable("categoryId") String categoryId, 
                                            @RequestBody RegisterFoodRequest request) {

        request.setCategoryId(categoryId);

        FoodResponse response = foodService.register(authentication, categoryId, request);

        return WebResponse.<FoodResponse>builder()
                                        .status(true)
                                        .messages("Food registration success")
                                        .data(response)
                                        .build();      
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @GetMapping(
        path = "/api/categories/{categoryId}/foods/{foodId}",                
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<FoodResponse> get(Authentication authentication,
                                            @PathVariable("categoryId") String categoryId,
                                            @PathVariable("foodId") String foodId) {

        FoodResponse response = foodService.get(authentication, categoryId, foodId);

        return WebResponse.<FoodResponse>builder()
                                        .status(true)
                                        .messages("Food fetching success")
                                        .data(response)
                                        .build();      
    }

    @GetMapping(
        path = "/api/foods",                
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<List<FoodResponse>> list() {

        List<FoodResponse> response = foodService.list();

        return WebResponse.<List<FoodResponse>>builder()
                                        .status(true)
                                        .messages("Food listing success")
                                        .data(response)
                                        .build();      
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping(
        path = "/api/categories/{categoryId}/foods/{foodId}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<FoodResponse> update(Authentication authentication, 
                                            @RequestBody UpdateFoodRequest request,
                                            @PathVariable("categoryId") String categoryId,
                                            @PathVariable("foodId") String foodId) {

        request.setCategoryId(categoryId);
        request.setId(foodId);

        FoodResponse response = foodService.update(authentication, request, categoryId, foodId);

        return WebResponse.<FoodResponse>builder()
                                        .status(true)
                                        .messages("Food update success")
                                        .data(response)
                                        .build(); 

    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping(
        path = "/api/categories/{categoryId}/foods/{foodId}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<String> delete(Authentication authentication,
                                        @PathVariable("categoryId") String categoryId,
                                        @PathVariable("foodId") String foodId) {

        foodService.delete(authentication, categoryId, foodId);

        return WebResponse.<String>builder()
                                        .status(true)
                                        .messages("Food delete success")
                                        .build();      
    }
}
