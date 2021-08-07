SELECT department_id, name, current_balance, comp.price
FROM department dep
WHERE dep.department_id IN
      (SELECT DISTINCT comp.department_id FROM computer comp)
ORDER BY dep.department_id