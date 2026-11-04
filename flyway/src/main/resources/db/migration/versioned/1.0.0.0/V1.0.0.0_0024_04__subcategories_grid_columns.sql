-- ============================================================
-- Subcategories Grid Configuration
-- ============================================================

-- ============================================================
-- 1. GRID NAME
-- ============================================================

INSERT INTO grid_name
(name)
VALUES ('SUBCATEGORIES_GRID')
ON CONFLICT (LOWER(TRIM(name)))
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
 visible,
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
       gc.visible,
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
              TRUE,
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
              TRUE,
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
              FALSE,
              NULL,
              'LEFT'),
             ('subcategoryName',
              'Subcategory Name',
              'text',
              'contains',
              250,
              3,
              'asc',
              2,
              TRUE,
              NULL,
              'LEFT'),
             ('subcategoryDescription',
              'Subcategory Description',
              'text',
              'contains',
              250,
              4,
              NULL,
              NULL,
              TRUE,
              NULL,
              'LEFT'),
             ('isActive',
              'Active Status',
              'boolean',
              'equals',
              NULL,
              5,
              NULL,
              NULL,
              TRUE,
              'cellValueTemplate',
              'CENTER'),
             ('createdBy',
              'Created By',
              'text',
              'contains',
              200,
              6,
              NULL,
              NULL,
              TRUE,
              NULL,
              'LEFT'),
             ('createdAt',
              'Created Date',
              'datetime',
              'equals',
              200,
              7,
              NULL,
              NULL,
              TRUE,
              NULL,
              'RIGHT'),
             ('updatedBy',
              'Modified By',
              'text',
              'contains',
              200,
              8,
              NULL,
              NULL,
              TRUE,
              NULL,
              'LEFT'),
             ('updatedAt',
              'Modified Date',
              'datetime',
              'equals',
              200,
              9,
              NULL,
              NULL,
              TRUE,
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
          visible,
          cell_template,
          align
             )
WHERE LOWER(TRIM(gn.name)) = LOWER(TRIM('SUBCATEGORIES_GRID'))
ON CONFLICT
    (
    grid_name_id,
    (LOWER(TRIM(field)))
    )
    DO NOTHING;


