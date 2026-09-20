package com.pf.common.service.financialTransaction;

import com.pf.common.dto.financialTransaction.FinancialTransactionDto;
import com.pf.common.dto.financialTransaction.FinancialTransactionViewDto;
import com.pf.common.dto.gp.GridResult;
import com.pf.common.dto.gp.SearchCriteria;
import com.pf.common.entity.categoryManagement.UserCategory;
import com.pf.common.entity.financialTransaction.FinancialTransaction;
import com.pf.common.entity.financialTransaction.FinancialTransactionView;
import com.pf.common.entity.generic.ApiResponse;
import com.pf.common.entity.userManagement.User;
import com.pf.common.exception.ResourceNotFoundException;
import com.pf.common.mapper.financialTransaction.FinancialTransactionMapper;
import com.pf.common.mapper.financialTransaction.FinancialTransactionViewMapper;
import com.pf.common.repository.financialTransaction.FinancialTransactionRepository;
import com.pf.common.service.generic.BaseService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinancialTransactionService extends BaseService {
    private final FinancialTransactionRepository financialTransactionRepository;
    private final FinancialTransactionMapper financialTransactionMapper;
    private final EntityManager entityManager;
    private final FinancialTransactionViewMapper financialTransactionViewMapper;

    public GridResult fetchExpensesGridData(SearchCriteria searchCriteria) {
        log.debug("fetchExpensesGridData: searchCriteria: {}", searchCriteria);
        long totalRecords = 0;
        if (!searchCriteria.isLoadAllData()) {
            totalRecords = getCountBySearchCriteria(FinancialTransactionView.class, searchCriteria);
        }
        List<FinancialTransactionViewDto> recordDetails = financialTransactionViewMapper.toDtoList(getDataBySearchCriteria(FinancialTransactionView.class, searchCriteria));
        log.debug("fetchExpensesGridData: totalRecords: {}", totalRecords);
        return gridResult(totalRecords, recordDetails);
    }

    @Transactional
    public ApiResponse saveFinancialTransaction(FinancialTransactionDto financialTransactionDto) {
        log.debug("saveFinancialTransaction: {}", financialTransactionDto);
        if (financialTransactionDto.getId() == null) {
            FinancialTransaction financialTransaction = financialTransactionMapper.toEntity(financialTransactionDto);
            financialTransaction.setAdminUser(
                    entityManager.getReference(User.class, 1)
            );
            financialTransaction.setCreatedBy(
                    entityManager.getReference(User.class, 1)
            );
            financialTransactionRepository.save(financialTransaction);
        } else {
            FinancialTransaction financialTransaction =
                    financialTransactionRepository.findById(financialTransactionDto.getId())
                            .orElseThrow(() -> {
                                        log.debug(
                                                "Financial transaction not found for update: id={}",
                                                financialTransactionDto.getId()
                                        );
                                        return new ResourceNotFoundException("The financial transaction you are trying to update was not found");
                                    }
                            );
            financialTransactionMapper.updateEntity(financialTransactionDto, financialTransaction);
            financialTransaction.setUpdatedBy(
                    entityManager.getReference(User.class, 1)
            );
        }
        return success("Financial transaction saved successfully");
    }
}
