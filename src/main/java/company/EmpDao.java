package company;

import org.springframework.dao.EmptyResultDataAccessException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EmpDao {
    private final JdbcTemplate jdbcTemp;


    public EmpDao(DataSource ds) {
        jdbcTemp = new JdbcTemplate(ds);
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
                new Object[]{id}, String.class);
    }

    public void updateEmpAge(String name, int age){
        jdbcTemp.update("UPDATE emp SET age = ? WHERE emp_name = ?;", age, name);
    }

    public Long getIdByName(String name){
        try{
        return jdbcTemp.queryForObject(
                    "SELECT id FROM emp WHERE emp_name = ?",
                    new Object[]{name}, Long.class);
        } catch (EmptyResultDataAccessException e){
            throw new IllegalStateException("No such name", e);
        }
    }

    public void addPicture(String empName, String filename, InputStream ins){
        long imgId = getIdByName(empName) + 1000;
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

    public InputStream getPicture(String name) throws SQLException {
        Long empId = getIdByName(name);
        List<Blob> blobs = jdbcTemp.query(
                "SELECT content FROM images WHERE (id - 1000) = ?;",
                new Object[]{empId},
                (rs, i) -> rs.getBlob("content"));
        return blobs.get(0).getBinaryStream();
    }

    public List<Employee> listEmployeesBetweenIds(int min, int max){
        String query = "SELECT emp_name, age FROM emp WHERE id BETWEEN ? AND ?;";
        List<Map<String, Object>> empData =
            jdbcTemp.queryForList(query, new Object[]{min, max});

        List<Employee> employees = new ArrayList<>();
        for(Map<String, Object> m : empData){
            Employee e = new Employee( m.get("emp_name").toString() );
            if( m.get("age") != null ) {
                e.setAge( (int) m.get("age"));
            }
            employees.add(e);
        }
        return employees;
    }

}
