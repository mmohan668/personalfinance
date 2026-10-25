package com.pf.common.service.userManagement;

import com.pf.common.dto.gp.GridResult;
import com.pf.common.dto.gp.GridSort;
import com.pf.common.dto.gp.SearchCriteria;
import com.pf.common.dto.userManagement.UserDto;
import com.pf.common.entity.settings.ReferenceObject;
import com.pf.common.entity.userManagement.User;
import com.pf.common.mapper.userManagement.UserMapper;
import com.pf.common.service.generic.BaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.pf.common.constants.EntityConstants.ADMIN_USER;
import static com.pf.common.constants.FieldConstants.ID;
import static com.pf.common.enums.SortOrder.ASC;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService extends BaseService {
    private final UserMapper userMapper;

    @Transactional
    public GridResult fetchUsersGridData(SearchCriteria searchCriteria) {
        log.debug("fetchUsersGridData: {}", searchCriteria);
        searchCriteria.getSortList().add(new GridSort(ID, ASC.getValue()));
        searchCriteria.setFetchPaths(List.of(ADMIN_USER));
        searchCriteria.setFIELD_MAPPINGS(UserDto.FIELD_MAPPINGS);
        long totalRecords = 0;
        if (!searchCriteria.isLoadAllData()) {
            totalRecords = getCountBySearchCriteria(
                    User.class,
                    searchCriteria
            );
            if (totalRecords == 0) {
                return gridResult(totalRecords, List.of());
            }
        }
        List<UserDto> recordDetails = userMapper.toDtoList(
                getDataBySearchCriteria(User.class, searchCriteria)
        );
        log.debug("fetchUsersGridData: totalRecords: {}", totalRecords);
        return gridResult(totalRecords, recordDetails);
    }
}
