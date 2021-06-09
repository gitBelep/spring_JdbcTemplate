package company;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class EmpDao {
    private JdbcTemplate jdbcTemp;

    public EmpDao(DataSource ds) {
        jdbcTemp = new JdbcTemplate(ds);
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemp;
    }

    public void createEmployee(String name) {
        jdbcTemp.update("INSERT INTO emp(emp_name) VALUES(?)", name);
    }

    public void saveEmployee(Employee emp) {
        jdbcTemp.update("INSERT INTO emp(emp_name, age) VALUES(?, ?)", emp.getName(), emp.getAge());
    }

    public long createEmployeeAndGetId(String name){
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemp.update( connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO emp(emp_name) VALUES(?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public List<String> listEmployeeNames() {
        return jdbcTemp.query(
                "SELECT emp_name FROM emp ORDER BY emp_name",
                (rs, rowNum) -> rs.getString("emp_name"));
    }

    public List<Employee> listEmployeesByAge(int criteria) {
        return jdbcTemp.query("SELECT emp_name, age FROM emp WHERE age > ? ORDER BY age",
            new RowMapper<Employee>() {
                @Override
                public Employee mapRow(ResultSet rs, int rowNum) throws SQLException {
                    String name = rs.getString("emp_name");
                    Employee result = new Employee(name);
                    int age = rs.getInt("age");
                    result.setAge(age);
                    return result;
                }
            }, criteria);
    }

    public String findEmployeeNameById(long id){
        return jdbcTemp.queryForObject(
                "SELECT emp_name FROM emp WHERE id = ?",
                new Object[]{id},         //1 elemű Array az 1 db "?" helyére
                (rs, i) -> rs.getString("emp_name")
        );
    }

    public void updateEmpAge(String name, int age){
        jdbcTemp.update("UPDATE emp SET age = ? WHERE emp_name = ?;", age, name);
    }

}
