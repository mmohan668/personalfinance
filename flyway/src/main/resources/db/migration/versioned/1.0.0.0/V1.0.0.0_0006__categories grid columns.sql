INSERT INTO grid_name(name)
VALUES ('CATEGORIES_GRID');

INSERT INTO grid_column(grid_name_id, field, header, data_type, default_filter_operator, width,
                        visible_index)
VALUES ((SELECT id FROM grid_name WHERE name = 'CATEGORIES_GRID'), 'transactionType', 'Category Type', 'text',
        'contains', 200, 0);

INSERT INTO grid_column(grid_name_id, field, header, data_type, default_filter_operator, width,
                        visible_index, default_sort_order)
VALUES ((SELECT id FROM grid_name WHERE name = 'CATEGORIES_GRID'), 'categoryName', 'Category Name', 'text',
        'contains', 250, 1, 'asc');

INSERT INTO grid_column(grid_name_id, field, header, data_type, default_filter_operator, width,
                        visible_index)
VALUES ((SELECT id FROM grid_name WHERE name = 'CATEGORIES_GRID'), 'categoryDescription', 'Category Description',
        'text',
        'contains', 250, 2);

INSERT INTO grid_column(grid_name_id, field, header, data_type, default_filter_operator,
                        visible_index, cell_template, align)
VALUES ((SELECT id FROM grid_name WHERE name = 'CATEGORIES_GRID'), 'active', 'Active Status', 'boolean',
        'equals', 3, 'cellValueTemplate', 'CENTER');

INSERT INTO grid_column(grid_name_id, field, header, data_type, default_filter_operator, width,
                        visible_index)
VALUES ((SELECT id FROM grid_name WHERE name = 'CATEGORIES_GRID'), 'createdBy', 'Created By', 'text',
        'contains', 200, 4);

INSERT INTO grid_column(grid_name_id, field, header, data_type, default_filter_operator, width,
                        visible_index)
VALUES ((SELECT id FROM grid_name WHERE name = 'CATEGORIES_GRID'), 'createdAt', 'Created Date', 'datetime',
        'contains', 200, 5);

INSERT INTO grid_column(grid_name_id, field, header, data_type, default_filter_operator, width,
                        visible_index)
VALUES ((SELECT id FROM grid_name WHERE name = 'CATEGORIES_GRID'), 'updatedBy', 'Modified By', 'text',
        'contains', 200, 6);

INSERT INTO grid_column(grid_name_id, field, header, data_type, default_filter_operator, width,
                        visible_index)
VALUES ((SELECT id FROM grid_name WHERE name = 'CATEGORIES_GRID'), 'updatedAt', 'Modified Date', 'datetime',
        'contains', 200, 7);