INSERT INTO grid_name
    (name)
VALUES ('DOWNLOADS_GRID')
ON CONFLICT (LOWER(TRIM(name))) DO NOTHING;

INSERT INTO grid_column
(grid_name_id,
 field,
 header,
 data_type,
 visible_index,
 filterable,
 sortable,
 cell_template,
 align)
SELECT gn.id,
       gc.field,
       gc.header,
       gc.data_type,
       gc.visible_index,
       gc.filterable,
       gc.sortable,
       gc.cell_template,
       gc.align
FROM grid_name gn
         CROSS JOIN
     (VALUES ('fileName',
              'File Name',
              'text',
              0,
              FALSE,
              FALSE,
              'downloadCellTemplate',
              'LEFT'),
             ('size',
              'File Size',
              'text',
              1,
              FALSE,
              FALSE,
              'fileSizeCellTemplate',
              'LEFT'),
             ('dateCreated',
              'Created Date',
              'datetime',
              2,
              FALSE,
              FALSE,
              NULL,
              'RIGHT')) AS gc
         (
          field,
          header,
          data_type,
          visible_index,
          filterable,
          sortable,
          cell_template,
          align
             )
WHERE LOWER(TRIM(gn.name)) = LOWER(TRIM('DOWNLOADS_GRID'))
ON CONFLICT ( grid_name_id, (LOWER(TRIM(field))) ) DO NOTHING;


