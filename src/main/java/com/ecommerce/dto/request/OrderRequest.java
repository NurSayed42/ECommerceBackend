package com.ecommerce.dto.request;
import com.ecommerce.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    private Long addressId;          // for logged-in users
    private GuestInfo guestInfo;     // for guests
    @NotNull private PaymentMethod paymentMethod;
    private String couponCode;
    private String orderNotes;
    private String deliverySlot;
    private List<Long> cartItemIds;

    @Data
    public static class GuestInfo {
        private String fullName;
        private String email;
        private String phone;
        private String streetAddress;
        private String city;
        private String district;
        private String postalCode;
    }
}
