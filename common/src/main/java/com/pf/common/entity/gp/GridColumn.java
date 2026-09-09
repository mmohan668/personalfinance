package com.pf.common.entity.gp;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "GRID_COLUMN")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GridColumn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GRID_NAME_ID", nullable = false)
    private GridName gridName;

    @Column(name = "FIELD", nullable = false, length = 50)
    private String field;

    @Column(name = "HEADER", nullable = false, length = 100)
    private String header;

    @Column(name = "DATA_TYPE", nullable = false, length = 50)
    private String dataType;

    @Column(name = "SORTABLE", nullable = false)
    private Boolean sortable;

    @Column(name = "DEFAULT_SORT_ORDER", length = 5)
    private String defaultSortOrder;

    @Column(name = "FILTERABLE", nullable = false)
    private Boolean filterable;

    @Column(name = "DEFAULT_FILTER_OPERATOR", length = 30)
    private String defaultFilterOperator;

    @Column(name = "WIDTH")
    private Integer width;

    @Column(name = "ALIGN")
    private String align;

    @Column(name = "VISIBLE")
    private Boolean visible;

    @Column(name = "VISIBLE_INDEX")
    private Integer visibleIndex;

    @Column(name = "CELL_TEMPLATE", length = 100)
    private String cellTemplate;

    @Column(name = "SORT_INDEX")
    private Integer sortIndex;
}

