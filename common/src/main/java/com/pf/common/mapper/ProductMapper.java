package com.pf.common.mapper;

import com.pf.common.dto.ProductDTO;
import com.pf.common.entity.Product;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    Product toProduct(ProductDTO productDTO);

    ProductDTO toProductDTO(Product product);

    List<ProductDTO> toProductDTO(List<Product> products);

}
