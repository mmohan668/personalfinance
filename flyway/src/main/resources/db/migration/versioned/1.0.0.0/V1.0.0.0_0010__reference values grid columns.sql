-- ============================================================
-- Subcategories Grid Configuration
-- ============================================================

BEGIN;

-- ============================================================
-- 1. GRID NAME
-- ============================================================

INSERT INTO grid_name(name)
VALUES ('REFERENCE_VALUES_GRID')
ON CONFLICT (LOWER(TRIM(name)))
    DO NOTHING;

-- ============================================================
-- 2. GRID COLUMNS
-- ============================================================

-- Reference Object Name
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
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('REFERENCE_VALUES_GRID'))),
        'refObjName',
        'Reference Object Name',
        'text',
        'contains',
        200,
        0,
        'asc',
        0)
ON CONFLICT (grid_name_id, LOWER(TRIM(field)))
    DO NOTHING;

-- Reference Code
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
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('REFERENCE_VALUES_GRID'))),
        'referenceCode',
        'Reference Code',
        'text',
        'contains',
        200,
        1,
        'asc',
        1)
ON CONFLICT (grid_name_id, LOWER(TRIM(field)))
    DO NOTHING;

-- Reference Code Description
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
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('REFERENCE_VALUES_GRID'))),
        'referenceCodeDescription',
        'Reference Code Description',
        'text',
        'contains',
        200,
        2)
ON CONFLICT (grid_name_id, LOWER(TRIM(field)))
    DO NOTHING;

-- Reference Code 2
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
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('REFERENCE_VALUES_GRID'))),
        'referenceCode2',
        'Reference Code 2',
        'text',
        'contains',
        200,
        3)
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
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('REFERENCE_VALUES_GRID'))),
        'createdBy',
        'Created By',
        'text',
        'contains',
        200,
        4)
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
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('REFERENCE_VALUES_GRID'))),
        'createdAt',
        'Created Date',
        'datetime',
        'equals',
        200,
        5)
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
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('REFERENCE_VALUES_GRID'))),
        'updatedBy',
        'Modified By',
        'text',
        'contains',
        200,
        6)
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
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('REFERENCE_VALUES_GRID'))),
        'updatedAt',
        'Modified Date',
        'datetime',
        'equals',
        200,
        7)
ON CONFLICT (grid_name_id, LOWER(TRIM(field)))
    DO NOTHING;

COMMIT;