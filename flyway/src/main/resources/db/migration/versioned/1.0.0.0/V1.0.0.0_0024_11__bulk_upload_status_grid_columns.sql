-- ============================================================
-- Bulk Uploads Status Grid Configuration
-- ============================================================

-- ============================================================
-- 1. GRID NAME
-- ============================================================

INSERT INTO grid_name
    (name)
VALUES ('BULK_UPLOADS_STATUS') ON CONFLICT (LOWER(TRIM(name)))
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
 filterable,
 sortable,
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
       gc.filterable,
       gc.sortable,
       gc.cell_template,
       gc.align
FROM grid_name gn
         CROSS JOIN
     (VALUES ('uploadType',
              'Upload Type',
              'text',
              'contains',
              250,
              0,
              NULL,
              NULL,
              TRUE,
              TRUE,
              NULL,
              'LEFT'),
             ('status',
              'Status',
              'text',
              'contains',
              200,
              1,
              NULL,
              NULL,
              TRUE,
              TRUE,
              NULL,
              'LEFT'),
             ('uploadedFile',
              'Uploaded File',
              'text',
              'contains',
              150,
              2,
              NULL,
              NULL,
              FALSE,
              FALSE,
              'downloadCellTemplate',
              'CENTER'),
             ('errorFile',
              'Error File',
              'text',
              'contains',
              150,
              3,
              NULL,
              NULL,
              FALSE,
              FALSE,
              'downloadCellTemplate',
              'CENTER'),
             ('remarks',
              'Remarks',
              'text',
              'contains',
              300,
              4,
              NULL,
              NULL,
              TRUE,
              TRUE,
              NULL,
              'LEFT'),
             ('createdBy',
              'Created By',
              'text',
              'contains',
              200,
              5,
              NULL,
              NULL,
              TRUE,
              TRUE,
              NULL,
              'LEFT'),
             ('createdAt',
              'Created Date',
              'datetime',
              'equals',
              200,
              6,
              'desc',
              0,
              TRUE,
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
          filterable,
          sortable,
          cell_template,
          align
             )
WHERE LOWER(TRIM(gn.name)) = LOWER(TRIM('BULK_UPLOADS_STATUS')) ON CONFLICT
    (
    grid_name_id,
    (LOWER(TRIM(field)))
    )
    DO NOTHING;


