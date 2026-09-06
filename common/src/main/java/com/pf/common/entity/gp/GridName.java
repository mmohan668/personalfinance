package com.pf.common.entity.gp;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "GRID_NAME")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GridName {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME", nullable = false, unique = true, length = 100)
    private String name;
}
