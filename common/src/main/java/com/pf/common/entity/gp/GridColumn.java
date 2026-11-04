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

    @Column(name = "FIELD", nullable = false)
    private String field;

    @Column(name = "HEADER", nullable = false)
    private String header;

    @Column(name = "DATA_TYPE", nullable = false)
    private String dataType;

    @Column(name = "SORTABLE", nullable = false)
    private Boolean sortable;

    @Column(name = "DEFAULT_SORT_ORDER")
    private String defaultSortOrder;

    @Column(name = "FILTERABLE", nullable = false)
    private Boolean filterable;

    @Column(name = "EXPORTABLE", nullable = false)
    private Boolean exportable;

    @Column(name = "DEFAULT_FILTER_OPERATOR")
    private String defaultFilterOperator;

    @Column(name = "WIDTH")
    private Integer width;

    @Column(name = "ALIGN")
    private String align;

    @Column(name = "VISIBLE")
    private Boolean visible;

    @Column(name = "VISIBLE_INDEX")
    private Integer visibleIndex;

    @Column(name = "CELL_TEMPLATE")
    private String cellTemplate;

    @Column(name = "SORT_INDEX")
    private Integer sortIndex;
}

