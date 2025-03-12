package restful.api.ezdine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {

    private Integer id;

    private String orderId;

    private String date;
        
    private Integer subTotal;
    
    private Double tax;
    
    private Integer totalPrice;

    private String status;

}
