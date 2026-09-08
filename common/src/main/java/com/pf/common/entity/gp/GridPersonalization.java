package com.pf.common.entity.gp;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


@Entity
@Table(name = "GRID_PERSONALIZATION",
        uniqueConstraints = @UniqueConstraint(name = "UQ_GRID_USER", columnNames = {"GRID_NAME_ID", "USER_ID"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GridPersonalization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GRID_NAME_ID", nullable = false)
    private GridName gridName;

    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "GRID_COLUMN", nullable = false, columnDefinition = "jsonb")
    private String gridColumnJson; // store JSON as String, or use JsonNode if using Hibernate Types
}
