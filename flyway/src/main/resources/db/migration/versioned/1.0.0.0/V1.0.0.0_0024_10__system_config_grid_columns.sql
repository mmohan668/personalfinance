-- ============================================================
-- System Config Grid Configuration
-- ============================================================

-- ============================================================
-- 1. GRID NAME
-- ============================================================

INSERT INTO grid_name
    (name)
VALUES ('SYSTEM_CONFIG') ON CONFLICT (LOWER(TRIM(name)))
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
 filterable,
 sortable,
 cell_template,
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
       gc.filterable,
       gc.sortable,
       gc.cell_template,
       gc.align
FROM grid_name gn
         CROSS JOIN
     (VALUES ('configName',
              'Config Name',
              'text',
              'contains',
              200,
              0,
              'asc',
              0,
              FALSE,
              FALSE,
              NULL,
              'LEFT'),
             ('configValue',
              'Config Value',
              'text',
              'contains',
              200,
              1,
              NULL,
              NULL,
              FALSE,
              FALSE,
              'hyperlinkCellTemplate',
              'LEFT'),
             ('description',
              'Config Description',
              'text',
              'contains',
              500,
              2,
              NULL,
              NULL,
              FALSE,
              FALSE,
              NULL,
              'LEFT'),
             ('createdBy',
              'Created By',
              'text',
              'contains',
              200,
              3,
              NULL,
              NULL,
              FALSE,
              FALSE,
              NULL,
              'LEFT'),
             ('createdAt',
              'Created Date',
              'datetime',
              'equals',
              200,
              4,
              NULL,
              NULL,
              FALSE,
              FALSE,
              NULL,
              'RIGHT'),
             ('updatedBy',
              'Modified By',
              'text',
              'contains',
              200,
              5,
              NULL,
              NULL,
              FALSE,
              FALSE,
              NULL,
              'LEFT'),
             ('updatedAt',
              'Modified Date',
              'datetime',
              'equals',
              200,
              6,
              NULL,
              NULL,
              FALSE,
              FALSE,
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
          filterable,
          sortable,
          cell_template,
          align
             )
WHERE LOWER(TRIM(gn.name)) = LOWER(TRIM('SYSTEM_CONFIG')) ON CONFLICT
    (
    grid_name_id,
    (LOWER(TRIM(field)))
    )
    DO NOTHING;


