package at.jku.dke.task_app.clustering;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

/**
 * A PostgreSQL container for use in unit tests.
 */
class AppPostgresContainer extends PostgreSQLContainer<AppPostgresContainer> {
    /**
     * The docker image version of the test database container.
     */
    public static final String IMAGE_VERSION = "postgres:16";

    /**
     * The docker image name of the test database container.
     */
    public static final DockerImageName IMAGE_NAME = DockerImageName.parse(IMAGE_VERSION);

    public static final String DATABASE_NAME = "test_db";
    public static final String USERNAME = "etutor_clustering_test_admin";
    public static final String PASSWORD = "strong-password";
    public static final String ETUTOR_USERNAME = "etutor_clustering_test";
    public static final String ETUTOR_PASSWORD = "etutor_clustering_pwd";

    /**
     * The singleton instance of the test database container.
     */
    public static final AppPostgresContainer INSTANCE = new AppPostgresContainer()
        .withDatabaseName(DATABASE_NAME)
        .withUsername(USERNAME)
        .withPassword(PASSWORD)
        .withCopyToContainer(Transferable.of(String.format("""
                #!/bin/bash
                set -e
                POSTGRES="psql -v ON_ERROR_STOP=1 --username %s --dbname %s"

                $POSTGRES <<-EOSQL
                CREATE USER %s WITH CREATEDB PASSWORD '%s';
                GRANT SELECT, INSERT, UPDATE, DELETE, TRUNCATE ON ALL TABLES IN SCHEMA public TO %s;
                ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE, TRUNCATE ON TABLES TO %s;
                GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA public TO %s;
                EOSQL
                """, USERNAME, DATABASE_NAME, ETUTOR_USERNAME, ETUTOR_PASSWORD, ETUTOR_USERNAME, ETUTOR_USERNAME, ETUTOR_USERNAME)),
            "/docker-entrypoint-initdb.d/900-create_user.sh");

    /**
     * Creates a new instance of class {@link AppPostgresContainer}.
     */
    private AppPostgresContainer() {
        super(IMAGE_NAME);
    }
}
