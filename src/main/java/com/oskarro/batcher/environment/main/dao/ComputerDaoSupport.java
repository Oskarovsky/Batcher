package com.oskarro.batcher.environment.main.dao;

import com.oskarro.batcher.environment.main.model.cargo.Computer;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

public class ComputerDaoSupport extends JdbcTemplate implements ComputerDao {

    public ComputerDaoSupport(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public List<Computer> getComputersByModel(String model) {
        return query(
                "SELECT comp.computer_id, comp.name, comp.description, comp.model, comp.create_date, comp.price " +
                        "FROM computer comp " +
                        "WHERE comp.model = ?",
                new Object[] { model },
                (rs, rowNum) -> {
                    Computer computer = new Computer();
                    computer.setName(rs.getString("name"));
                    computer.setPrice(rs.getBigDecimal("price"));
                    return computer;
                }
        );
    }

    @Override
    public Computer getComputerById(Integer id) {
        return queryForObject(
                "SELECT comp.computer_id, comp.name, comp.model, comp.price "
                        + "FROM computer comp "
                        + "WHERE comp.computer_id = ?",
                new Object[] { id },
                (rs, rowNum) -> {
                    Computer computer = new Computer();
                    computer.setName(rs.getString("name"));
                    computer.setModel(rs.getString("model"));
                    computer.setPrice(rs.getBigDecimal("price"));
                    return computer;
                }
        );
    }
}
