package restful.api.ezdine.model;

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
public class RegisterOrderRequest {

    @NotNull
    private Integer subTotal;

    @NotNull
    private Double tax;

    @NotNull
    private Integer totalPrice;

    @NotBlank
    private String status;

}
