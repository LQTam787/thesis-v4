-- System Foods Insert Script
-- These foods have user_id = NULL, making them available to all users

INSERT INTO foods (name, image, meal_type, calories, created_at, user_id) VALUES
('Oatmeal', NULL, 'BREAKFAST', 150, NOW(), NULL),
('Scrambled Eggs', NULL, 'BREAKFAST', 180, NOW(), NULL),
('Whole Wheat Toast', NULL, 'BREAKFAST', 80, NOW(), NULL),
('Grilled Chicken Breast', NULL, 'LUNCH', 165, NOW(), NULL),
('Brown Rice', NULL, 'LUNCH', 215, NOW(), NULL),
('Garden Salad', NULL, 'LUNCH', 50, NOW(), NULL),
('Grilled Salmon', NULL, 'DINNER', 280, NOW(), NULL),
('Steamed Broccoli', NULL, 'DINNER', 55, NOW(), NULL),
('Baked Potato', NULL, 'DINNER', 160, NOW(), NULL),
('Banana', NULL, 'SNACKS', 105, NOW(), NULL);
