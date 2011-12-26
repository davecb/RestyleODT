/**
 * ReadMapFile -- simplistic config file reader.
 */
import java.io.*;
import java.util.*;       /* For Lists. */
import java.util.regex.*; /* For Matcher. */


public class ReadMapFile {
    private static final String pre = "ReadMapFile: ";
    private static final String rcsVersion = "$Id: ReadMapFile.java,v 1.2 2011/12/24 22:03:55 user Exp $";
    private static final double version = 0.9;
    private HashMap<String,String> map = new HashMap<String,String>();
    private String mapFile = null;
    private Pattern eqPattern = Pattern.compile("([-_A-Za-z0-9:]+)=(.+)");
    private Pattern tagPattern = Pattern.compile("<([^>]+)>");
    private boolean debug = false;
    
   /**
    * Read a map file, ignoring any comments, reading specific
    * stanzas into like-named maps
    */
    boolean readMapFile(String fileName, String type,
				HashMap<String,String> map, boolean debug) {
	this.debug = debug;
	String line = null;
	Matcher m = null, n = null;
	boolean in = false;
	try {
		if (fileName == null) {
			dl("filename was null");
			return false;
		}
		BufferedReader f = new BufferedReader(
			new FileReader(fileName));
		dl("reading config file.");
		while ((line = f.readLine()) != null) {
			dl("read: " + line);
			m = tagPattern.matcher(line);
			if (m.find() ) {
				if (m.group(1).equals(type)) {
					in = true;
				}
				else if (m.group(1).equals("/"+type)) {
					in = false;
				}
				continue;
			}

			if (in == true) {
				n = eqPattern.matcher(line);
				if (n.find()) {
					map.put(n.group(1), n.group(2));
					dl("set '" + n.group(1) + "'='" 
						+ n.group(2) + "'");
				}
			}
		}
		f.close();
		return true;
	} catch (FileNotFoundException e) {
		return false;
	} catch (Exception e) {
		throw new RuntimeException(e);
	}
    }

    void p(String s) {
    	System.out.print(s);
    }
    void pl(String s) {
    	System.out.println(s);
    }
    void e(String s) {
    	System.err.print(s);
    }
    void el(String s) {
    	System.err.println(s);
    }
    // And my.debug functions 
    void d(String s) {
	if (debug) {
    		System.out.print(s);
	}
    }
    void dl(String s) {
	if (debug) {
		System.out.println(s);
	}
    }
}
