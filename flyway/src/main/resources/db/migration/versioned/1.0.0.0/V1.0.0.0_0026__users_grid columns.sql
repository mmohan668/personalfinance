-- ============================================================
-- Users Grid Configuration
-- ============================================================

BEGIN;

-- ============================================================
-- 1. GRID NAME
-- ============================================================

INSERT INTO grid_name(name)
VALUES ('USERS_GRID')
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
 default_sort_order,
 visible_index,
 width)
VALUES ((SELECT id
         FROM grid_name
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('USERS_GRID'))),
        'username',
        'Username',
        'text',
        'contains',
        'asc',
        0,
        200)
ON CONFLICT (grid_name_id, LOWER(TRIM(field)))
    DO NOTHING;

INSERT INTO grid_column
(grid_name_id,
 field,
 header,
 data_type,
 default_filter_operator,
 visible_index,
 width)
VALUES ((SELECT id
         FROM grid_name
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('USERS_GRID'))),
        'displayName',
        'Display Name',
        'text',
        'contains',
        1,
        200)
ON CONFLICT (grid_name_id, LOWER(TRIM(field)))
    DO NOTHING;

INSERT INTO grid_column
(grid_name_id,
 field,
 header,
 data_type,
 default_filter_operator,
 visible_index,
 width)
VALUES ((SELECT id
         FROM grid_name
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('USERS_GRID'))),
        'email',
        'Email Id',
        'text',
        'contains',
        2,
        300)
ON CONFLICT (grid_name_id, LOWER(TRIM(field)))
    DO NOTHING;

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
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('USERS_GRID'))),
        'active',
        'Active Status',
        'boolean',
        'equals',
        3,
        'cellValueTemplate',
        'CENTER')
ON CONFLICT (grid_name_id, LOWER(TRIM(field)))
    DO NOTHING;

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
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('USERS_GRID'))),
        'isAdminUser',
        'Admin User',
        'boolean',
        'equals',
        4,
        'cellValueTemplate',
        'CENTER')
ON CONFLICT (grid_name_id, LOWER(TRIM(field)))
    DO NOTHING;

INSERT INTO grid_column
(grid_name_id,
 field,
 header,
 data_type,
 default_filter_operator,
 visible_index,
 width)
VALUES ((SELECT id
         FROM grid_name
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('USERS_GRID'))),
        'createdBy',
        'Created By',
        'text',
        'contains',
        5,
        200)
ON CONFLICT (grid_name_id, LOWER(TRIM(field)))
    DO NOTHING;

INSERT INTO grid_column
(grid_name_id,
 field,
 header,
 data_type,
 default_filter_operator,
 visible_index,
 width,
 align)
VALUES ((SELECT id
         FROM grid_name
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('USERS_GRID'))),
        'createdAt',
        'Created Date',
        'datetime',
        'equals',
        6,
        200,
        'RIGHT')
ON CONFLICT (grid_name_id, LOWER(TRIM(field)))
    DO NOTHING;

INSERT INTO grid_column
(grid_name_id,
 field,
 header,
 data_type,
 default_filter_operator,
 visible_index,
 width)
VALUES ((SELECT id
         FROM grid_name
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('USERS_GRID'))),
        'updatedBy',
        'Updated By',
        'text',
        'contains',
        7,
        200)
ON CONFLICT (grid_name_id, LOWER(TRIM(field)))
    DO NOTHING;

INSERT INTO grid_column
(grid_name_id,
 field,
 header,
 data_type,
 visible_index,
 default_filter_operator,
 width,
 align)
VALUES ((SELECT id
         FROM grid_name
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('USERS_GRID'))),
        'updatedAt',
        'Updated Date',
        'datetime',
        8,
        'equals',
        200,
        'RIGHT')
ON CONFLICT (grid_name_id, LOWER(TRIM(field)))
    DO NOTHING;

COMMIT;