package company;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import javax.sql.DataSource;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.*;
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

    public List<Long> getIdByName(String name){
        return jdbcTemp.query(
                "SELECT id FROM emp WHERE emp_name = ?", new Object[]{name},
                (rs, i) -> rs.getLong("id"));
    }

    public void addPicture(String empName, String filename, InputStream ins){
        long imgId = getIdByName(empName).get(0) + 1000;
        jdbcTemp.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO images(id, filename, content) VALUES(?,?,?)");
            ps.setLong(1, imgId);
            ps.setString(2, filename);
            Blob blob = connection.createBlob();
            fillBlob( blob, ins );
            ps.setBlob(3, blob);
            return ps;
        });
    }

    private void fillBlob(Blob blob, InputStream isImage){
        try(OutputStream os = blob.setBinaryStream(1);
            BufferedInputStream is = new BufferedInputStream(isImage)
        ){
            is.transferTo( os );
        } catch (SQLException | IOException e) {
            throw new IllegalArgumentException("Error creating blob", e);
        }
    }

}
