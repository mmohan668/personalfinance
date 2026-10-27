package com.pf.common.service.financialTransaction;

import com.pf.common.dto.financialTransaction.FinancialTransactionDto;
import com.pf.common.dto.financialTransaction.FinancialTransactionViewDto;
import com.pf.common.dto.generic.ApiResponse;
import com.pf.common.dto.gp.GridResult;
import com.pf.common.dto.gp.SearchCriteria;
import com.pf.common.entity.financialTransaction.FinancialTransaction;
import com.pf.common.entity.financialTransaction.FinancialTransactionView;
import com.pf.common.entity.userManagement.User;
import com.pf.common.exception.ResourceNotFoundException;
import com.pf.common.mapper.financialTransaction.FinancialTransactionMapper;
import com.pf.common.mapper.financialTransaction.FinancialTransactionViewMapper;
import com.pf.common.repository.financialTransaction.FinancialTransactionRepository;
import com.pf.common.service.generic.BaseService;
import com.pf.common.util.BatchUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinancialTransactionService extends BaseService {
    private final FinancialTransactionRepository financialTransactionRepository;
    private final FinancialTransactionMapper financialTransactionMapper;
    private final FinancialTransactionViewMapper financialTransactionViewMapper;

    @Transactional
    public GridResult fetchFinancialTransactionGridData(SearchCriteria searchCriteria) {
        log.debug("fetchFinancialTransactionGridData: searchCriteria: {}", searchCriteria);
        long totalRecords = 0;
        if (!searchCriteria.isLoadAllData()) {
            totalRecords = getCountBySearchCriteria(
                    FinancialTransactionView.class,
                    searchCriteria
            );
            if (totalRecords == 0) {
                gridResult(totalRecords, List.of());
            }
        }
        List<FinancialTransactionViewDto> recordDetails = financialTransactionViewMapper.toDtoList(
                getDataBySearchCriteria(FinancialTransactionView.class, searchCriteria)
        );
        log.debug("fetchFinancialTransactionGridData: totalRecords: {}", totalRecords);
        return gridResult(totalRecords, recordDetails);
    }

    @Transactional
    public ApiResponse saveFinancialTransaction(FinancialTransactionDto financialTransactionDto) {
        log.debug("saveFinancialTransaction: {}", financialTransactionDto);
        User user = fetchLoginUser();
        if (!StringUtils.hasText(financialTransactionDto.getRemarks())) {
            financialTransactionDto.setRemarks(null);
        }
        FinancialTransaction sameFinancialTransaction = financialTransactionRepository.findIdBySameCombination(
                financialTransactionDto.getSubcategoryId(),
                financialTransactionDto.getTransactionAt(),
                financialTransactionDto.getLocationId(),
                financialTransactionDto.getRemarks() != null ? financialTransactionDto.getRemarks().toLowerCase(Locale.ROOT).trim() : null,
                user.getAdminUser().getId()
        );
        if (sameFinancialTransaction != null
                && (financialTransactionDto.getId() == null
                || !financialTransactionDto.getId().equals(sameFinancialTransaction.getId()))) {
            log.debug(
                    "Same FinancialTransaction already exists!. So updating the same instead of creating new record."
            );
            BigDecimal amount = sameFinancialTransaction.getAmount();
            amount = amount.add(financialTransactionDto.getAmount());
            sameFinancialTransaction.setAmount(amount);
            if (financialTransactionDto.getRemarks() != null) {
                sameFinancialTransaction.setRemarks(financialTransactionDto.getRemarks());
            }
            if (financialTransactionDto.getLocationId() != null) {
                sameFinancialTransaction.setLocation(financialTransactionMapper.location(financialTransactionDto.getLocationId()));
            }
            sameFinancialTransaction.setUpdatedBy(user);
            financialTransactionRepository.save(sameFinancialTransaction);
            if (financialTransactionDto.getId() != null) {
                log.debug("Deleting updated FinancialTransaction with id: {}", financialTransactionDto.getId());
                deleteFinancialTransactions(List.of(financialTransactionDto.getId()));
                log.debug("Deleted FinancialTransaction with id: {}", financialTransactionDto.getId());
            }
            return success("Financial transaction saved successfully");
        }
        if (financialTransactionDto.getId() == null) {
            FinancialTransaction financialTransaction = financialTransactionMapper.toEntity(financialTransactionDto);
            financialTransaction.setAdminUser(user.getAdminUser());
            financialTransaction.setCreatedBy(user);
            financialTransactionRepository.save(financialTransaction);
        } else {
            FinancialTransaction financialTransaction =
                    financialTransactionRepository.findById(financialTransactionDto.getId())
                            .orElseThrow(() -> {
                                        log.debug(
                                                "Financial transaction not found for update: id={}",
                                                financialTransactionDto.getId()
                                        );
                                        return new ResourceNotFoundException(
                                                "The financial transaction you are trying to update was not found"
                                        );
                                    }
                            );
            financialTransactionDto.setAdminUserId(user.getAdminUser().getId());
            financialTransactionMapper.updateEntity(financialTransactionDto, financialTransaction);
            financialTransaction.setUpdatedBy(user);
        }
        return success("Financial transaction saved successfully");
    }

    @Transactional
    public ApiResponse deleteFinancialTransactions(List<Long> financialTransactionIds) {
        log.debug("deleteFinancialTransactions: {}", financialTransactionIds);
        if (financialTransactionIds == null || financialTransactionIds.isEmpty()) {
            log.debug("deleteFinancialTransactions: financialTransactionIds is null or empty");
            return failure("No records found to delete");
        }
        BatchUtils.processInBatches(
                financialTransactionIds,
                financialTransactionRepository::deleteAllByIdInBatch
        );
        return success("Financial transaction deleted successfully");
    }
}
