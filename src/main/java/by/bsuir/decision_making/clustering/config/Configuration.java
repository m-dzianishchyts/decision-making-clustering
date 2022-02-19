package by.bsuir.decision_making.clustering.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class Configuration {

    public static final String RESOURCES_PATH = "resources.path";

    private static final Logger logger = LogManager.getLogger(Configuration.class);

    private static Configuration instance;

    private final Properties properties;

    private Configuration() {
        properties = loadProperties();
    }

    public static synchronized Configuration getInstance() {
        if (instance == null) {
            instance = new Configuration();
        }
        return instance;
    }

    public Properties getProperties() {
        return properties;
    }

    private Properties loadProperties() {
        Properties loadedProperties = new Properties();
        InputStream propertiesInputStream = getClass().getResourceAsStream("/app.properties");
        if (propertiesInputStream != null) {
            try {
                loadedProperties.load(propertiesInputStream);
            } catch (IOException e) {
                logger.error("I/O error occurred while reading config file.", e);
            }
        } else {
            logger.error("Config file was not found.");
        }
        return loadedProperties;
    }
}