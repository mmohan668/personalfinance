INSERT INTO grid_name (name)
VALUES ('RECURRING_TRANSACTIONS_GRID')
ON CONFLICT (LOWER(TRIM(name))) DO NOTHING;

INSERT INTO grid_column
(grid_name_id, field, header, data_type, default_filter_operator, width, visible_index,
 default_sort_order, sort_index, cell_template, align)
SELECT gn.id,
       gc.field,
       gc.header,
       gc.data_type,
       gc.filter_operator,
       gc.width,
       gc.visible_index,
       gc.sort_order,
       gc.sort_index,
       gc.cell_template,
       gc.align
FROM grid_name gn
         CROSS JOIN (VALUES ('ruleName', 'Rule Name', 'text', 'contains', 180, 0, NULL, NULL, NULL, 'LEFT'),
                            ('transactionType', 'Transaction Type', 'text', 'contains', 150, 1, 'asc', 0, NULL, 'LEFT'),
                            ('categoryName', 'Category', 'text', 'contains', 160, 2, 'asc', 1, NULL, 'LEFT'),
                            ('subcategoryName', 'Subcategory', 'text', 'contains', 160, 3, 'asc', 2, NULL, 'LEFT'),
                            ('amount', 'Amount', 'number', 'equals', 120, 4, NULL, NULL, NULL, 'RIGHT'),
                            ('location', 'Location', 'text', 'contains', 160, 5, NULL, NULL, NULL, 'LEFT'),
                            ('frequency', 'Schedule', 'text', 'contains', 220, 6, NULL, NULL, 'cellValueTemplate',
                             'LEFT'),
                            ('startDate', 'Start Date', 'date', 'equals', 130, 7, NULL, NULL, NULL, 'RIGHT'),
                            ('endDate', 'End Date', 'date', 'equals', 130, 8, NULL, NULL, NULL, 'RIGHT'),
                            ('executionTime', 'Execution Time', 'text', 'equals', 120, 9, NULL, NULL, NULL, 'RIGHT'),
                            ('active', 'Status', 'boolean', 'equals', 100, 10, NULL, NULL, 'cellValueTemplate',
                             'CENTER'),
                            ('createdBy', 'Created By', 'text', 'contains', 160, 11, NULL, NULL, NULL, 'LEFT'),
                            ('createdAt', 'Created Date', 'datetime', 'equals', 160, 12, NULL, NULL, NULL, 'RIGHT'),
                            ('updatedBy', 'Updated By', 'text', 'contains', 160, 13, NULL, NULL, NULL, 'LEFT'),
                            ('updatedAt', 'Updated Date', 'datetime', 'equals', 160, 14, NULL, NULL, NULL,
                             'RIGHT')) AS gc(field, header, data_type, filter_operator, width, visible_index,
                                             sort_order, sort_index, cell_template, align)
WHERE LOWER(TRIM(gn.name)) = LOWER(TRIM('RECURRING_TRANSACTIONS_GRID'))
ON CONFLICT (grid_name_id, (LOWER(TRIM(field)))) DO NOTHING;
