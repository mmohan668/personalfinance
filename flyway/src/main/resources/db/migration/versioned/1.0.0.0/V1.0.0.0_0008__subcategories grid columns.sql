-- ============================================================
-- Subcategories Grid Configuration
-- ============================================================

BEGIN;


-- ============================================================
-- 1. GRID NAME
-- ============================================================

INSERT INTO grid_name(name)
VALUES ('SUBCATEGORIES_GRID')
ON CONFLICT (LOWER(TRIM(name)))
    DO NOTHING;


-- ============================================================
-- 2. GRID COLUMNS
-- ============================================================

-- Category Type
INSERT INTO grid_column
(grid_name_id,
 field,
 header,
 data_type,
 default_filter_operator,
 width,
 visible_index)
VALUES ((SELECT id
         FROM grid_name
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('SUBCATEGORIES_GRID'))),
        'categoryType',
        'Category Type',
        'text',
        'contains',
        200,
        0)
ON CONFLICT (grid_name_id, LOWER(TRIM(field)))
    DO NOTHING;


-- Category Name
INSERT INTO grid_column
(grid_name_id,
 field,
 header,
 data_type,
 default_filter_operator,
 width,
 visible_index,
 default_sort_order,
 sort_index)
VALUES ((SELECT id
         FROM grid_name
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('SUBCATEGORIES_GRID'))),
        'categoryName',
        'Category Name',
        'text',
        'contains',
        250,
        1,
        'asc',
        1)
ON CONFLICT (grid_name_id, LOWER(TRIM(field)))
    DO NOTHING;


-- Category Description
INSERT INTO grid_column
(grid_name_id,
 field,
 header,
 data_type,
 default_filter_operator,
 width,
 visible_index)
VALUES ((SELECT id
         FROM grid_name
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('SUBCATEGORIES_GRID'))),
        'categoryDescription',
        'Category Description',
        'text',
        'contains',
        250,
        2)
ON CONFLICT (grid_name_id, LOWER(TRIM(field)))
    DO NOTHING;


-- Subcategory Name
INSERT INTO grid_column
(grid_name_id,
 field,
 header,
 data_type,
 default_filter_operator,
 width,
 visible_index)
VALUES ((SELECT id
         FROM grid_name
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('SUBCATEGORIES_GRID'))),
        'subcategoryName',
        'Subcategory Name',
        'text',
        'contains',
        250,
        3)
ON CONFLICT (grid_name_id, LOWER(TRIM(field)))
    DO NOTHING;


-- Subcategory Description
INSERT INTO grid_column
(grid_name_id,
 field,
 header,
 data_type,
 default_filter_operator,
 width,
 visible_index)
VALUES ((SELECT id
         FROM grid_name
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('SUBCATEGORIES_GRID'))),
        'subcategoryDescription',
        'Subcategory Description',
        'text',
        'contains',
        250,
        4)
ON CONFLICT (grid_name_id, LOWER(TRIM(field)))
    DO NOTHING;


-- Active Status
INSERT INTO grid_column
(grid_name_id,
 field,
 header,
 data_type,
 default_filter_operator,
 visible_index,
 cell_template,
 align)
VALUES ((SELECT id
         FROM grid_name
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('SUBCATEGORIES_GRID'))),
        'isActive',
        'Active Status',
        'boolean',
        'equals',
        5,
        'cellValueTemplate',
        'CENTER')
ON CONFLICT (grid_name_id, LOWER(TRIM(field)))
    DO NOTHING;


-- Created By
INSERT INTO grid_column
(grid_name_id,
 field,
 header,
 data_type,
 default_filter_operator,
 width,
 visible_index)
VALUES ((SELECT id
         FROM grid_name
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('SUBCATEGORIES_GRID'))),
        'createdBy',
        'Created By',
        'text',
        'contains',
        200,
        6)
ON CONFLICT (grid_name_id, LOWER(TRIM(field)))
    DO NOTHING;


-- Created Date
INSERT INTO grid_column
(grid_name_id,
 field,
 header,
 data_type,
 default_filter_operator,
 width,
 visible_index)
VALUES ((SELECT id
         FROM grid_name
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('SUBCATEGORIES_GRID'))),
        'createdAt',
        'Created Date',
        'datetime',
        'equals',
        200,
        7)
ON CONFLICT (grid_name_id, LOWER(TRIM(field)))
    DO NOTHING;


-- Modified By
INSERT INTO grid_column
(grid_name_id,
 field,
 header,
 data_type,
 default_filter_operator,
 width,
 visible_index)
VALUES ((SELECT id
         FROM grid_name
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('SUBCATEGORIES_GRID'))),
        'updatedBy',
        'Modified By',
        'text',
        'contains',
        200,
        8)
ON CONFLICT (grid_name_id, LOWER(TRIM(field)))
    DO NOTHING;


-- Modified Date
INSERT INTO grid_column
(grid_name_id,
 field,
 header,
 data_type,
 default_filter_operator,
 width,
 visible_index)
VALUES ((SELECT id
         FROM grid_name
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('SUBCATEGORIES_GRID'))),
        'updatedAt',
        'Modified Date',
        'datetime',
        'equals',
        200,
        9)
ON CONFLICT (grid_name_id, LOWER(TRIM(field)))
    DO NOTHING;


COMMIT;