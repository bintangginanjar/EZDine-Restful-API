package restful.api.ezdine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateProfileRequest {

    private String firstname;
    
    private String lastname;

    private String address;
    
    private String phoneNumber;

    private String city;

    private String province;

    private String postalCode;

}
