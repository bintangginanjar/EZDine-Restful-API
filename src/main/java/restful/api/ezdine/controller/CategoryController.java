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

import restful.api.ezdine.model.CategoryResponse;
import restful.api.ezdine.model.RegisterCategoryRequest;
import restful.api.ezdine.model.UpdateCategoryRequest;
import restful.api.ezdine.model.WebResponse;
import restful.api.ezdine.service.CategoryService;

@RestController
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(
        path = "/api/categories",        
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<CategoryResponse> register(Authentication authentication, 
                                            @RequestBody RegisterCategoryRequest request) {

        CategoryResponse response = categoryService.register(authentication, request);

        return WebResponse.<CategoryResponse>builder()
                                        .status(true)
                                        .messages("Category registration success")
                                        .data(response)
                                        .build();      
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @GetMapping(
        path = "/api/categories/{categoryId}",                
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<CategoryResponse> get(Authentication authentication,
                                            @PathVariable("categoryId") String categoryId) {

        CategoryResponse response = categoryService.get(authentication, categoryId);

        return WebResponse.<CategoryResponse>builder()
                                        .status(true)
                                        .messages("Category fetching success")
                                        .data(response)
                                        .build();      
    }
    
    @GetMapping(
        path = "/api/categories",                
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<List<CategoryResponse>> list() {

        List<CategoryResponse> response = categoryService.list();

        return WebResponse.<List<CategoryResponse>>builder()
                                        .status(true)
                                        .messages("Category listing success")
                                        .data(response)
                                        .build();      
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping(
        path = "/api/categories/{categoryId}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<CategoryResponse> update(Authentication authentication, 
                                            @RequestBody UpdateCategoryRequest request,
                                            @PathVariable("categoryId") String categoryId) {

        request.setId(categoryId);

        CategoryResponse response = categoryService.update(authentication, request, categoryId);

        return WebResponse.<CategoryResponse>builder()
                                        .status(true)
                                        .messages("Service update success")
                                        .data(response)
                                        .build();      
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping(
        path = "/api/categories/{categoryId}",                
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<String> delete(Authentication authentication,
                                            @PathVariable("categoryId") String categoryId) {

        categoryService.delete(authentication, categoryId);

        return WebResponse.<String>builder()
                                        .status(true)
                                        .messages("Category delete success")                                        
                                        .build();      
    }
}
