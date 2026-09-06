package com.pf.common.service.gp;

import com.pf.common.dto.gp.*;
import com.pf.common.dto.ProductDTO;
import com.pf.common.entity.Product;
import com.pf.common.mapper.ProductMapper;
import com.pf.common.mapper.gp.GridColumnMapper;
import com.pf.common.repository.GridColumnRepository;
import com.pf.common.service.criteria.GenericCriteriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GridService {
    private final ProductMapper productMapper;
    private final GenericCriteriaRepository genericCriteriaRepository;
    private final GridColumnRepository gridColumnRepository;
    private final GridColumnMapper gridColumnMapper;

    public GridResult getProducts(SearchCriteria searchCriteria) {
        searchCriteria.getSortList().add(GridSort.builder().field("id").order("asc").build());
        long totalRecords = genericCriteriaRepository.getCountBySearchCriteria(Product.class, searchCriteria);
        List<Product> products = genericCriteriaRepository.getDataBySearchCriteria(Product.class, searchCriteria);
        List<ProductDTO> productDTOS = productMapper.toProductDTO(products);
        return GridResult.builder().totalRecords(totalRecords).recordDetails(productDTOS).build();
    }

    public List<GridColumnDto> getGridColumns(String gridName) {
        try {
            List<GridColumnDto> list = gridColumnMapper.toDTOList(gridColumnRepository.findByGridName_NameOrderByVisibleIndex(gridName));
            if (list.isEmpty()) {
                log.debug("No GridColumn entries found for gridName: {}", gridName);
                return Collections.emptyList();
            }
            return list;
        } catch (DataAccessException dae) {
            log.error("Database error while fetching GridColumn entries for gridName: {}", gridName, dae);
            throw dae; // propagate or wrap in custom exception
        } catch (Exception e) {
            log.error("Unexpected error while fetching GridColumn entries for gridName: {}", gridName, e);
            throw e; // don’t silently swallow
        }
    }

}
