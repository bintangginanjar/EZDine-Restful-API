package restful.api.ezdine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemResponse {

    private Integer id;

    private Integer orderId;

    private Integer foodId;  
    
    private String foodName;

    private Integer quantity;

    private Integer subTotal;

}
