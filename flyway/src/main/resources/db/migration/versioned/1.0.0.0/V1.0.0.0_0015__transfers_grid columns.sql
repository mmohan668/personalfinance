-- ============================================================
-- Transfers Grid Configuration
-- ============================================================

BEGIN;


-- ============================================================
-- 1. GRID NAME
-- ============================================================

INSERT INTO grid_name(name)
VALUES ('TRANSFERS_GRID')
ON CONFLICT (LOWER(TRIM(name)))
    DO NOTHING;


-- ============================================================
-- 2. GRID COLUMNS
-- ============================================================

-- Transaction Date
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
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('TRANSFERS_GRID'))),
        'transactionAt',
        'Transaction Date',
        'date',
        'equals',
        200,
        0,
        'desc',
        0)
ON CONFLICT (grid_name_id, LOWER(TRIM(field)))
    DO NOTHING;


-- Amount
INSERT INTO grid_column
(grid_name_id,
 field,
 header,
 data_type,
 default_filter_operator,
 width,
 visible_index,
 cell_template)
VALUES ((SELECT id
         FROM grid_name
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('TRANSFERS_GRID'))),
        'amount',
        'Amount',
        'number',
        'equals',
        200,
        1,
        'currencyCellTemplate')
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
 visible_index)
VALUES ((SELECT id
         FROM grid_name
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('TRANSFERS_GRID'))),
        'categoryName',
        'Category Name',
        'text',
        'contains',
        250,
        2)
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
 visible_index,
 visible)
VALUES ((SELECT id
         FROM grid_name
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('TRANSFERS_GRID'))),
        'categoryDescription',
        'Category Description',
        'text',
        'contains',
        250,
        3,
        false)
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
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('TRANSFERS_GRID'))),
        'subcategoryName',
        'Subcategory Name',
        'text',
        'contains',
        250,
        4)
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
 visible_index,
 visible)
VALUES ((SELECT id
         FROM grid_name
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('TRANSFERS_GRID'))),
        'subcategoryDescription',
        'Subcategory Description',
        'text',
        'contains',
        250,
        5,
        false)
ON CONFLICT (grid_name_id, LOWER(TRIM(field)))
    DO NOTHING;


-- Remarks
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
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('TRANSFERS_GRID'))),
        'remarks',
        'Remarks',
        'text',
        'contains',
        250,
        6)
ON CONFLICT (grid_name_id, LOWER(TRIM(field)))
    DO NOTHING;


-- Location
INSERT INTO grid_column
(grid_name_id,
 field,
 header,
 data_type,
 default_filter_operator,
 width,
 visible_index,
 cell_template)
VALUES ((SELECT id
         FROM grid_name
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('TRANSFERS_GRID'))),
        'location',
        'Location',
        'text',
        'contains',
        250,
        7,
        'titleCellTemplate')
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
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('TRANSFERS_GRID'))),
        'createdBy',
        'Created By',
        'text',
        'contains',
        200,
        8)
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
 visible_index,
 default_sort_order,
 sort_index)
VALUES ((SELECT id
         FROM grid_name
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('TRANSFERS_GRID'))),
        'createdAt',
        'Created Date',
        'datetime',
        'equals',
        200,
        9,
        'desc',
        1)
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
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('TRANSFERS_GRID'))),
        'updatedBy',
        'Modified By',
        'text',
        'contains',
        200,
        10)
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
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('TRANSFERS_GRID'))),
        'updatedAt',
        'Modified Date',
        'datetime',
        'equals',
        200,
        11)
ON CONFLICT (grid_name_id, LOWER(TRIM(field)))
    DO NOTHING;


COMMIT;