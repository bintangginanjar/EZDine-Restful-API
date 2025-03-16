package restful.api.ezdine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodResponse {

    private Integer id;    

    private Integer categoryId;

    private String code;

    private String name;

    private String description;    
    
    private Double price;

    private Integer stock;

    private String photoUrl;

}
