-- ============================================================
-- Subcategories Grid Configuration
-- ============================================================

BEGIN;

-- ============================================================
-- 1. GRID NAME
-- ============================================================

INSERT INTO grid_name(name)
VALUES ('REFERENCE_OBJECTS_GRID')
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
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('REFERENCE_OBJECTS_GRID'))),
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
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('REFERENCE_OBJECTS_GRID'))),
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
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('REFERENCE_OBJECTS_GRID'))),
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
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('REFERENCE_OBJECTS_GRID'))),
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
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('REFERENCE_OBJECTS_GRID'))),
        'updatedAt',
        'Modified Date',
        'datetime',
        'equals',
        200,
        9)
ON CONFLICT (grid_name_id, LOWER(TRIM(field)))
    DO NOTHING;

COMMIT;