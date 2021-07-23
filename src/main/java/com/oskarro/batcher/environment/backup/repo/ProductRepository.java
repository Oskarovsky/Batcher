package com.oskarro.batcher.environment.backup.repo;

import com.oskarro.batcher.environment.backup.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Serializable> {

    long count();

    List<Product> findAllByProductType(String productType);

    List<Product> findAllByOrderDate(Date orderDate);

    List<Product> findAllByOrderDateBetween(Date orderDateStart, Date orderDateEnd);



}
