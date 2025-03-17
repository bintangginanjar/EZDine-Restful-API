package restful.api.ezdine.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateFoodRequest {

    @NotBlank
    @JsonIgnore
    private String categoryId;

    @NotBlank
    @JsonIgnore
    private String id;

    private String code;

    private String newCategoryId;

    private String name;

    private String description;
    
    private Double price;

    private Integer stock;

    private String photoUrl;

}
