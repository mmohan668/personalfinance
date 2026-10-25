-- ============================================================
-- Bulk Uploads Status Grid Configuration
-- ============================================================

BEGIN;

-- ============================================================
-- 1. GRID NAME
-- ============================================================

INSERT INTO grid_name(name)
VALUES ('DOWNLOADS')
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
 visible_index,
 cell_template,
 filterable,
 sortable)
VALUES ((SELECT id
         FROM grid_name
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('DOWNLOADS'))),
        'fileName',
        'File Name',
        'text',
        0,
        'downloadCellTemplate',
        false,
        false)
ON CONFLICT (grid_name_id, LOWER(TRIM(field)))
    DO NOTHING;

INSERT INTO grid_column
(grid_name_id,
 field,
 header,
 data_type,
 visible_index,
 cell_template,
 filterable,
 sortable)
VALUES ((SELECT id
         FROM grid_name
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('DOWNLOADS'))),
        'size',
        'File Size',
        'text',
        1,
        'fileSizeCellTemplate',
        false,
        false)
ON CONFLICT (grid_name_id, LOWER(TRIM(field)))
    DO NOTHING;

INSERT INTO grid_column
(grid_name_id,
 field,
 header,
 data_type,
 visible_index,
 filterable,
 sortable,
 align)
VALUES ((SELECT id
         FROM grid_name
         WHERE LOWER(TRIM(name)) = LOWER(TRIM('DOWNLOADS'))),
        'dateCreated',
        'Created Date',
        'datetime',
        2,
        false,
        false,
        'RIGHT')
ON CONFLICT (grid_name_id, LOWER(TRIM(field)))
    DO NOTHING;

COMMIT;