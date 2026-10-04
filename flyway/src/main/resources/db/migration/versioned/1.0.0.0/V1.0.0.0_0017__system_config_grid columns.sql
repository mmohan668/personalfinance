-- ============================================================
-- System Config Grid Configuration
-- ============================================================

BEGIN;


-- ============================================================
-- 1. GRID NAME
-- ============================================================

INSERT INTO grid_name(name)
VALUES ('SYSTEM_CONFIG')
ON CONFLICT (LOWER(TRIM(name)))
    DO NOTHING;

-- ============================================================
-- 2. GRID COLUMNS
-- ============================================================

-- Config Name
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
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('SYSTEM_CONFIG'))),
        'configName',
        'Config Name',
        'text',
        'contains',
        200,
        0,
        'asc',
        0)
ON CONFLICT (grid_name_id, LOWER(TRIM(field)))
    DO NOTHING;

-- Config Value
INSERT INTO grid_column
(grid_name_id,
 field,
 header,
 data_type,
 default_filter_operator,
 width,
 visible_index,
 cell_template,
 align)
VALUES ((SELECT id
         FROM grid_name
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('SYSTEM_CONFIG'))),
        'configValue',
        'Config Value',
        'text',
        'contains',
        200,
        1,
        'hyperlinkCellTemplate',
        'CENTER')
ON CONFLICT (grid_name_id, LOWER(TRIM(field)))
    DO NOTHING;

-- Config Description
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
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('SYSTEM_CONFIG'))),
        'description',
        'Config Description',
        'text',
        'contains',
        500,
        2)
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
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('SYSTEM_CONFIG'))),
        'createdBy',
        'Created By',
        'text',
        'contains',
        200,
        3)
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
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('SYSTEM_CONFIG'))),
        'createdAt',
        'Created Date',
        'datetime',
        'equals',
        200,
        4)
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
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('SYSTEM_CONFIG'))),
        'updatedBy',
        'Modified By',
        'text',
        'contains',
        200,
        5)
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
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('SYSTEM_CONFIG'))),
        'updatedAt',
        'Modified Date',
        'datetime',
        'equals',
        200,
        6)
ON CONFLICT (grid_name_id, LOWER(TRIM(field)))
    DO NOTHING;

COMMIT;