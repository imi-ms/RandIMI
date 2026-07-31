package de.unimuenster.imi.randimi.migration;

import de.unimuenster.imi.randimi.RandimiIntegrationTest;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public abstract class RandimiMigrationTest extends RandimiIntegrationTest {

	@Value("${spring.datasource.url}")
	private String databaseUrl;
	@Value("${spring.datasource.username}")
	private String databaseUser;
	@Value("${spring.datasource.password}")
	private String databasePassword;

	@Value("${spring.flyway.locations}")
	private String[] flywayLocations;

	protected String targetVersion = "latest";

	@BeforeEach
	public void setUp() {
		final Flyway flyway = configureFlyway();
		flyway.clean();
		flyway.migrate();
	}

	@AfterAll
	public static void tearDown(@Autowired Flyway flyway) {
		flyway.clean();
		flyway.migrate();
	}

	private Flyway configureFlyway() {
		var locations = new String[]{flywayLocations[0], flywayLocations[1]};
		return Flyway.configure()
		             .target(targetVersion)
		             .cleanDisabled(false)
		             .locations(locations)
		             .dataSource(databaseUrl, databaseUser, databasePassword)
		             .load();
	}

}
