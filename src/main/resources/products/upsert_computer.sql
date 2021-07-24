INSERT INTO computer (computer_id, name, description, model, price, product_status, create_date, modify_date)
VALUES(:computerId, :name, :description, :model, :price, 'DRAFT', now(), now())
    ON CONFLICT (computer_id)
DO
UPDATE SET product_status = 'IN-PROGRESS'