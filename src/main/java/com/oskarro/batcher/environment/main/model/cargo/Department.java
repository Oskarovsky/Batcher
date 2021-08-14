package com.oskarro.batcher.environment.main.model.cargo;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "department")
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Department extends ProductItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "department_id")
    private Integer departmentId;

    private String name;

    private String city;

    @Column(name = "current_balance")
    private BigDecimal currentBalance;

    @OneToMany(mappedBy = "department")
    private List<Computer> computers = new ArrayList<Computer>();
}
