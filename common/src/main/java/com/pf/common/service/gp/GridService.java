package com.pf.common.service.gp;

import com.pf.common.dto.gp.*;
import com.pf.common.dto.ProductDTO;
import com.pf.common.entity.Product;
import com.pf.common.mapper.ProductMapper;
import com.pf.common.service.criteria.GenericCriteriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GridService {
    private final ProductMapper productMapper;
    private final GenericCriteriaRepository genericCriteriaRepository;

    public GridResult getProducts(SearchCriteria searchCriteria) {
        long totalRecords = genericCriteriaRepository.getCountBySearchCriteria(Product.class, searchCriteria);
        List<Product> products = genericCriteriaRepository.getDataBySearchCriteria(Product.class, searchCriteria);
        List<ProductDTO> productDTOS = productMapper.toProductDTO(products);
        return GridResult.builder().totalRecords(totalRecords).recordDetails(productDTOS).build();
    }

    public List<GridColumnDTO> getGridColumns() {
        List<GridColumnDTO> list = new ArrayList<>();
        GridColumnDTO id = new GridColumnDTO("id", "ID", "number", false, null, false, "equals", "80px", "right", false, 0, null);
        GridColumnDTO name = new GridColumnDTO("name", "Name", "text", true, "asc", true, "contains", null, null, true, 1, null);
        GridColumnDTO category = new GridColumnDTO("category", "Category", "text", true, null, true, "contains", null, null, false, 2, null);
        GridColumnDTO price = new GridColumnDTO("price", "Price (₹)", "number", true, null, true, "equals", null, "right", true, 3, "currencyCellTemplate");
        GridColumnDTO quantity = new GridColumnDTO("quantity", "Quantity", "number", true, null, true, "equals", null, "right", true, 4, null);
        GridColumnDTO active = new GridColumnDTO("active", "Status", "boolean", true, null, true, "equals", null, "right", true, 5, "cellValueTemplate");
        GridColumnDTO createdDate = new GridColumnDTO("createdDate", "Created Date", "date", true, null, true, "equals", null, "right", true, 5, null);
        list.add(id);
        list.add(name);
        list.add(category);
        list.add(price);
        list.add(quantity);
        list.add(active);
        list.add(createdDate);
        return list;
    }

}
