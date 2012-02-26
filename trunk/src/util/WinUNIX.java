package util;

import java.io.*;

import mdp.Config;

public class WinUNIX {

	public static final int UNDEFINED  = 0;
	public static final int LINUX      = 1;
	public static final int WINDOWS    = 2;
	public static final int MAC        = 3;
	public static final int SERVLET    = 4;
	
	////////////////////////////////////////////////////////////////////////////
	public static int SYSTEM = UNDEFINED;
	////////////////////////////////////////////////////////////////////////////
	
	public static String GVIZ_EXE = null;
	public static String GVIZ_CMD = null;
	public static String GVIZ_CMD_CLOSE = null;

	public static String FACT_EXE = null;
	public static String FACT_CMD = null;
	public static String FACT_CONF_SRC = null;
	
	public static String OTTER_EXE = null;
	public static String OTTER_CMD = null;

	public static String VAMPIRE_EXE = null;
	public static String VAMPIRE_CMD = null;
	
	public static String USER_DIR = System.getProperty("user.dir");
	public static String FILE_SEP = System.getProperty("file.separator");

	static {
		
		String line = Config.getConfig().getOperatingSystemName();
		
		if (line.equalsIgnoreCase("windows")) {
			SYSTEM = WINDOWS;
		} else if (line.equalsIgnoreCase("linux")) {
			SYSTEM = LINUX;
		} else if (line.equalsIgnoreCase("mac")) {
			SYSTEM = MAC;
		} else if (line.equalsIgnoreCase("servlet")) {
			SYSTEM = SERVLET;
		}
		
		if (SYSTEM == WINDOWS) {
			
			GVIZ_EXE = "dot.exe -Tdot";
			GVIZ_CMD = "CMD /C dot.exe -Tdot";
			GVIZ_CMD_CLOSE = "";

			FACT_EXE = "FaCTWin.exe";
			FACT_CMD = "CMD /C FaCTWin.exe";
			FACT_CONF_SRC = "FACT.conf";;
			
			OTTER_EXE = "otter.exe";
			OTTER_CMD = "CMD /C otter.exe <";

			VAMPIRE_EXE = "vampire8.exe";
			VAMPIRE_CMD = "CMD /C vampire8.exe";
		
		} else if (SYSTEM == SERVLET) {
			
			GVIZ_EXE = "C:\\cygwin\\usr\\tomcat55\\dot.exe -Tdot";
			GVIZ_CMD = "CMD /C C:\\cygwin\\usr\\tomcat55\\dot.exe -Tdot";
			GVIZ_CMD_CLOSE = "";

			FACT_EXE = "C:\\cygwin\\usr\\tomcat55\\FACTWin.exe";
			FACT_CMD = "CMD /C C:\\cygwin\\usr\\tomcat55\\FACTWin.exe";
			FACT_CONF_SRC = "C:\\cygwin\\usr\\tomcat55\\FACT.conf";;
			
			OTTER_EXE = "C:\\cygwin\\usr\\tomcat55\\otter.exe";
			OTTER_CMD = "CMD /C C:\\cygwin\\usr\\tomcat55\\otter.exe <";

			VAMPIRE_EXE = "C:\\cygwin\\usr\\tomcat55\\vampire8.exe";
			VAMPIRE_CMD = "CMD /C C:\\cygwin\\usr\\tomcat55\\vampire8.exe";
		
		} else if (SYSTEM == LINUX) {
			
			// Note that this 'dot' is actually a script
			// that calls .libs/lt-dot
			GVIZ_EXE = "dot -Tdot";
			GVIZ_CMD = "/bin/sh 'dot -Tdot";
			GVIZ_CMD_CLOSE = "'";

			// For some reason FaCT++ requires full path
            FACT_EXE = USER_DIR + FILE_SEP + "FaCT++";
            FACT_CMD = USER_DIR + FILE_SEP + "runfact";
            FACT_CONF_SRC = "FACT.conf";
			
			OTTER_EXE = "otter";
			OTTER_CMD = "/bin/sh otter.sh";

			VAMPIRE_EXE = "vampire";
			VAMPIRE_CMD = "/bin/sh vampire";
			
		} else if (SYSTEM == MAC) {
			
			GVIZ_EXE = "dot -Tdot";
			GVIZ_CMD = "/bin/sh 'dot -Tdot";
			GVIZ_CMD_CLOSE = "'";

            FACT_EXE = "FaCT++";
            FACT_CMD = "/bin/sh FaCT++";
            FACT_CONF_SRC = "FACT.conf";
			
			OTTER_EXE = "otter";
			OTTER_CMD = "/bin/sh otter.sh";

			VAMPIRE_EXE = "vampire8";
			VAMPIRE_CMD = "/bin/sh vampire8";
			
		} else {
			System.out.println("WinUNIX: Invalid configuration: the operating system cannot be recognized.");
			System.exit(1);
		}
		
	}

}
