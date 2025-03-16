package restful.api.ezdine.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterFoodRequest {

    @NotBlank
    @JsonIgnore
    private String categoryId;

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    @NotBlank
    private String description;
    
    @NotNull
    private Double price;

    @NotNull
    private Integer stock;

    @NotNull
    private String photoUrl;

}
