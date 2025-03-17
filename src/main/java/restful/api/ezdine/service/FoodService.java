package restful.api.ezdine.service;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import restful.api.ezdine.entity.CategoryEntity;
import restful.api.ezdine.entity.FoodEntity;
import restful.api.ezdine.entity.UserEntity;
import restful.api.ezdine.mapper.ResponseMapper;
import restful.api.ezdine.model.FoodResponse;
import restful.api.ezdine.model.RegisterFoodRequest;
import restful.api.ezdine.model.UpdateFoodRequest;
import restful.api.ezdine.repository.CategoryRepository;
import restful.api.ezdine.repository.FoodRepository;
import restful.api.ezdine.repository.UserRepository;

@Service
public class FoodService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private ValidationService validationService;

    public FoodService(UserRepository userRepository, CategoryRepository categoryRepository,
            FoodRepository foodRepository, ValidationService validationService) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.foodRepository = foodRepository;
        this.validationService = validationService;
    }

    @Transactional
    public FoodResponse register(Authentication authentication, String strCategoryId, RegisterFoodRequest request) {
        validationService.validate(request);        

        Integer categoryId = 0;

        try {
            categoryId = Integer.parseInt(strCategoryId);       
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad request");
        }

        UserEntity user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        CategoryEntity category = categoryRepository.findFirstByUserEntityAndId(user, categoryId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        if (foodRepository.findByName(request.getName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Food already registered");
        }

        FoodEntity food = new FoodEntity();
        food.setCode(request.getCode());
        food.setName(request.getName());
        food.setDescription(request.getDescription());
        food.setPrice(request.getPrice());
        food.setStock(request.getStock());
        food.setPhotoUrl(request.getPhotoUrl());
        food.setCategoryEntity(category);

        foodRepository.save(food);

        return ResponseMapper.ToFoodResponseMapper(food);

    }

    @Transactional(readOnly = true)
    public FoodResponse get(Authentication authentication, String strCategoryId, String strFoodId) {
        Integer categoryId = 0;
        Integer foodId = 0;

        try {
            categoryId = Integer.parseInt(strCategoryId);       
            foodId = Integer.parseInt(strFoodId);       
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad request");
        }

        UserEntity user = userRepository.findByEmail(authentication.getName())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        CategoryEntity category = categoryRepository.findFirstByUserEntityAndId(user, categoryId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        FoodEntity food = foodRepository.findFirstByCategoryEntityAndId(category, foodId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Food not found"));

        return ResponseMapper.ToFoodResponseMapper(food);
    }

    @Transactional(readOnly = true)
    public List<FoodResponse> list() {
        List<FoodEntity> foods = foodRepository.findAll();

        return ResponseMapper.ToFoodResponseListMapper(foods);
    }

    @Transactional
    public FoodResponse update(Authentication authentication, 
                                UpdateFoodRequest request, 
                                String strCategoryId, String strFoodId) {

        Integer categoryId = 0;
        Integer newCategoryId = 0;
        Integer foodId = 0;

        try {
            categoryId = Integer.parseInt(strCategoryId);       
            foodId = Integer.parseInt(strFoodId);
            newCategoryId = Integer.parseInt(request.getNewCategoryId());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad request");
        }

        UserEntity user = userRepository.findByEmail(authentication.getName())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        CategoryEntity category = categoryRepository.findFirstByUserEntityAndId(user, categoryId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        FoodEntity food = foodRepository.findFirstByCategoryEntityAndId(category, foodId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Food not found"));

        if (Objects.nonNull(request.getNewCategoryId())) {
            CategoryEntity newCategory = categoryRepository.findFirstByUserEntityAndId(user, newCategoryId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

            food.setCategoryEntity(newCategory);
        }

        if (Objects.nonNull(request.getCode())) {
            food.setCode(request.getCode());
        }

        if (Objects.nonNull(request.getName())) {
            food.setName(request.getName());
        }

        if (Objects.nonNull(request.getDescription())) {
            food.setDescription(request.getDescription());
        }

        if (Objects.nonNull(request.getPhotoUrl())) {
            food.setPhotoUrl(request.getPhotoUrl());
        }

        if (Objects.nonNull(request.getPrice())) {
            food.setPrice(request.getPrice());
        }

        if (Objects.nonNull(request.getStock())) {
            food.setStock(request.getStock());
        }

        foodRepository.save(food);

        return ResponseMapper.ToFoodResponseMapper(food);

    }

    @Transactional
    public void delete(Authentication authentication, String strCategoryId, String strFoodId) {
        Integer categoryId = 0;
        Integer foodId = 0;

        try {
            categoryId = Integer.parseInt(strCategoryId);       
            foodId = Integer.parseInt(strFoodId);       
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad request");
        }

        UserEntity user = userRepository.findByEmail(authentication.getName())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        CategoryEntity category = categoryRepository.findFirstByUserEntityAndId(user, categoryId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        FoodEntity food = foodRepository.findFirstByCategoryEntityAndId(category, foodId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Food not found"));

        try {
            foodRepository.delete(food);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Delete food failed");
        } 
    }

}
