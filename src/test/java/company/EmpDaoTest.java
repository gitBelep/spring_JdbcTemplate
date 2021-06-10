package company;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
// import org.springframework.boot.test.context.SpringBootTest;

// @SpringbootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = Config.class)
class EmpDaoTest {
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

    @Test
    void testGetIdByName(){
        assertEquals(2L, dao.getIdByName("Második"));
        assertThrows(IllegalStateException.class, () -> dao.getIdByName("Non Existing Name"));
    }

    @Test
    void testAddPicture() throws IOException, SQLException {
        String inputFile = "Qtya.gif";

        InputStream ins1 = Files.newInputStream(Path.of("c:","training","sprJdbcTemplate","src","main","resources",inputFile));
        dao.addPicture("Ötödik", inputFile, ins1);
        InputStream ins2 = Files.newInputStream(Path.of("c:","training","sprJdbcTemplate","src","main","resources",inputFile));
        dao.addPicture("Második", inputFile, ins2);

        //read the image again for making an Array
        InputStream ins3 = Files.newInputStream(Path.of("c:","training","sprJdbcTemplate","src","main","resources",inputFile));
        byte[] insArr = new byte[20];
        ins3.read(insArr);

        //read images of emp2 & emp5 from DB
        InputStream empImg1 = dao.getPicture("Második");
        byte[] empArr1 = new byte[20];
        empImg1.read(empArr1);
        InputStream empImg2 = dao.getPicture("Ötödik");
        byte[] empArr2 = new byte[20];
        empImg2.read(empArr2);

        assertArrayEquals(empArr1, insArr);
        assertArrayEquals(empArr2, insArr);
        assertThrows(IllegalStateException.class, () -> dao.getPicture("Non Existing Name"));
    }

    @Test
    void testListEmpBetweenIds(){
        List<Employee> result = dao.listEmployeesBetweenIds(2,6); //BETWEEN 2 AND 6 = {2,3,4,5,6}
        assertEquals(5, result.size());
        for(Employee e : result){
            System.out.println(e.getName());
        }

    }

}