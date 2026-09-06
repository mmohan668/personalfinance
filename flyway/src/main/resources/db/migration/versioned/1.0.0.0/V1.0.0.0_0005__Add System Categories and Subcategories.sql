-- ============================================================
-- BudgetNest - Master Categories & Subcategories Seed
-- ============================================================

BEGIN;


-- ============================================================
-- 1. MASTER CATEGORIES
-- ============================================================

INSERT INTO categories
(transaction_type,
 category_name,
 category_description,
 is_active,
 created_by)
VALUES

-- =========================
-- EXPENSE
-- =========================
('EXPENSE', 'Banking & Finance',
 'Banking fees, financial charges, interest and penalties',
 TRUE, 'SYSTEM'),

('EXPENSE', 'Bills & Utilities',
 'Recurring household utility and communication bills',
 TRUE, 'SYSTEM'),

('EXPENSE', 'Child Expenses',
 'Expenses related to children and their daily needs',
 TRUE, 'SYSTEM'),

('EXPENSE', 'Dining & Food Delivery',
 'Restaurant meals, cafes, fast food and food delivery',
 TRUE, 'SYSTEM'),

('EXPENSE', 'Donations & Contributions',
 'Charitable donations, religious giving, tips and contributions',
 TRUE, 'SYSTEM'),

('EXPENSE', 'Education',
 'Education fees, learning materials, courses and coaching',
 TRUE, 'SYSTEM'),

('EXPENSE', 'Electronics',
 'Electronic devices, accessories, repairs and related expenses',
 TRUE, 'SYSTEM'),

('EXPENSE', 'Entertainment',
 'Entertainment, hobbies, gaming, subscriptions and events',
 TRUE, 'SYSTEM'),

('EXPENSE', 'Food & Groceries',
 'Groceries, fresh food, dairy, meat and household food items',
 TRUE, 'SYSTEM'),

('EXPENSE', 'Healthcare',
 'Medical consultations, medicines, tests and health insurance',
 TRUE, 'SYSTEM'),

('EXPENSE', 'Housing & Household',
 'Home rent, loan payments, maintenance and household expenses',
 TRUE, 'SYSTEM'),

('EXPENSE', 'Insurance',
 'Life insurance and other insurance-related expenses',
 TRUE, 'SYSTEM'),

('EXPENSE', 'Lifestyle Shopping',
 'Clothing, footwear, accessories, jewelry and personal shopping',
 TRUE, 'SYSTEM'),

('EXPENSE', 'Personal Care',
 'Personal grooming, cosmetics, fitness and laundry expenses',
 TRUE, 'SYSTEM'),

('EXPENSE', 'Transportation',
 'Fuel, charging, public transport, parking and vehicle expenses',
 TRUE, 'SYSTEM'),

('EXPENSE', 'Travel & Vacation',
 'Travel accommodation, sightseeing, documents and insurance',
 TRUE, 'SYSTEM'),


-- =========================
-- INCOME
-- =========================
('INCOME', 'Business Income',
 'Income received from business activities',
 TRUE, 'SYSTEM'),

('INCOME', 'Freelance & Contract Work',
 'Income received from freelance and contract work',
 TRUE, 'SYSTEM'),

('INCOME', 'Gifts & Windfalls',
 'Cash gifts, prize money and other unexpected receipts',
 TRUE, 'SYSTEM'),

('INCOME', 'Government Benefits',
 'Pensions, tax refunds and other government benefits',
 TRUE, 'SYSTEM'),

('INCOME', 'Investment Income',
 'Income generated from investments and financial assets',
 TRUE, 'SYSTEM'),

('INCOME', 'Other Income',
 'Income that does not fit into another income category',
 TRUE, 'SYSTEM'),

('INCOME', 'Rental Income',
 'Income received from rental properties',
 TRUE, 'SYSTEM'),

('INCOME', 'Salary & Employment',
 'Salary, bonuses and other employment-related income',
 TRUE, 'SYSTEM'),


-- =========================
-- TRANSFER
-- =========================
('TRANSFER', 'Transfer',
 'Movement of money between people or accounts',
 TRUE, 'SYSTEM'),


-- =========================
-- INVESTMENT
-- =========================
('INVESTMENT', 'Investments',
 'Money allocated to investments and long-term financial assets',
 TRUE, 'SYSTEM')


ON CONFLICT (transaction_type, category_name)
    DO NOTHING;


-- ============================================================
-- 2. MASTER SUBCATEGORIES
-- ============================================================

INSERT INTO subcategories
(category_id,
 subcategory_name,
 subcategory_description,
 is_active,
 created_by)
SELECT c.id,
       s.subcategory_name,
       s.subcategory_description,
       TRUE,
       'SYSTEM'
FROM (VALUES

          -- ========================================================
          -- EXPENSE
          -- ========================================================

          -- Banking & Finance
          ('EXPENSE', 'Banking & Finance',
           'Fees', 'Banking and financial service fees'),

          ('EXPENSE', 'Banking & Finance',
           'Interest & Finance Charges', 'Interest payments and other finance charges'),

          ('EXPENSE', 'Banking & Finance',
           'Miscellaneous', 'Other banking and finance-related expenses'),

          ('EXPENSE', 'Banking & Finance',
           'Penalties', 'Financial penalties, fines and related charges'),


          -- Bills & Utilities
          ('EXPENSE', 'Bills & Utilities',
           'DTH / Cable TV', 'DTH, cable television and related subscription bills'),

          ('EXPENSE', 'Bills & Utilities',
           'Electricity Bill', 'Household electricity and power bills'),

          ('EXPENSE', 'Bills & Utilities',
           'Internet / Broadband', 'Internet and broadband service bills'),

          ('EXPENSE', 'Bills & Utilities',
           'LPG Cylinder', 'LPG cylinder purchase and related charges'),

          ('EXPENSE', 'Bills & Utilities',
           'Miscellaneous', 'Other household utility expenses'),

          ('EXPENSE', 'Bills & Utilities',
           'Mobile Recharge', 'Mobile phone recharge and prepaid services'),

          ('EXPENSE', 'Bills & Utilities',
           'Water Bill', 'Household water supply bills'),


          -- Child Expenses
          ('EXPENSE', 'Child Expenses',
           'Baby Care', 'Baby care products and daily care expenses'),

          ('EXPENSE', 'Child Expenses',
           'Baby Food', 'Food and nutritional products for babies'),

          ('EXPENSE', 'Child Expenses',
           'Daycare', 'Daycare and child supervision expenses'),

          ('EXPENSE', 'Child Expenses',
           'Miscellaneous', 'Other child-related expenses'),

          ('EXPENSE', 'Child Expenses',
           'Pocket Money', 'Pocket money given to children'),

          ('EXPENSE', 'Child Expenses',
           'Toys', 'Toys, games and play items for children'),


          -- Dining & Food Delivery
          ('EXPENSE', 'Dining & Food Delivery',
           'Cafe', 'Food and beverages purchased from cafes'),

          ('EXPENSE', 'Dining & Food Delivery',
           'Family Outing Meals', 'Meals purchased during family outings'),

          ('EXPENSE', 'Dining & Food Delivery',
           'Food Delivery', 'Food ordered through delivery services'),

          ('EXPENSE', 'Dining & Food Delivery',
           'Miscellaneous', 'Other dining and food service expenses'),

          ('EXPENSE', 'Dining & Food Delivery',
           'Office Lunch', 'Lunch purchased during office or work hours'),

          ('EXPENSE', 'Dining & Food Delivery',
           'Restaurant', 'Meals and beverages purchased at restaurants'),

          ('EXPENSE', 'Dining & Food Delivery',
           'Street / Fast Food', 'Street food, snacks and fast food purchases'),


          -- Donations & Contributions
          ('EXPENSE', 'Donations & Contributions',
           'Charity', 'Donations made to charitable causes'),

          ('EXPENSE', 'Donations & Contributions',
           'Miscellaneous', 'Other donation and contribution expenses'),

          ('EXPENSE', 'Donations & Contributions',
           'Religious Donations', 'Donations made to religious institutions or causes'),

          ('EXPENSE', 'Donations & Contributions',
           'Tips & Gratitude', 'Tips, appreciation payments and gratitude contributions'),


          -- Education
          ('EXPENSE', 'Education',
           'Books & Stationery', 'Educational books, notebooks and stationery supplies'),

          ('EXPENSE', 'Education',
           'Education Fees', 'School, college and other education fees'),

          ('EXPENSE', 'Education',
           'Miscellaneous', 'Other education-related expenses'),

          ('EXPENSE', 'Education',
           'Online Courses', 'Online courses and digital learning programs'),

          ('EXPENSE', 'Education',
           'School Transport', 'Transportation costs for school or education'),

          ('EXPENSE', 'Education',
           'Tuition / Coaching', 'Private tuition, coaching and training fees'),

          ('EXPENSE', 'Education',
           'Uniform & Shoes', 'School uniforms and education-related footwear'),


          -- Electronics
          ('EXPENSE', 'Electronics',
           'Accessories', 'Electronic device accessories and peripherals'),

          ('EXPENSE', 'Electronics',
           'Computers & Smart Devices', 'Computers, tablets and smart electronic devices'),

          ('EXPENSE', 'Electronics',
           'Miscellaneous', 'Other electronics-related expenses'),

          ('EXPENSE', 'Electronics',
           'Mobile Phone', 'Mobile phone purchases and related expenses'),

          ('EXPENSE', 'Electronics',
           'Repairs & Maintenance', 'Electronic device repair and maintenance expenses'),


          -- Entertainment
          ('EXPENSE', 'Entertainment',
           'Books & Magazines', 'Books, magazines and other reading entertainment'),

          ('EXPENSE', 'Entertainment',
           'Gaming', 'Video games, gaming purchases and related expenses'),

          ('EXPENSE', 'Entertainment',
           'Hobbies', 'Hobby materials, activities and related expenses'),

          ('EXPENSE', 'Entertainment',
           'Miscellaneous', 'Other entertainment expenses'),

          ('EXPENSE', 'Entertainment',
           'Movie Tickets', 'Cinema and movie ticket purchases'),

          ('EXPENSE', 'Entertainment',
           'Music Subscription', 'Paid music streaming and subscription services'),

          ('EXPENSE', 'Entertainment',
           'OTT Subscription', 'Video streaming and OTT subscription services'),

          ('EXPENSE', 'Entertainment',
           'Sports Events', 'Tickets and expenses for sports events'),


          -- Food & Groceries
          ('EXPENSE', 'Food & Groceries',
           'Drinking Water', 'Packaged drinking water and water-related purchases'),

          ('EXPENSE', 'Food & Groceries',
           'Fruits', 'Fresh and packaged fruit purchases'),

          ('EXPENSE', 'Food & Groceries',
           'Groceries', 'General household grocery purchases'),

          ('EXPENSE', 'Food & Groceries',
           'Meat / Fish / Eggs', 'Meat, fish, seafood and egg purchases'),

          ('EXPENSE', 'Food & Groceries',
           'Milk & Dairy', 'Milk, dairy products and related purchases'),

          ('EXPENSE', 'Food & Groceries',
           'Miscellaneous', 'Other food and grocery expenses'),

          ('EXPENSE', 'Food & Groceries',
           'Vegetables', 'Fresh and packaged vegetable purchases'),


          -- Healthcare
          ('EXPENSE', 'Healthcare',
           'Doctor Consultation', 'Doctor consultation and medical professional fees'),

          ('EXPENSE', 'Healthcare',
           'Health Insurance', 'Health insurance premiums and related payments'),

          ('EXPENSE', 'Healthcare',
           'Hospitalization', 'Hospital admission, treatment and related expenses'),

          ('EXPENSE', 'Healthcare',
           'Medical Tests', 'Diagnostic tests, scans and laboratory expenses'),

          ('EXPENSE', 'Healthcare',
           'Medicines', 'Prescription and over-the-counter medicines'),

          ('EXPENSE', 'Healthcare',
           'Miscellaneous', 'Other healthcare-related expenses'),


          -- Housing & Household
          ('EXPENSE', 'Housing & Household',
           'Home Loan EMI', 'Monthly home loan installment payments'),

          ('EXPENSE', 'Housing & Household',
           'House Rent', 'Residential house or apartment rent payments'),

          ('EXPENSE', 'Housing & Household',
           'Maintenance Charges', 'Home, apartment and property maintenance charges'),

          ('EXPENSE', 'Housing & Household',
           'Miscellaneous', 'Other housing and household expenses'),


          -- Insurance
          ('EXPENSE', 'Insurance',
           'Life & Term Insurance', 'Life insurance and term insurance premiums'),

          ('EXPENSE', 'Insurance',
           'Miscellaneous', 'Other insurance-related expenses'),


          -- Lifestyle Shopping
          ('EXPENSE', 'Lifestyle Shopping',
           'Accessories & Fancy Items', 'Fashion accessories and decorative lifestyle items'),

          ('EXPENSE', 'Lifestyle Shopping',
           'Bags & Wallets', 'Bags, wallets, purses and related accessories'),

          ('EXPENSE', 'Lifestyle Shopping',
           'Clothes', 'Clothing and apparel purchases'),

          ('EXPENSE', 'Lifestyle Shopping',
           'Footwear', 'Shoes, sandals and other footwear purchases'),

          ('EXPENSE', 'Lifestyle Shopping',
           'Jewelry', 'Jewelry and related fashion accessories'),

          ('EXPENSE', 'Lifestyle Shopping',
           'Miscellaneous', 'Other lifestyle shopping expenses'),

          ('EXPENSE', 'Lifestyle Shopping',
           'Tailoring', 'Tailoring, alteration and stitching expenses'),

          ('EXPENSE', 'Lifestyle Shopping',
           'Watches', 'Watches and related accessories'),


          -- Personal Care
          ('EXPENSE', 'Personal Care',
           'Cosmetics & Skincare', 'Cosmetics, skincare and personal beauty products'),

          ('EXPENSE', 'Personal Care',
           'Gym & Fitness', 'Gym memberships, fitness programs and equipment'),

          ('EXPENSE', 'Personal Care',
           'Haircut & Salon', 'Haircuts, salon services and personal grooming'),

          ('EXPENSE', 'Personal Care',
           'Laundry & Dry Cleaning', 'Laundry and professional dry cleaning services'),

          ('EXPENSE', 'Personal Care',
           'Miscellaneous', 'Other personal care expenses'),

          ('EXPENSE', 'Personal Care',
           'Shaving & Grooming', 'Shaving products and personal grooming supplies'),


          -- Transportation
          ('EXPENSE', 'Transportation',
           'EV Charging', 'Electric vehicle charging expenses'),

          ('EXPENSE', 'Transportation',
           'Fuel', 'Petrol, diesel and other vehicle fuel expenses'),

          ('EXPENSE', 'Transportation',
           'Miscellaneous', 'Other transportation-related expenses'),

          ('EXPENSE', 'Transportation',
           'Parking Charges', 'Parking fees and parking-related charges'),

          ('EXPENSE', 'Transportation',
           'Public Transport', 'Bus, train, metro and other public transport fares'),

          ('EXPENSE', 'Transportation',
           'Toll Charges', 'Highway, road and bridge toll payments'),

          ('EXPENSE', 'Transportation',
           'Vehicle EMI', 'Monthly vehicle loan installment payments'),

          ('EXPENSE', 'Transportation',
           'Vehicle Insurance', 'Vehicle insurance premiums and related payments'),

          ('EXPENSE', 'Transportation',
           'Vehicle Maintenance', 'Vehicle servicing, repairs and maintenance'),


          -- Travel & Vacation
          ('EXPENSE', 'Travel & Vacation',
           'Accommodation & Sightseeing', 'Hotels, accommodation, tours and sightseeing expenses'),

          ('EXPENSE', 'Travel & Vacation',
           'Miscellaneous', 'Other travel and vacation expenses'),

          ('EXPENSE', 'Travel & Vacation',
           'Travel Documents & Insurance', 'Passports, visas, travel documents and travel insurance'),


          -- ========================================================
          -- INCOME
          -- ========================================================

          -- Business Income
          ('INCOME', 'Business Income',
           'Business Income', 'Income received from business activities'),

          ('INCOME', 'Business Income',
           'Miscellaneous', 'Other business-related income'),


          -- Freelance & Contract Work
          ('INCOME', 'Freelance & Contract Work',
           'Freelance Income', 'Income received from freelance work'),

          ('INCOME', 'Freelance & Contract Work',
           'Miscellaneous', 'Other freelance and contract-related income'),


          -- Gifts & Windfalls
          ('INCOME', 'Gifts & Windfalls',
           'Cash Gift', 'Money received as a cash gift'),

          ('INCOME', 'Gifts & Windfalls',
           'Miscellaneous', 'Other gifts and unexpected receipts'),

          ('INCOME', 'Gifts & Windfalls',
           'Prize Money', 'Money received as prize or award winnings'),


          -- Government Benefits
          ('INCOME', 'Government Benefits',
           'Pension', 'Pension payments received from government sources'),

          ('INCOME', 'Government Benefits',
           'Tax Refund', 'Tax refunds received from government authorities'),

          ('INCOME', 'Government Benefits',
           'Miscellaneous', 'Other government benefits received'),


          -- Investment Income
          ('INCOME', 'Investment Income',
           'Capital Gains', 'Income from gains on investments'),

          ('INCOME', 'Investment Income',
           'Dividends', 'Dividend income received from investments'),

          ('INCOME', 'Investment Income',
           'Miscellaneous', 'Other investment-related income'),

          ('INCOME', 'Investment Income',
           'Savings / RD / FD Interest', 'Interest earned from savings, RD and FD accounts'),


          -- Other Income
          ('INCOME', 'Other Income',
           'Miscellaneous', 'Other miscellaneous income'),

          ('INCOME', 'Other Income',
           'Other Income', 'Income not classified under another category'),


          -- Rental Income
          ('INCOME', 'Rental Income',
           'House Rent Received', 'Rental income received from residential property'),

          ('INCOME', 'Rental Income',
           'Miscellaneous', 'Other rental-related income'),


          -- Salary & Employment
          ('INCOME', 'Salary & Employment',
           'Bonus', 'Bonus and incentive payments received from employment'),

          ('INCOME', 'Salary & Employment',
           'Miscellaneous', 'Other employment-related income'),

          ('INCOME', 'Salary & Employment',
           'Salary', 'Regular salary received from employment'),


          -- ========================================================
          -- TRANSFER
          -- ========================================================

          ('TRANSFER', 'Transfer',
           'Family Transfer', 'Money transferred to or received from family members'),

          ('TRANSFER', 'Transfer',
           'Miscellaneous', 'Other money transfer transactions'),


          -- ========================================================
          -- INVESTMENT
          -- ========================================================

          ('INVESTMENT', 'Investments',
           'Fixed / Recurring Deposit',
           'Fixed deposits and recurring deposits'),

          ('INVESTMENT', 'Investments',
           'Gold',
           'Gold purchases made as an investment'),

          ('INVESTMENT', 'Investments',
           'Life Insurance',
           'Life insurance policies treated as investments'),

          ('INVESTMENT', 'Investments',
           'Miscellaneous',
           'Other investment-related transactions'),

          ('INVESTMENT', 'Investments',
           'Mutual Funds',
           'Mutual fund investment contributions'),

          ('INVESTMENT', 'Investments',
           'National Pension System (NPS)',
           'Contributions to the National Pension System'),

          ('INVESTMENT', 'Investments',
           'Public Provident Fund (PPF)',
           'Contributions to the Public Provident Fund'),

          ('INVESTMENT', 'Investments',
           'Stocks / Shares',
           'Stock and share market investments')) AS s
         (
          transaction_type,
          category_name,
          subcategory_name,
          subcategory_description
             )
         JOIN categories c
              ON c.transaction_type = s.transaction_type
                  AND c.category_name = s.category_name

ON CONFLICT (category_id, subcategory_name)
    DO NOTHING;


COMMIT;