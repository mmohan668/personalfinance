package com.pf.common.service.settings;

import com.google.common.collect.Lists;
import com.pf.common.record.generic.SelectItem;
import com.pf.common.dto.gp.GridFilter;
import com.pf.common.dto.gp.GridResult;
import com.pf.common.dto.gp.GridSort;
import com.pf.common.dto.gp.SearchCriteria;
import com.pf.common.dto.settings.ReferenceObjectDto;
import com.pf.common.dto.settings.ReferenceValueDto;
import com.pf.common.dto.settings.SystemConfigDto;
import com.pf.common.dto.generic.ApiResponse;
import com.pf.common.entity.settings.ReferenceObject;
import com.pf.common.entity.settings.ReferenceValue;
import com.pf.common.entity.settings.SystemConfig;
import com.pf.common.entity.userManagement.User;
import com.pf.common.mapper.settings.ReferenceObjectMapper;
import com.pf.common.mapper.settings.ReferenceValueMapper;
import com.pf.common.mapper.settings.SystemConfigMapper;
import com.pf.common.repository.categoryManagement.UserCategoryRepository;
import com.pf.common.repository.setting.ReferenceObjectRepository;
import com.pf.common.repository.setting.ReferenceValueRepository;
import com.pf.common.repository.setting.SystemConfigRepository;
import com.pf.common.service.generic.BaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.List;

import static com.pf.common.constants.CommonConstants.*;
import static com.pf.common.constants.EntityConstants.*;
import static com.pf.common.constants.FieldConstants.*;
import static com.pf.common.enums.FilterOperator.EQUALS;
import static com.pf.common.enums.RefObjectNames.*;
import static com.pf.common.enums.SortOrder.ASC;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettingsService extends BaseService {
    private final ReferenceObjectMapper referenceObjectMapper;
    private final ReferenceValueMapper referenceValueMapper;
    private final SystemConfigMapper systemConfigMapper;
    private final ReferenceObjectRepository referenceObjectRepository;
    private final ReferenceValueRepository referenceValueRepository;
    private final SystemConfigRepository systemConfigRepository;
    private final UserCategoryRepository userCategoryRepository;

    public GridResult fetchReferenceObjectGridData(SearchCriteria searchCriteria) {
        log.debug("fetchReferenceObjectGridData: {}", searchCriteria);
        GridFilter gridFilter = GridFilter.builder().field(USER_ID).operator(EQUALS.getValue()).value(String.valueOf(fetchLoginUser().getAdminUser().getId())).build();
        searchCriteria.getFilterList().add(gridFilter);
        searchCriteria.getSortList().add(new GridSort(ID, ASC.getValue()));
        searchCriteria.setFetchPaths(List.of(CREATED_BY_USER, UPDATED_BY_USER, USER));
        searchCriteria.setFIELD_MAPPINGS(ReferenceObjectDto.FIELD_MAPPINGS);
        long totalRecords = getCountBySearchCriteria(ReferenceObject.class, searchCriteria);
        List<ReferenceObjectDto> recordDetails = referenceObjectMapper.toDtoList(getDataBySearchCriteria(ReferenceObject.class, searchCriteria));
        log.debug("fetchReferenceObjectGridData: totalRecords: {}", totalRecords);
        return gridResult(totalRecords, recordDetails);
    }

    public GridResult fetchReferenceValueGridData(SearchCriteria searchCriteria) {
        log.debug("fetchReferenceValuesGridData: {}", searchCriteria);
        GridFilter gridFilter = GridFilter.builder().field(USER_ID).operator(EQUALS.getValue()).value(String.valueOf(fetchLoginUser().getAdminUser().getId())).build();
        searchCriteria.getFilterList().add(gridFilter);
        searchCriteria.getSortList().add(new GridSort(ID, ASC.getValue()));
        searchCriteria.setFetchPaths(List.of(REFERENCE_OBJECT, USER, CREATED_BY_USER, UPDATED_BY_USER));
        searchCriteria.setFIELD_MAPPINGS(ReferenceValueDto.FIELD_MAPPINGS);
        long totalRecords = getCountBySearchCriteria(ReferenceValue.class, searchCriteria);
        List<ReferenceValue> referenceValues = getDataBySearchCriteria(ReferenceValue.class, searchCriteria);
        List<ReferenceValueDto> recordDetails = referenceValueMapper.toDtoList(referenceValues);
        log.debug("fetchReferenceValuesGridData: totalRecords: {}", totalRecords);
        return gridResult(totalRecords, recordDetails);
    }

    public GridResult fetchSystemConfigGridData(SearchCriteria searchCriteria) {
        log.debug("fetchSystemConfigGridData: {}", searchCriteria);
        GridFilter gridFilter = GridFilter.builder().field(USER_ID).operator(EQUALS.getValue()).value(String.valueOf(fetchLoginUser().getAdminUser().getId())).build();
        searchCriteria.getFilterList().add(gridFilter);
        searchCriteria.getSortList().add(new GridSort(ID, ASC.getValue()));
        searchCriteria.setFetchPaths(List.of(CONFIG_VALUE, CREATED_BY_USER, UPDATED_BY_USER));
        searchCriteria.setFIELD_MAPPINGS(SystemConfigDto.FIELD_MAPPINGS);
        long totalRecords = getCountBySearchCriteria(SystemConfig.class, searchCriteria);
        List<SystemConfigDto> recordDetails = systemConfigMapper.toDtoList(getDataBySearchCriteria(SystemConfig.class, searchCriteria));
        log.debug("fetchSystemConfigGridData: totalRecords: {}", totalRecords);
        return gridResult(totalRecords, recordDetails);
    }

    @Transactional
    public ApiResponse saveReferenceValue(ReferenceValueDto referenceValueDto) {
        log.debug("saveReferenceValue: {}", referenceValueDto);
        User user = fetchLoginUser();
        ReferenceObject referenceObject = referenceObjectRepository.findById(referenceValueDto.getRefObjNameId()).orElse(null);
        if (referenceObject == null) {
            log.warn("saveReferenceValue: Reference object not found. refObjNameId = {}", referenceValueDto.getRefObjNameId());
            return failure("Reference object not found.");
        }
        ReferenceValue referenceValue;
        if (referenceValueDto.getId() == null) {
            //Create
            referenceValue = referenceValueMapper.toEntity(referenceValueDto);
            referenceValue.setReferenceObject(referenceObject);
            if (referenceValueRepository.countByReferenceObjectIdAndReferenceCode(referenceObject.getId(), referenceValue.getReferenceCode()) > 0) {
                log.warn("Reference code already exists for the selected reference object. refObjName = {}, referenceCode = {}", referenceObject.getRefObjName(), referenceValue.getReferenceCode());
                return failure("Reference code already exists for the selected reference object.");
            }
            referenceValue.setUser(user.getAdminUser());
            referenceValue.setCreatedBy(user);
        } else {
            //Update
            referenceValue = referenceValueRepository.findById(referenceValueDto.getId()).orElse(null);
            if (referenceValue == null) {
                log.warn("saveReferenceValue: Reference value not found. referenceValueId = {}", referenceValueDto.getId());
                return failure("Reference value not found.");
            }
            if (referenceValueRepository.countByReferenceObjectIdAndReferenceCodeAndIdNot(referenceObject.getId(), referenceValueDto.getReferenceCode(), referenceValue.getId()) > 0) {
                log.warn("Reference code already exists for the selected reference object. referenceCode = {}, refObjName {}", referenceValueDto.getReferenceCode(), referenceObject.getRefObjName());
                return failure("Reference code already exists for the selected reference object.");
            }
            referenceValue.setReferenceObject(referenceObject);
            referenceValue.setReferenceCode(referenceValueDto.getReferenceCode());
            referenceValue.setReferenceCodeDescription(referenceValueDto.getReferenceCodeDescription());
            referenceValue.setReferenceCode2(referenceValueDto.getReferenceCode2());
            referenceValue.setReferenceCode3(referenceValueDto.getReferenceCode3());
            referenceValue.setUser(user.getAdminUser());
            referenceValue.setUpdatedBy(user);
        }
        referenceValueRepository.save(referenceValue);
        return success("Reference value saved successfully.");
    }

    @Transactional
    public ApiResponse deleteReferenceValue(List<Long> ids) {
        log.debug("deleteReferenceValue: {}", ids);
        if (ids == null || ids.isEmpty()) {
            return failure("No reference values selected for deletion.");
        }
        List<List<Long>> chunks = Lists.partition(ids, CHUNK_SIZE);
        // Validate all selected reference values before deleting anything.
        for (List<Long> chunk : chunks) {
            if (systemConfigRepository.existsByConfigValueIdIn(chunk)) {
                return failure("Reference values cannot be deleted because one or more are currently in use.");
            }
            if (userCategoryRepository.existsByReferenceValueId(chunk)) {
                return failure("Reference values cannot be deleted because one or more are currently in use.");
            }
        }
        for (List<Long> chunk : chunks) {
            referenceValueRepository.deleteAllByIdInBatch(chunk);
        }
        return success("Reference value(s) deleted successfully.");
    }

    public List<SelectItem> fetchRefObjNames() {
        return referenceObjectRepository.fetchRefObjNames();
    }

    public List<SelectItem> fetchTransactionTypes() {
        return referenceValueRepository.fetchReferenceValuesByRefObjName(String.valueOf(TRANSACTION_TYPE));
    }

    public List<SelectItem> fetchLocations() {
        return referenceValueRepository.fetchReferenceValuesByRefObjName(String.valueOf(LOCATION));
    }

    public List<SelectItem> fetchCurrencies() {
        return referenceValueRepository.fetchReferenceValuesByRefObjName(String.valueOf(CURRENCY_CODE));
    }

    public Long fetchIdByReferenceCodeAndRefObjName(String referenceCode, String refObjName) {
        return referenceValueRepository.fetchIdByReferenceCodeAndRefObjName(referenceCode, refObjName);
    }

    @Transactional
    public ApiResponse saveSystemConfig(SystemConfigDto systemConfigDto) {
        log.debug("saveSystemConfig: {}", systemConfigDto);
        if (systemConfigDto == null) {
            return failure("System config object not found.");
        }
        SystemConfig systemConfig = systemConfigRepository.findById(systemConfigDto.getId()).orElse(null);
        if (systemConfig == null) {
            return failure("System config not found.");
        }
        ReferenceValue referenceValue = referenceValueRepository.findById(systemConfigDto.getReferenceId()).orElse(null);
        if (referenceValue == null) {
            return failure("Reference value not found.");
        }
        systemConfig.setConfigValue(referenceValue);
        systemConfig.setUpdatedBy(fetchLoginUser());
        return success("System config saved successfully.");
    }

    @Transactional
    public ApiResponse activateReferenceObject(List<Long> ids) {
        log.debug("activateReferenceObject: {}", ids);
        if (ids == null || ids.isEmpty()) {
            return failure("No reference object(s) selected for activation.");
        }
        Long modifiedBy = fetchLoginUser().getId();
        LocalDateTime modifiedAt = LocalDateTime.now();
        int activated = 0;
        List<List<Long>> chunks = Lists.partition(ids, CHUNK_SIZE);
        for (List<Long> chunk : chunks) {
            activated += referenceObjectRepository.activateReferenceObject(chunk, modifiedBy, modifiedAt);
        }
        log.debug("{} Reference object(s) activated", activated);
        return success("Reference object(s) activated successfully.");
    }

    @Transactional
    public ApiResponse inactivateReferenceObject(List<Long> ids) {
        log.debug("inactivateReferenceObject: {}", ids);
        if (ids == null || ids.isEmpty()) {
            return failure("No reference object(s) selected for inactivation.");
        }
        Long modifiedBy = fetchLoginUser().getId();
        LocalDateTime modifiedAt = LocalDateTime.now();
        int inactivated = 0;
        List<List<Long>> chunks = Lists.partition(ids, CHUNK_SIZE);
        for (List<Long> chunk : chunks) {
            inactivated += referenceObjectRepository.inactivateReferenceObject(chunk, modifiedBy, modifiedAt);
        }
        log.debug("{} Reference object(s) inactivated", inactivated);
        return success("Reference object(s) inactivated successfully.");
    }

    @Transactional
    public ApiResponse deleteReferenceObject(List<Long> ids) {
        log.debug("deleteReferenceObject: {}", ids);
        if (ids == null || ids.isEmpty()) {
            return failure("No reference object(s) selected for deletion.");
        }
        List<List<Long>> chunks = Lists.partition(ids, CHUNK_SIZE);
        for (List<Long> chunk : chunks) {
            if (referenceValueRepository.existsByReferenceObject(chunk)) {
                return failure("Reference object(s) cannot be deleted because one or more are currently in use.");
            }
        }
        for (List<Long> chunk : chunks) {
            referenceObjectRepository.deleteAllByIdInBatch(chunk);
        }
        log.debug("{} Reference object(s) deleted", ids.size());
        return success("Reference object(s) deleted successfully.");
    }

    @Transactional
    public ApiResponse saveReferenceObject(@RequestBody ReferenceObjectDto referenceObjectDto) {
        log.debug("saveReferenceObject: {}", referenceObjectDto);
        if (referenceObjectDto == null) {
            return failure("Reference object object not found.");
        }
        if (referenceObjectDto.getId() == null) {
            log.info("add reference object: {}", referenceObjectDto);
            if (referenceObjectRepository.existsByRefObjName(referenceObjectDto.getRefObjName())) {
                return failure("Reference object name already exists.");
            }
            ReferenceObject referenceObject = new ReferenceObject();
            referenceObject.setRefObjName(referenceObjectDto.getRefObjName().toUpperCase());
            referenceObject.setUser(fetchLoginUser().getAdminUser());
            referenceObject.setCreatedBy(fetchLoginUser());
            referenceObjectRepository.save(referenceObject);
        } else {
            log.info("update reference object: {}", referenceObjectDto);
            if (referenceObjectRepository.existsByRefObjNameAndId(referenceObjectDto.getRefObjName(), referenceObjectDto.getId())) {
                return failure("Reference object name already exists.");
            }
            ReferenceObject referenceObject = referenceObjectRepository.findById(referenceObjectDto.getId()).orElse(null);
            if (referenceObject == null) {
                return failure("Reference object not found.");
            }
            referenceObject.setRefObjName(referenceObjectDto.getRefObjName().toUpperCase());
            referenceObject.setUpdatedBy(fetchLoginUser());
        }
        return success("Reference object saved successfully.");
    }

    @Transactional
    public ApiResponse activateReferenceValue(List<Long> ids) {
        log.debug("activateReferenceValue: {}", ids);
        if (ids == null || ids.isEmpty()) {
            return failure("No reference value(s) selected for activation.");
        }
        Long updatedBy = fetchLoginUser().getId();
        LocalDateTime updatedAt = LocalDateTime.now();
        int activated = 0;
        List<List<Long>> chunks = Lists.partition(ids, CHUNK_SIZE);
        for (List<Long> chunk : chunks) {
            activated += referenceValueRepository.activateReferenceValues(chunk, updatedBy, updatedAt);
        }
        log.debug("{} Reference value(s) activated", activated);
        return success("Reference value(s) activated successfully.");
    }

    @Transactional
    public ApiResponse inactivateReferenceValue(List<Long> ids) {
        log.debug("inactivateReferenceValue: {}", ids);
        if (ids == null || ids.isEmpty()) {
            return failure("No reference value(s) selected for inactivation.");
        }
        Long updatedBy = fetchLoginUser().getId();
        LocalDateTime updatedAt = LocalDateTime.now();
        int inactivated = 0;
        List<List<Long>> chunks = Lists.partition(ids, CHUNK_SIZE);
        for (List<Long> chunk : chunks) {
            inactivated += referenceValueRepository.inactivateReferenceValues(chunk, updatedBy, updatedAt);
        }
        log.debug("{} Reference value(s) inactivated", inactivated);
        return success("Reference value(s) inactivated successfully.");
    }

}
