package com.yas.system.catalog;

import com.yas.system.catalog.api.dto.ProductVariantPublicDto;
import com.yas.system.catalog.api.enumeration.ProductStatus;
import com.yas.system.catalog.internal.entity.Product;
import com.yas.system.catalog.internal.entity.variant.ProductVariant;
import com.yas.system.catalog.internal.repository.ProductMediaRepository;
import com.yas.system.catalog.internal.repository.ProductVariantRepository;
import com.yas.system.catalog.internal.repository.VariantOptionValueRepository;
import com.yas.system.catalog.service.ProductService;
import com.yas.system.catalog.service.impl.ProductServiceImpl;
import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.media.service.MediaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductVariantService Tests")
class ProductVariantServiceTest {

    @Mock
    ProductVariantRepository productVariantRepository;

    @Mock
    ProductMediaRepository productMediaRepository;

    @Mock
    VariantOptionValueRepository variantOptionValueRepository;

    @Mock
    MediaService mediaService;

    @InjectMocks
    ProductServiceImpl productService;

    Product activeProduct;
    Product inactiveProduct;
    ProductVariant activeVariant;
    ProductVariant inactiveVariant;

    @BeforeEach
    void setUp() {
        activeProduct = Product.builder()
                .name("MacBook Pro")
                .slug("macbook-pro")
                .status(ProductStatus.ACTIVE)
                .build();
        activeProduct.setId(10L);

        inactiveProduct = Product.builder()
                .name("Old Laptop")
                .slug("old-laptop")
                .status(ProductStatus.INACTIVE)
                .build();
        inactiveProduct.setId(20L);

        activeVariant = ProductVariant.builder()
                .sku("MBP-M3")
                .price(BigDecimal.valueOf(1999.00))
                .quantity(10)
                .mediaId("media-123")
                .product(activeProduct)
                .build();
        activeVariant.setId(100L);

        inactiveVariant = ProductVariant.builder()
                .sku("OLD-LAP")
                .price(BigDecimal.valueOf(499.00))
                .quantity(5)
                .product(inactiveProduct)
                .build();
        inactiveVariant.setId(200L);
    }

    @Test
    @DisplayName("getProductVariantById should return variant DTO when product is ACTIVE")
    void getProductVariantById_active_success() {
        when(productVariantRepository.findByIdWithProduct(100L)).thenReturn(Optional.of(activeVariant));
        when(variantOptionValueRepository.findByProductVariantIdInWithDetails(List.of(100L))).thenReturn(List.of());
        when(mediaService.getMediaUrl("media-123")).thenReturn("http://media/123.jpg");

        ProductVariantPublicDto result = productService.getProductVariantById(100L);

        assertThat(result).isNotNull();
        assertThat(result.variantId()).isEqualTo(100L);
        assertThat(result.productId()).isEqualTo(10L);
        assertThat(result.productName()).isEqualTo("MacBook Pro");
        assertThat(result.productSlug()).isEqualTo("macbook-pro");
        assertThat(result.thumbnailUrl()).isEqualTo("http://media/123.jpg");
        assertThat(result.sku()).isEqualTo("MBP-M3");
        assertThat(result.price()).isEqualByComparingTo(BigDecimal.valueOf(1999.00));
    }

    @Test
    @DisplayName("getProductVariantById should throw PRODUCT_INACTIVE when product is not ACTIVE")
    void getProductVariantById_inactive_throwsException() {
        when(productVariantRepository.findByIdWithProduct(200L)).thenReturn(Optional.of(inactiveVariant));

        assertThatThrownBy(() -> productService.getProductVariantById(200L))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_INACTIVE);
                });
    }

    @Test
    @DisplayName("getProductVariantById should throw PRODUCT_VARIANT_NOT_FOUND when variant not found")
    void getProductVariantById_notFound_throwsException() {
        when(productVariantRepository.findByIdWithProduct(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductVariantById(999L))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_VARIANT_NOT_FOUND);
                });
    }

    @Test
    @DisplayName("getProductVariantsByIds should return variant map using batch query")
    void getProductVariantsByIds_active_success() {
        when(productVariantRepository.findByIdInWithProduct(List.of(100L))).thenReturn(List.of(activeVariant));
        when(mediaService.getMediaUrls(any())).thenReturn(Map.of("media-123", "http://media/123.jpg"));
        when(variantOptionValueRepository.findByProductVariantIdInWithDetails(List.of(100L))).thenReturn(List.of());

        Map<Long, ProductVariantPublicDto> result = productService.getProductVariantsByIds(List.of(100L));

        assertThat(result).hasSize(1);
        assertThat(result).containsKey(100L);
        assertThat(result.get(100L).productName()).isEqualTo("MacBook Pro");
    }

    @Test
    @DisplayName("getProductVariantsByIds should throw PRODUCT_INACTIVE when any variant product is inactive")
    void getProductVariantsByIds_containsInactive_throwsException() {
        when(productVariantRepository.findByIdInWithProduct(List.of(100L, 200L)))
                .thenReturn(List.of(activeVariant, inactiveVariant));

        assertThatThrownBy(() -> productService.getProductVariantsByIds(List.of(100L, 200L)))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_INACTIVE);
                });
    }
}
