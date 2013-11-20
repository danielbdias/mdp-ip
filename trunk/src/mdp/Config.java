package mdp;

import java.text.DecimalFormat;
import java.util.Properties;

/**
 * Singleton façade to handle the .properties file. 
 */
public class Config {
	
	private final String CONFIG_FILE = "config.properties";
	
	private final String ROOT_DIR_PLACEHOLDER = "{root.dir}";
	
	private Properties propertiesData = null;
	
	/**
	 * Internal constructor. This constructor can be called only by getConfig method.
	 */
	private Config() {
		this.propertiesData = new Properties();
		
		try {
			this.propertiesData.load(new java.io.FileReader(CONFIG_FILE));
		} catch (Exception e) {
			System.err.println("Problems in .properties file loading.");
			System.err.println("Error:");
			System.err.println(e.toString());
			e.printStackTrace(System.err);
			
			System.exit(-1);
		}
	}
	
	/**
	 * Internal instance of this class. This instance must be the only instance of this class.
	 */
	private static Config _instance = null;
	
	/**
	 * Gets an instance of Config handler.
	 * @return An instance of Config handler.
	 */
	public static Config getConfig() {
		if (_instance == null) _instance = new Config();
		return _instance;
	}

	/**
	 * Gets the current format used in application.
	 * @return Current format used in application.
	 */
	public DecimalFormat getFormat() {
		final String configKey = "number.format";
		
		String value = this.getConfigValue(configKey);
		
		return new DecimalFormat(value);
	}

	public String getAmplTempFile() {
		final String configKey = "ampl.tempfile";
		
		String configValue = this.getConfigValue(configKey);
		
		if (configValue.contains(ROOT_DIR_PLACEHOLDER))
			configValue = configValue.replace(ROOT_DIR_PLACEHOLDER, this.getRootDir());
		
		return configValue;
	}
	
	public String getAmplBoundTempFile() {
		final String configKey = "ampl.bound.tempfile";
		
		String configValue = this.getConfigValue(configKey);
		
		if (configValue.contains(ROOT_DIR_PLACEHOLDER))
			configValue = configValue.replace(ROOT_DIR_PLACEHOLDER, this.getRootDir());
		
		return configValue;
	}
	
	public String getAmplConstraintFile() {
		final String configKey = "ampl.constraintfile";
		
		String configValue = this.getConfigValue(configKey);
		
		if (configValue.contains(ROOT_DIR_PLACEHOLDER))
			configValue = configValue.replace(ROOT_DIR_PLACEHOLDER, this.getRootDir());
		
		return configValue;
	}
	
	public String getAmplConstraintFileGreaterZero() {
		final String configKey = "ampl.constraintfileGreaterZero";
		
		String configValue = this.getConfigValue(configKey);
		
		if (configValue.contains(ROOT_DIR_PLACEHOLDER))
			configValue = configValue.replace(ROOT_DIR_PLACEHOLDER, this.getRootDir());
		
		return configValue;
	}

	public String getProblemsDir() {
		final String configKey = "problems.dir";
		
		String configValue = this.getConfigValue(configKey);
		
		if (configValue.contains(ROOT_DIR_PLACEHOLDER))
			configValue = configValue.replace(ROOT_DIR_PLACEHOLDER, this.getRootDir());
		
		return configValue;
	}
	
	public String getReportsDir() {
		final String configKey = "reports.dir";
		
		String configValue = this.getConfigValue(configKey);
		
		if (configValue.contains(ROOT_DIR_PLACEHOLDER))
			configValue = configValue.replace(ROOT_DIR_PLACEHOLDER, this.getRootDir());
		
		return configValue;
	}
	
	public String getOperatingSystemName() {
		final String configKey = "user.os";
		
		return this.getConfigValue(configKey);
	}
	
	public String getRootDir() {
		final String configKey = "root.dir";
		
		return this.getConfigValue(configKey);
	}
	
	public boolean getVerbose() {
		final String configKey = "verbose";
		
		String resultAsString = this.getConfigValue(configKey);
		resultAsString = resultAsString.toLowerCase().trim();
		
		if (resultAsString.equals("true") ||
			resultAsString.equals("1") ||
			resultAsString.equals("yes"))
			return true;
		else
			return false;
	}
	
	private String getConfigValue(String configKey) {
		String value = this.propertiesData.getProperty(configKey);
		
		if (value == null) {
			System.err.printf("Config [%s] not found in properties file.", value);
			System.err.println();
			
			System.exit(-1);
		}
		
		return value;
	}
}
