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
public class RegisterOrderItemRequest {

    @NotBlank
    @JsonIgnore
    private String orderId;

    @NotBlank
    private String foodId;

    @NotNull
    private Integer quantity;

    @NotNull
    private Integer subTotal;

}
