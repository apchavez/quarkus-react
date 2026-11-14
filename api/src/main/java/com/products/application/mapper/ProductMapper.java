package com.products.application.mapper;

import com.products.application.dto.ProductRequest;
import com.products.application.dto.ProductResponse;
import com.products.domain.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "cdi")
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "userCreated", ignore = true)
    @Mapping(target = "userUpdated", ignore = true)
    Product toEntity(ProductRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "userCreated", ignore = true)
    @Mapping(target = "userUpdated", ignore = true)
    void updateEntity(ProductRequest request, @MappingTarget Product entity);

    @Mapping(target = "id", expression = "java(product.id != null ? product.id.toString() : null)")
    ProductResponse toResponse(Product product);

    List<ProductResponse> toResponseList(List<Product> products);
}