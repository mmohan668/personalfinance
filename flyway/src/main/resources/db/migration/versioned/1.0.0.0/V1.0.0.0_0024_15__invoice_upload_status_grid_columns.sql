INSERT INTO grid_name (name)
VALUES ('INVOICE_UPLOAD_STATUS')
ON CONFLICT (LOWER(TRIM(name))) DO NOTHING;

INSERT INTO grid_column
    (grid_name_id, field, header, data_type, default_filter_operator, width, visible_index,
     default_sort_order, sort_index, filterable, sortable, cell_template, align)
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
CROSS JOIN (VALUES
    ('invoiceNumber', 'Invoice Number', 'text', 'contains', 220, 0, NULL, NULL, TRUE, TRUE, NULL, 'LEFT'),
    ('invoiceDate', 'Invoice Date', 'date', 'equals', 160, 1, NULL, NULL, TRUE, TRUE, NULL, 'RIGHT'),
    ('fileName', 'File Name', 'text', 'contains', 260, 2, NULL, NULL, TRUE, TRUE, NULL, 'LEFT'),
    ('status', 'Status', 'text', 'contains', 140, 3, NULL, NULL, TRUE, TRUE, NULL, 'LEFT'),
    ('categorySubcategorySuggestion', 'Category/Subcategory Suggestions', 'text', 'contains', 420, 4, NULL, NULL, FALSE, FALSE, NULL, 'LEFT'),
    ('uploadedDate', 'Uploaded Date', 'date', 'equals', 160, 5, NULL, NULL, TRUE, TRUE, NULL, 'RIGHT'),
    ('uploadedBy', 'Uploaded By', 'text', 'contains', 180, 6, NULL, NULL, TRUE, TRUE, NULL, 'LEFT'),
    ('uploadedAt', 'Uploaded At', 'datetime', 'equals', 200, 7, 'desc', 0, TRUE, TRUE, NULL, 'RIGHT')
) AS gc(field, header, data_type, default_filter_operator, width, visible_index,
        default_sort_order, sort_index, filterable, sortable, cell_template, align)
WHERE LOWER(TRIM(gn.name)) = LOWER(TRIM('INVOICE_UPLOAD_STATUS'))
ON CONFLICT (grid_name_id, (LOWER(TRIM(field)))) DO NOTHING;
