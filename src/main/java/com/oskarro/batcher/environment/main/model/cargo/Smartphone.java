package com.oskarro.batcher.environment.main.model.cargo;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "smartphone")
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Smartphone extends ProductItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "smartphone_id")
    private Integer smartphoneId;

    private String model;

    private String producer;

    private String software;

    private BigDecimal price;

    @Column(name = "product_status")
    private String productStatus;

    @Temporal(TemporalType.DATE)
    @Column(name = "order_date")
    private Date orderDate;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_date")
    private Date createDate;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modify_date")
    private Date modifyDate;
}
