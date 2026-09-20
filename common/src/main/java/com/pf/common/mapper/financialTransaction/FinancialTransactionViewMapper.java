package com.pf.common.mapper.financialTransaction;

import com.pf.common.dto.financialTransaction.FinancialTransactionViewDto;
import com.pf.common.entity.financialTransaction.FinancialTransactionView;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FinancialTransactionViewMapper {

    FinancialTransactionViewDto toDto(FinancialTransactionView financialTransactionView);

    List<FinancialTransactionViewDto> toDtoList(List<FinancialTransactionView> financialTransactionList);
}
