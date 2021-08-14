INSERT INTO department (department_id, name, city, current_balance)
VALUES(:departmentId, :name, :city, :currentBalance)
    ON CONFLICT (department_id)
DO
UPDATE SET current_balance = :currentBalance