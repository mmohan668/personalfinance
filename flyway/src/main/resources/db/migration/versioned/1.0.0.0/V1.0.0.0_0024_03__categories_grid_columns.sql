-- ============================================================
-- Categories Grid Configuration
-- ============================================================

-- ============================================================
-- 1. GRID NAME
-- ============================================================

INSERT INTO grid_name
    (name)
VALUES ('CATEGORIES_GRID') ON CONFLICT (LOWER(TRIM(name)))
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
       gc.cell_template,
       gc.align
FROM grid_name gn
         CROSS JOIN
     (VALUES ('transactionType',
              'Transaction Type',
              'text',
              'contains',
              200,
              0,
              'asc',
              0,
              NULL,
              'LEFT'),
             ('categoryName',
              'Category Name',
              'text',
              'contains',
              250,
              1,
              'asc',
              1,
              NULL,
              'LEFT'),
             ('categoryDescription',
              'Category Description',
              'text',
              'contains',
              250,
              2,
              NULL,
              NULL,
              NULL,
              'LEFT'),
             ('active',
              'Active Status',
              'boolean',
              'equals',
              NULL,
              3,
              NULL,
              NULL,
              'cellValueTemplate',
              'CENTER'),
             ('createdBy',
              'Created By',
              'text',
              'contains',
              200,
              4,
              NULL,
              NULL,
              NULL,
              'LEFT'),
             ('createdAt',
              'Created Date',
              'datetime',
              'equals',
              200,
              5,
              NULL,
              NULL,
              NULL,
              'RIGHT'),
             ('updatedBy',
              'Modified By',
              'text',
              'contains',
              200,
              6,
              NULL,
              NULL,
              NULL,
              'LEFT'),
             ('updatedAt',
              'Modified Date',
              'datetime',
              'equals',
              200,
              7,
              NULL,
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
          cell_template,
          align
             )
WHERE LOWER(TRIM(gn.name)) = LOWER(TRIM('CATEGORIES_GRID')) ON CONFLICT
    (
    grid_name_id,
    (LOWER(TRIM(field)))
    )
    DO NOTHING;


