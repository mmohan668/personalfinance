INSERT INTO grid_name(name)
VALUES ('SUBCATEGORIES_GRID');

INSERT INTO grid_column(grid_name_id, field, header, data_type, default_filter_operator, width,
                        visible_index)
VALUES ((SELECT id FROM grid_name WHERE name = 'SUBCATEGORIES_GRID'), 'transactionType', 'Category Type', 'text',
        'contains', 200, 0);

INSERT INTO grid_column(grid_name_id, field, header, data_type, default_filter_operator, width,
                        visible_index, default_sort_order)
VALUES ((SELECT id FROM grid_name WHERE name = 'SUBCATEGORIES_GRID'), 'categoryName', 'Category Name', 'text',
        'contains', 250, 1, 'asc');

INSERT INTO grid_column(grid_name_id, field, header, data_type, default_filter_operator, width,
                        visible_index)
VALUES ((SELECT id FROM grid_name WHERE name = 'SUBCATEGORIES_GRID'), 'categoryDescription', 'Category Description',
        'text',
        'contains', 250, 2);

INSERT INTO grid_column(grid_name_id, field, header, data_type, default_filter_operator, width,
                        visible_index)
VALUES ((SELECT id FROM grid_name WHERE name = 'SUBCATEGORIES_GRID'), 'subcategoryName', 'Subcategory Name', 'text',
        'contains', 250, 3);

INSERT INTO grid_column(grid_name_id, field, header, data_type, default_filter_operator, width,
                        visible_index)
VALUES ((SELECT id FROM grid_name WHERE name = 'SUBCATEGORIES_GRID'), 'subcategoryDescription',
        'Subcategory Description',
        'text',
        'contains', 250, 4);

INSERT INTO grid_column(grid_name_id, field, header, data_type, default_filter_operator,
                        visible_index, cell_template, align)
VALUES ((SELECT id FROM grid_name WHERE name = 'SUBCATEGORIES_GRID'), 'isActive', 'Active Status', 'boolean',
        'equals', 5, 'cellValueTemplate', 'CENTER');

INSERT INTO grid_column(grid_name_id, field, header, data_type, default_filter_operator, width,
                        visible_index)
VALUES ((SELECT id FROM grid_name WHERE name = 'SUBCATEGORIES_GRID'), 'createdBy', 'Created By', 'text',
        'contains', 200, 6);

INSERT INTO grid_column(grid_name_id, field, header, data_type, default_filter_operator, width,
                        visible_index)
VALUES ((SELECT id FROM grid_name WHERE name = 'SUBCATEGORIES_GRID'), 'createdAt', 'Created Date', 'datetime',
        'equals', 200, 7);

INSERT INTO grid_column(grid_name_id, field, header, data_type, default_filter_operator, width,
                        visible_index)
VALUES ((SELECT id FROM grid_name WHERE name = 'SUBCATEGORIES_GRID'), 'updatedBy', 'Modified By', 'text',
        'contains', 200, 8);

INSERT INTO grid_column(grid_name_id, field, header, data_type, default_filter_operator, width,
                        visible_index)
VALUES ((SELECT id FROM grid_name WHERE name = 'SUBCATEGORIES_GRID'), 'updatedAt', 'Modified Date', 'datetime',
        'equals', 200, 9);