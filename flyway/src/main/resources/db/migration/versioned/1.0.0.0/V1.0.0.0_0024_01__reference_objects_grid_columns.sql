-- ============================================================
-- Reference Objects Grid Configuration
-- ============================================================

-- ============================================================
-- 1. GRID NAME
-- ============================================================

INSERT INTO grid_name
    (name)
VALUES ('REFERENCE_OBJECTS_GRID') ON CONFLICT (LOWER(TRIM(name)))
    DO NOTHING;


-- ============================================================
-- 2. GRID COLUMNS
-- ============================================================

INSERT INTO grid_column
(grid_name_id,
 field,
 header,
 data_type,
 default_filter_operator,
 width,
 visible_index,
 default_sort_order,
 sort_index,
 align)
SELECT gn.id,
       gc.field,
       gc.header,
       gc.data_type,
       gc.default_filter_operator,
       gc.width,
       gc.visible_index,
       gc.default_sort_order,
       gc.sort_index,
       gc.align
FROM grid_name gn
         CROSS JOIN
     (VALUES ('refObjName',
              'Reference Object Name',
              'text',
              'contains',
              200,
              0,
              'asc',
              0,
              'LEFT'),
             ('createdBy',
              'Created By',
              'text',
              'contains',
              200,
              1,
              NULL,
              NULL,
              'LEFT'),
             ('createdAt',
              'Created Date',
              'datetime',
              'equals',
              200,
              2,
              NULL,
              NULL,
              'RIGHT'),
             ('updatedBy',
              'Modified By',
              'text',
              'contains',
              200,
              3,
              NULL,
              NULL,
              'LEFT'),
             ('updatedAt',
              'Modified Date',
              'datetime',
              'equals',
              200,
              4,
              NULL,
              NULL,
              'RIGHT')) AS gc
         (
          field,
          header,
          data_type,
          default_filter_operator,
          width,
          visible_index,
          default_sort_order,
          sort_index,
          align
             )
WHERE LOWER(TRIM(gn.name)) = LOWER(TRIM('REFERENCE_OBJECTS_GRID')) ON CONFLICT
    (
    grid_name_id,
    (LOWER(TRIM(field)))
    )
    DO NOTHING;


