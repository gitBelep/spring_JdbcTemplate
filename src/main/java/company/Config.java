package company;

import org.flywaydb.core.Flyway;
import org.mariadb.jdbc.MariaDbDataSource;
import org.springframework.context.annotation.Bean;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

public class Config {

    @Bean
    public DataSource getDataSource(){
        try (InputStream is = EmpDao.class.getResourceAsStream("/company.properties")) {
            Properties pr = new Properties();
            pr.load(is);
            MariaDbDataSource ds = new MariaDbDataSource();
            ds.setUrl(pr.getProperty("Url"));
            ds.setUser(pr.getProperty("User"));
            ds.setPassword(pr.getProperty("Password"));
            return ds;
        } catch (IOException | SQLException e) {
            throw new IllegalStateException("Cannot read company.properties", e);
        }
    }

    @Bean
    public EmpDao empDao(){
        return new EmpDao( getDataSource() );
    }

    @Bean
    public Flyway flyway(){
        Flyway f = Flyway.configure()
                .locations("/db/migration/company")
                .dataSource( getDataSource() ).load();
        return f;
    }

}
