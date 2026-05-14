// package com.ecommerce.service.impl;

// import com.ecommerce.dto.request.ProductRequest;
// import com.ecommerce.dto.response.PageResponse;
// import com.ecommerce.dto.response.ProductResponse;
// import com.ecommerce.exception.BadRequestException;
// import com.ecommerce.exception.ResourceNotFoundException;
// import com.ecommerce.model.*;
// import com.ecommerce.repository.*;
// import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import lombok.RequiredArgsConstructor;
// import org.springframework.cache.annotation.CacheEvict;
// import org.springframework.cache.annotation.Cacheable;
// import org.springframework.data.domain.*;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;
// import java.math.BigDecimal;
// import java.math.RoundingMode;
// import java.text.Normalizer;
// import java.util.*;
// import java.util.stream.Collectors;

// @Service
// @RequiredArgsConstructor
// public class ProductService {

//     private final ProductRepository productRepository;
//     private final CategoryRepository categoryRepository;
//     private final ObjectMapper objectMapper;

//     @Cacheable(value = "products", key = "#id")
//     public ProductResponse getById(Long id) {
//         Product p = productRepository.findById(id)
//                 .orElseThrow(() -> new ResourceNotFoundException("Product", id));
//         productRepository.incrementViewCount(id);
//         return toResponse(p);
//     }

//     @Cacheable(value = "products", key = "'slug_'+#slug")
//     public ProductResponse getBySlug(String slug) {
//         Product p = productRepository.findBySlug(slug)
//                 .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + slug));
//         return toResponse(p);
//     }

//     public PageResponse<ProductResponse> search(String q, Long categoryId,
//             BigDecimal minPrice, BigDecimal maxPrice,
//             String brand, Integer minRating,
//             Boolean inStock, String sort, int page, int size) {

//         Sort sortObj = switch (sort != null ? sort : "newest") {
//             case "price_asc"  -> Sort.by("price").ascending();
//             case "price_desc" -> Sort.by("price").descending();
//             case "popular"    -> Sort.by("soldCount").descending();
//             case "rating"     -> Sort.by("avgRating").descending();
//             default           -> Sort.by("createdAt").descending();
//         };
//         Pageable pageable = PageRequest.of(page, size, sortObj);

//         Page<Product> products;
//         if (q != null && !q.isBlank()) {
//             products = productRepository.searchProducts(q, pageable);
//         } else if (categoryId != null) {
//             products = productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable);
//         } else {
//             products = productRepository.findByActiveTrue(pageable);
//         }

//         // Apply price/rating/stock filters in memory (ideally use Specifications)
//         List<Product> filtered = products.getContent().stream()
//                 .filter(p -> minPrice == null || p.getPrice().compareTo(minPrice) >= 0)
//                 .filter(p -> maxPrice == null || p.getPrice().compareTo(maxPrice) <= 0)
//                 .filter(p -> brand == null || brand.equalsIgnoreCase(p.getBrand()))
//                 .filter(p -> minRating == null || p.getAvgRating().intValue() >= minRating)
//                 .filter(p -> inStock == null || !inStock || p.getStock() > 0)
//                 .collect(Collectors.toList());

//         Page<ProductResponse> responsePage = new PageImpl<>(
//                 filtered.stream().map(this::toResponse).collect(Collectors.toList()),
//                 pageable, products.getTotalElements());

//         return PageResponse.of(responsePage);
//     }

//     @Transactional
//     @CacheEvict(value = "products", allEntries = true)
//     public ProductResponse create(ProductRequest req) {
//         if (req.getSku() != null && productRepository.existsBySku(req.getSku()))
//             throw new BadRequestException("SKU already exists: " + req.getSku());

//         Category category = categoryRepository.findById(req.getCategoryId())
//                 .orElseThrow(() -> new ResourceNotFoundException("Category", req.getCategoryId()));

//         Product p = new Product();
//         mapRequestToProduct(req, p, category);
//         p.setSlug(generateSlug(req.getName()));

//         if (req.getPrice() != null && req.getSalePrice() != null) {
//             BigDecimal discount = req.getPrice().subtract(req.getSalePrice())
//                     .divide(req.getPrice(), 4, RoundingMode.HALF_UP)
//                     .multiply(BigDecimal.valueOf(100));
//             p.setDiscountPercent(discount.setScale(2, RoundingMode.HALF_UP));
//         }

//         // Build variants
//         if (req.getVariants() != null) {
//             List<ProductVariant> variants = req.getVariants().stream().map(vr -> {
//                 ProductVariant v = new ProductVariant();
//                 v.setProduct(p);
//                 v.setSize(vr.getSize());
//                 v.setColor(vr.getColor());
//                 v.setColorCode(vr.getColorCode());
//                 v.setSku(vr.getSku());
//                 v.setAdditionalPrice(vr.getAdditionalPrice());
//                 v.setStock(vr.getStock());
//                 v.setImage(vr.getImage());
//                 return v;
//             }).collect(Collectors.toList());
//             p.setVariants(variants);
//         }

//         return toResponse(productRepository.save(p));
//     }

//     @Transactional
//     @CacheEvict(value = "products", allEntries = true)
//     public ProductResponse update(Long id, ProductRequest req) {
//         Product p = productRepository.findById(id)
//                 .orElseThrow(() -> new ResourceNotFoundException("Product", id));
//         Category category = categoryRepository.findById(req.getCategoryId())
//                 .orElseThrow(() -> new ResourceNotFoundException("Category", req.getCategoryId()));
//         mapRequestToProduct(req, p, category);
//         return toResponse(productRepository.save(p));
//     }

//     @Transactional
//     @CacheEvict(value = "products", allEntries = true)
//     public void delete(Long id) {
//         Product p = productRepository.findById(id)
//                 .orElseThrow(() -> new ResourceNotFoundException("Product", id));
//         p.setActive(false);
//         productRepository.save(p);
//     }

//     public List<ProductResponse> getFeatured() {
//         return productRepository.findByFeaturedTrueAndActiveTrue()
//                 .stream().map(this::toResponse).collect(Collectors.toList());
//     }

//     public List<ProductResponse> getFlashSale() {
//         return productRepository.findByFlashSaleTrueAndActiveTrue()
//                 .stream().map(this::toResponse).collect(Collectors.toList());
//     }

//     public List<ProductResponse> getTrending() {
//         return productRepository.findByTrendingTrueAndActiveTrue()
//                 .stream().map(this::toResponse).collect(Collectors.toList());
//     }

//     public List<ProductResponse> getTopSelling(int limit) {
//         return productRepository.findTopSellingProducts(PageRequest.of(0, limit))
//                 .stream().map(this::toResponse).collect(Collectors.toList());
//     }

//     private void mapRequestToProduct(ProductRequest req, Product p, Category category) {
//         p.setName(req.getName());
//         p.setDescription(req.getDescription());
//         p.setShortDescription(req.getShortDescription());
//         p.setPrice(req.getPrice());
//         p.setSalePrice(req.getSalePrice());
//         p.setCategory(category);
//         p.setBrand(req.getBrand());
//         p.setSku(req.getSku());
//         p.setStock(req.getStock());
//         p.setLowStockThreshold(req.getLowStockThreshold());
//         p.setFeatured(req.isFeatured());
//         p.setFlashSale(req.isFlashSale());
//         p.setTrending(req.isTrending());
//         p.setDeliveryInfo(req.getDeliveryInfo());
//         p.setReturnPolicy(req.getReturnPolicy());
//         p.setWeight(req.getWeight());
//         p.setDimensions(req.getDimensions());
//         p.setVideoUrl(req.getVideoUrl());
//         p.setTags(req.getTags());
//         p.setMetaTitle(req.getMetaTitle());
//         p.setMetaDescription(req.getMetaDescription());
//         if (req.getImages() != null) {
//             try { p.setImages(objectMapper.writeValueAsString(req.getImages())); }
//             catch (JsonProcessingException e) { p.setImages("[]"); }
//         }
//     }

//     public ProductResponse toResponse(Product p) {
//         List<String> images = new ArrayList<>();
//         if (p.getImages() != null) {
//             try { images = objectMapper.readValue(p.getImages(), List.class); }
//             catch (Exception ignored) {}
//         }
//         List<ProductResponse.VariantResponse> variants = p.getVariants() == null ? List.of() :
//                 p.getVariants().stream().filter(ProductVariant::isActive).map(v ->
//                     ProductResponse.VariantResponse.builder()
//                         .id(v.getId()).size(v.getSize()).color(v.getColor())
//                         .colorCode(v.getColorCode()).sku(v.getSku())
//                         .additionalPrice(v.getAdditionalPrice()).stock(v.getStock())
//                         .image(v.getImage()).build()
//                 ).collect(Collectors.toList());

//         return ProductResponse.builder()
//                 .id(p.getId()).name(p.getName()).slug(p.getSlug())
//                 .description(p.getDescription()).shortDescription(p.getShortDescription())
//                 .price(p.getPrice()).salePrice(p.getSalePrice()).discountPercent(p.getDiscountPercent())
//                 .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
//                 .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
//                 .brand(p.getBrand()).sku(p.getSku()).stock(p.getStock()).inStock(p.getStock() > 0)
//                 .featured(p.isFeatured()).flashSale(p.isFlashSale()).trending(p.isTrending())
//                 .avgRating(p.getAvgRating()).reviewCount(p.getReviewCount()).soldCount(p.getSoldCount())
//                 .images(images).videoUrl(p.getVideoUrl())
//                 .deliveryInfo(p.getDeliveryInfo()).returnPolicy(p.getReturnPolicy())
//                 .tags(p.getTags()).variants(variants).createdAt(p.getCreatedAt())
//                 .build();
//     }

//     private String generateSlug(String name) {
//         String base = Normalizer.normalize(name, Normalizer.Form.NFD)
//                 .replaceAll("[^\\p{ASCII}]", "")
//                 .toLowerCase().trim()
//                 .replaceAll("[^a-z0-9\\s-]", "")
//                 .replaceAll("\\s+", "-")
//                 .replaceAll("-+", "-");
//         String slug = base;
//         int counter = 1;
//         while (productRepository.existsBySlug(slug)) {
//             slug = base + "-" + counter++;
//         }
//         return slug;
//     }
// }























package com.ecommerce.service.impl;

import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.dto.response.PageResponse;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.*;
import com.ecommerce.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ObjectMapper objectMapper;

    @Cacheable(value = "products", key = "#id")
    public ProductResponse getById(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        productRepository.incrementViewCount(id);
        return toResponse(p);
    }

    @Cacheable(value = "products", key = "'slug_'+#slug")
    public ProductResponse getBySlug(String slug) {
        Product p = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + slug));
        return toResponse(p);
    }

    public PageResponse<ProductResponse> search(String q, Long categoryId,
                                                BigDecimal minPrice, BigDecimal maxPrice,
                                                String brand, Integer minRating,
                                                Boolean inStock, String sort, int page, int size) {

        Sort sortObj = switch (sort != null ? sort : "newest") {
            case "price_asc"  -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "popular"    -> Sort.by("soldCount").descending();
            case "rating"     -> Sort.by("avgRating").descending();
            default           -> Sort.by("createdAt").descending();
        };
        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<Product> products;
        if (q != null && !q.isBlank()) {
            products = productRepository.searchProducts(q, pageable);
        } else if (categoryId != null) {
            products = productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable);
        } else {
            products = productRepository.findByActiveTrue(pageable);
        }

        List<Product> filtered = products.getContent().stream()
                .filter(p -> minPrice == null || p.getPrice().compareTo(minPrice) >= 0)
                .filter(p -> maxPrice == null || p.getPrice().compareTo(maxPrice) <= 0)
                .filter(p -> brand == null || brand.equalsIgnoreCase(p.getBrand()))
                .filter(p -> minRating == null || p.getAvgRating().intValue() >= minRating)
                .filter(p -> inStock == null || !inStock || p.getStock() > 0)
                .collect(Collectors.toList());

        Page<ProductResponse> responsePage = new PageImpl<>(
                filtered.stream().map(this::toResponse).collect(Collectors.toList()),
                pageable, products.getTotalElements());

        return PageResponse.of(responsePage);
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse create(ProductRequest req) {
        if (req.getSku() != null && productRepository.existsBySku(req.getSku()))
            throw new BadRequestException("SKU already exists: " + req.getSku());

        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", req.getCategoryId()));

        Product p = new Product();
        mapRequestToProduct(req, p, category);
        p.setSlug(generateSlug(req.getName()));

        if (req.getPrice() != null && req.getSalePrice() != null) {
            BigDecimal discount = req.getPrice().subtract(req.getSalePrice())
                    .divide(req.getPrice(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            p.setDiscountPercent(discount.setScale(2, RoundingMode.HALF_UP));
        }

        if (req.getVariants() != null) {
            List<ProductVariant> variants = req.getVariants().stream().map(vr -> {
                ProductVariant v = new ProductVariant();
                v.setProduct(p);
                v.setSize(vr.getSize());
                v.setColor(vr.getColor());
                v.setColorCode(vr.getColorCode());
                v.setSku(vr.getSku());
                v.setAdditionalPrice(vr.getAdditionalPrice());
                v.setStock(vr.getStock());
                v.setImage(vr.getImage());
                return v;
            }).collect(Collectors.toList());
            p.setVariants(variants);
        }

        return toResponse(productRepository.save(p));
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse update(Long id, ProductRequest req) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", req.getCategoryId()));
        mapRequestToProduct(req, p, category);
        return toResponse(productRepository.save(p));
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void delete(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        p.setActive(false);
        productRepository.save(p);
    }

    public List<ProductResponse> getFeatured() {
        return productRepository.findByFeaturedTrueAndActiveTrue()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<ProductResponse> getFlashSale() {
        return productRepository.findByFlashSaleTrueAndActiveTrue()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<ProductResponse> getTrending() {
        return productRepository.findByTrendingTrueAndActiveTrue()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<ProductResponse> getTopSelling(int limit) {
        return productRepository.findTopSellingProducts(PageRequest.of(0, limit))
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private void mapRequestToProduct(ProductRequest req, Product p, Category category) {
        p.setName(req.getName());
        p.setDescription(req.getDescription());
        p.setShortDescription(req.getShortDescription());
        p.setPrice(req.getPrice());
        p.setSalePrice(req.getSalePrice());
        p.setCategory(category);
        p.setBrand(req.getBrand());
        p.setSku(req.getSku());
        p.setStock(req.getStock());
        p.setLowStockThreshold(req.getLowStockThreshold());
        p.setFeatured(req.isFeatured());
        p.setFlashSale(req.isFlashSale());
        p.setTrending(req.isTrending());
        p.setDeliveryInfo(req.getDeliveryInfo());
        p.setReturnPolicy(req.getReturnPolicy());
        p.setWeight(req.getWeight());
        p.setDimensions(req.getDimensions());
        p.setVideoUrl(req.getVideoUrl());
        p.setTags(req.getTags());
        p.setMetaTitle(req.getMetaTitle());
        p.setMetaDescription(req.getMetaDescription());

        // images সবসময় handle করো
        if (req.getImages() != null && !req.getImages().isEmpty()) {
            try {
                p.setImages(objectMapper.writeValueAsString(req.getImages()));
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize images for product: {}", req.getName(), e);
                p.setImages("[]");
            }
        } else {
            if (p.getImages() == null) {
                p.setImages("[]");
            }
            // update হলে existing images রাখো — null set করো না
        }
    }

    // ✅ DB-তে যেকোনো format হোক — single string বা JSON array — দুটোই handle করে
    public ProductResponse toResponse(Product p) {
        List<String> images = new ArrayList<>();

        if (p.getImages() != null && !p.getImages().isBlank()) {
            String raw = p.getImages().trim();

            if (raw.startsWith("[")) {
                // JSON array format: ["url1", "url2"]
                try {
                    images = objectMapper.readValue(raw, new TypeReference<List<String>>() {});
                } catch (Exception e) {
                    log.warn("Failed to parse images JSON array for product id={}, raw={}",
                            p.getId(), raw);
                }
            } else if (raw.startsWith("\"") && raw.endsWith("\"")) {
                // JSON quoted string: "https://..."
                try {
                    String url = objectMapper.readValue(raw, String.class);
                    if (!url.isBlank()) images.add(url);
                } catch (Exception e) {
                    log.warn("Failed to parse images JSON string for product id={}", p.getId());
                }
            } else {
                // Plain string: https://...
                images.add(raw);
            }
        }

        List<ProductResponse.VariantResponse> variants = p.getVariants() == null ? List.of() :
                p.getVariants().stream().filter(ProductVariant::isActive).map(v ->
                        ProductResponse.VariantResponse.builder()
                                .id(v.getId()).size(v.getSize()).color(v.getColor())
                                .colorCode(v.getColorCode()).sku(v.getSku())
                                .additionalPrice(v.getAdditionalPrice()).stock(v.getStock())
                                .image(v.getImage()).build()
                ).collect(Collectors.toList());

        return ProductResponse.builder()
                .id(p.getId()).name(p.getName()).slug(p.getSlug())
                .description(p.getDescription()).shortDescription(p.getShortDescription())
                .price(p.getPrice()).salePrice(p.getSalePrice()).discountPercent(p.getDiscountPercent())
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                .brand(p.getBrand()).sku(p.getSku()).stock(p.getStock()).inStock(p.getStock() > 0)
                .featured(p.isFeatured()).flashSale(p.isFlashSale()).trending(p.isTrending())
                .avgRating(p.getAvgRating()).reviewCount(p.getReviewCount()).soldCount(p.getSoldCount())
                .images(images).videoUrl(p.getVideoUrl())
                .deliveryInfo(p.getDeliveryInfo()).returnPolicy(p.getReturnPolicy())
                .tags(p.getTags()).variants(variants).createdAt(p.getCreatedAt())
                .build();
    }

    private String generateSlug(String name) {
        String base = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .toLowerCase().trim()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
        String slug = base;
        int counter = 1;
        while (productRepository.existsBySlug(slug)) {
            slug = base + "-" + counter++;
        }
        return slug;
    }
}
