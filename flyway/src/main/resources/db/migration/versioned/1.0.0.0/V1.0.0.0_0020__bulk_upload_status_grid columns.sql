-- ============================================================
-- Bulk Uploads Status Grid Configuration
-- ============================================================

BEGIN;

-- ============================================================
-- 1. GRID NAME
-- ============================================================

INSERT INTO grid_name(name)
VALUES ('BULK_UPLOADS_STATUS')
ON CONFLICT (LOWER(TRIM(name)))
    DO NOTHING;


-- ============================================================
-- 2. GRID COLUMNS
-- ============================================================

-- Upload Type
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
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('BULK_UPLOADS_STATUS'))),
        'uploadType',
        'Upload Type',
        'text',
        'contains',
        300,
        0)
ON CONFLICT (grid_name_id, LOWER(TRIM(field)))
    DO NOTHING;

-- Status
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
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('BULK_UPLOADS_STATUS'))),
        'status',
        'Status',
        'text',
        'contains',
        150,
        1)
ON CONFLICT (grid_name_id, LOWER(TRIM(field)))
    DO NOTHING;

-- Upload File
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
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('BULK_UPLOADS_STATUS'))),
        'uploadedFile',
        'Uploaded File',
        'text',
        'contains',
        300,
        2,
        'downloadCellTemplate',
        'CENTER')
ON CONFLICT (grid_name_id, LOWER(TRIM(field)))
    DO NOTHING;

-- Error File
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
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('BULK_UPLOADS_STATUS'))),
        'errorFile',
        'Error File',
        'text',
        'contains',
        300,
        3,
        'downloadCellTemplate',
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
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('BULK_UPLOADS_STATUS'))),
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
 visible_index,
 default_sort_order,
 sort_index)
VALUES ((SELECT id
         FROM grid_name
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('BULK_UPLOADS_STATUS'))),
        'createdAt',
        'Created Date',
        'datetime',
        'equals',
        200,
        5,
        'desc',
        0)
ON CONFLICT (grid_name_id, LOWER(TRIM(field)))
    DO NOTHING;


COMMIT;