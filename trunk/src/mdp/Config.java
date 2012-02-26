package mdp;

import java.text.DecimalFormat;
import java.util.Properties;

/**
 * Singleton façade to handle the .properties file. 
 */
public class Config {
	
	private final String CONFIG_FILE = "config.properties";
	
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
		
		return this.getConfigValue(configKey);
	}
	
	public String getAmplBoundTempFile() {
		final String configKey = "ampl.bound.tempfile";
		
		return this.getConfigValue(configKey);
	}
	
	public String getAmplConstraintFile() {
		final String configKey = "ampl.constraintfile";
		
		return this.getConfigValue(configKey);
	}
	
	public String getProblemsDir() {
		final String configKey = "problems.dir";
		
		return this.getConfigValue(configKey);
	}
	
	public String getReportsDir() {
		final String configKey = "reports.dir";
		
		return this.getConfigValue(configKey);
	}
	
	public String getOperatingSystemName() {
		final String configKey = "user.os";
		
		return this.getConfigValue(configKey);
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
