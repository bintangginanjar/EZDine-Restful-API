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
public class UpdateOrderRequest {

    @NotBlank
    @JsonIgnore
    private String id;
    
    private Integer subTotal;
    
    private Double tax;

    private Integer totalPrice;

    private String status;

}
