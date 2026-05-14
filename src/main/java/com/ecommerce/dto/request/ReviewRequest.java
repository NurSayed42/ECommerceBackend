package com.ecommerce.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;
import lombok.Builder;
@Data
@Builder
public class ReviewRequest {
    @NotNull private Long productId;
    @Min(1) @Max(5) private int rating;
    private String title;
    @NotBlank private String comment;
    private List<String> images;
    private String videoUrl;
}
