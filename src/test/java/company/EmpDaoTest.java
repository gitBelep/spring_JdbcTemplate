package company;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
// @SpringbootTest
@ContextConfiguration(classes = Config.class)
public class EmpDaoTest {
    @Autowired
    private EmpDao dao;

    @Autowired
    private Flyway flyway;

    @BeforeEach
    void setUp() {
        flyway.clean();
        flyway.migrate();

        dao.createEmployee("Első");
        dao.createEmployee("Második");
        dao.createEmployee("Harmadik");

        Employee e1 = new Employee("Negyvennégy", 44);
        Employee e2 = new Employee("Ötödik", 35);
        Employee e3 = new Employee("Hatodik", 26);
        Employee e88 = new Employee("Nyócvannyóc", 88);
        dao.saveEmployee(e1);
        dao.saveEmployee(e2);
        dao.saveEmployee(e3);
        dao.saveEmployee(e88);
    }

    @Test
    void testListEmployeeNames() {
        List<String> names = dao.listEmployeeNames();

        assertEquals(7, names.size());
        assertEquals("Első", names.get(0));     //abc
        assertEquals("Harmadik", names.get(1));
        assertEquals("Hatodik", names.get(2));
    }

    @Test
    void testListEmployeesByAge() {
        List<Employee> emps = dao.listEmployeesByAge(20);
        List<Employee> notJungs = dao.listEmployeesByAge(41);

        assertEquals(4, emps.size());
        assertEquals(26, emps.get(0).getAge());
        assertEquals(35, emps.get(1).getAge());
        assertEquals("Nyócvannyóc", emps.get(3).getName());

        assertEquals(2, notJungs.size());
        assertEquals(44, notJungs.get(0).getAge());
        assertEquals(88, notJungs.get(1).getAge());
        assertEquals("Nyócvannyóc", notJungs.get(1).getName());
    }

    @Test
    void testGetIdAndFindEmployeeNameById(){
        long idOfEmp = dao.createEmployeeAndGetId("Árvíztűrő");

        assertEquals(8L, idOfEmp);
        assertEquals("Árvíztűrő", dao.findEmployeeNameById( idOfEmp ));
    }

    @Test
    void testUpdateAge(){
        dao.updateEmpAge("Első", 31);
        dao.updateEmpAge("Ötödik", 31);
        dao.updateEmpAge("Harmadik", 31);
        List<Employee> emps = dao.listEmployeesByAge(30);

        //Első31 Harmadik31 Ötödik31 Negyvennégy44 Nyócvannyóc88
        assertEquals(5, emps.size());
        assertEquals(31, emps.get(0).getAge());
        assertEquals(31, emps.get(2).getAge());
        assertEquals("Negyvennégy", emps.get(3).getName());
    }

}