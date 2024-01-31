package at.jku.dke.task_app.binary_search;

import at.jku.dke.etutor.task_app.auth.AuthConstants;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures the client API keys.
 */
public class ClientSetupExtension implements BeforeAllCallback {

    private static final Logger LOG = LoggerFactory.getLogger(ClientSetupExtension.class);

    public static final String CRUD_API_KEY = "crud-api-key";
    public static final String SUBMIT_API_KEY = "submit-api-key";
    public static final String READ_API_KEY = "read-api-key";

    /**
     * Creates a new instance of class {@link ClientSetupExtension}.
     */
    public ClientSetupExtension() {
    }

    /**
     * Starts the database container and updates the database connection properties.
     *
     * @param extensionContext The extension context.
     */
    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        LOG.info("Configuring API keys ...");

        System.setProperty("clients.api-keys[0].name", "task-administration");
        System.setProperty("clients.api-keys[0].key", CRUD_API_KEY);
        System.setProperty("clients.api-keys[0].roles[0]", AuthConstants.CRUD);

        System.setProperty("clients.api-keys[1].name", "moodle");
        System.setProperty("clients.api-keys[1].key", SUBMIT_API_KEY);
        System.setProperty("clients.api-keys[1].roles[0]", AuthConstants.SUBMIT);

        System.setProperty("clients.api-keys[2].name", "moodle");
        System.setProperty("clients.api-keys[2].key", READ_API_KEY);
        System.setProperty("clients.api-keys[2].roles[0]", AuthConstants.READ_SUBMISSION);
    }

}
