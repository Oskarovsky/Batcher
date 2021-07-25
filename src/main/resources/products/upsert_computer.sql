INSERT INTO computer (computer_id, name, description, model, price, product_status, create_date, modify_date, department_id)
VALUES(:computerId, :name, :description, :model, :price, 'DRAFT', now(), now(), :department.departmentId)
    ON CONFLICT (computer_id)
DO
UPDATE SET product_status = :productStatus, modify_date = now()