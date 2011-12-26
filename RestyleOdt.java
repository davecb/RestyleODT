/**
 * Restyle ODT -- load and apply transforms to an odt document,
 * 	originally just for Peterson pubs. Program based on ExampleExtractor 
 *	in "Processing XML with Java" by Elliotte Rusty Harold, 
 *	http://www.cafeconleche.org/books/xmljava.
 *
 *   Copyright (C) 2009 David Collier-Brown
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 3 of the License, or
 *   (at your option) any later version.
 *   
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *   
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

 *
 */
import org.apache.xerces.parsers.DOMParser; /* Parse either HTML or XML */
/* New w3c serializer */
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.*;
/* Rest of the DOM */
import org.w3c.dom.Node;
import org.w3c.dom.*;
import gnu.getopt.Getopt; 
import gnu.getopt.LongOpt;
import java.io.*;

public class RestyleOdt {
	static PropertyPrinter debugNode = new PropertyPrinter();
	private static final String rcsVersion = "$Id: RestyleOdt.java,v 1.13 2011/12/26 16:48:46 user Exp $";
	private static final double version = 0.9;
	private static boolean debug = false;
	private static boolean verbose = false;
	private static String paragraphMap = null;
	private static String spanMap = null;
	private static String odtTemplate = null;
	private static String rebalanceHeaderPattern = null;
    
    public static void main(String[] argv) {
	RestyleOdt my = new RestyleOdt();
	int	c = 0;
	String	arg = null;
	int	errors = 0;
	boolean warnings = false; // Debugging, turns on sax warnings.
	boolean prettyPrint = false; // Deferred
	boolean restyle = false;
	boolean rebalance = false;
	
	LongOpt[] longopts = {
	    new LongOpt("debug", LongOpt.NO_ARGUMENT, null, 'd'),
	    new LongOpt("paragraph-map", LongOpt.REQUIRED_ARGUMENT, null, 'p'),
	    new LongOpt("span-map", LongOpt.REQUIRED_ARGUMENT, null, 's'),
	    new LongOpt("inserted-heading", LongOpt.REQUIRED_ARGUMENT, 
			null, 'h'), 
	    new LongOpt("rebalance", LongOpt.NO_ARGUMENT, null,'B'),
	    new LongOpt("restyle", LongOpt.NO_ARGUMENT, null,'S'),
	    new LongOpt("template", LongOpt.REQUIRED_ARGUMENT, null, 't'),
	    new LongOpt("verbose", LongOpt.NO_ARGUMENT, null, 'v'),
	};

	Getopt g = new Getopt("RestyleOdt", argv, "BSdh:s:r:t:Vv", longopts);
	while ((c = g.getopt()) != -1) {
		switch (c) {
		case 'B': /* re-balance */
			rebalance = true;
			break;
		case 'S': /* re-style */
			restyle = true;
			break;
		
		case 'd': /* Debug to stderr */
			debug = true;
			break;
		case 'p': 
			paragraphMap = g.getOptarg();
			break;
		case 'h':
			rebalanceHeaderPattern = g.getOptarg();
			break;
		case 's': 
			spanMap = g.getOptarg();
			break;
		case 't':
			odtTemplate = g.getOptarg();
			break;
		case 'v':
			verbose = true;
			break;
		case 'V': /* Report versions. */
			System.err.printf("RestyleOdt version = %1.1f, "
				+ "RCS \"%s\".\n", version, rcsVersion);
			System.exit(1);
			break;
		default:
    			el("Usage: RestyleOdt {---rebalance|--restyle} "
				+ "[-opts] file.xml");
			el("where opts are:");
			el("  --inserted-heading pattern -- what to fix.");
			el("  --paragraph-map file - load para mappings");
			el("  --span-map file - load span mappings");
			el("  --template file - load allowed-styles template");
    			return;
		}
    	}

	if (g.getOptind() >= argv.length) {
		el("RestyleOdt: no input files provided, halting.");
		System.exit(1);
	}
	OdtRestyle st = new OdtRestyle(debug, verbose, paragraphMap, 
		spanMap, odtTemplate);
	OdtRebalance br = new OdtRebalance(debug, verbose, 
		rebalanceHeaderPattern);
	for (int i = g.getOptind(); i < argv.length; i++) {
    		String fileName = argv[i];
	
    	 	if (verbose) {
			el("RestyleOdt: opening " + fileName);
		}
    		try {
			DOMImplementationRegistry registry = 
	    			DOMImplementationRegistry.newInstance();
			DOMImplementationLS impl = 
	    			(DOMImplementationLS)
				registry.getDOMImplementation("LS");
			LSParser builder = impl.createLSParser(
	    			DOMImplementationLS.MODE_SYNCHRONOUS, null);
			//setopts(parser, warnings);
			Document d = builder.parseURI(argv[i]);	

			if (restyle == true) {
				st.remapParaStyles(d);
				st.remapSpanStyles(d);
			}
			if (rebalance == true) {
				br.rebalance(d);
			}
			if (restyle == false && rebalance == false) {
				el("Error: neither --restyle nor "
					+ "--rebalance specified"
					+ "halting.");
				System.exit(1);
			}
			writer(d, prettyPrint);
    		}
		catch (LSException e) {
			el("RestyleOdt could not open \"" + fileName
				+ "\", ignored.");
		}
    		catch (Exception e) { 
    			el("RestyleOdt failed while reading " + fileName); 
    			el(e.toString());
			e.printStackTrace();
			System.exit(++errors);
    		}
    	if (verbose) {
		el("RestyleOdt: completed " + fileName);
	}
    	} // end for 
	System.exit(errors);
    } // end main


   /**
    * The dom3 serializer version of writer, part of the mainline.
    * A pretty-print option can also be used.
    */
    static void writer(Document d, boolean pretty) {
	try {
		DOMImplementationRegistry registry 
			= DOMImplementationRegistry.newInstance();
		DOMImplementationLS impl 
			= (DOMImplementationLS)registry.getDOMImplementation(
				"LS");
		LSSerializer writer = impl.createLSSerializer();
		writer.getDomConfig().setParameter(
			"format-pretty-print",pretty);
		LSOutput output = impl.createLSOutput();
		output.setByteStream(System.out);
		// Is this buffered?
		writer.write(d, output);
	}
	catch(Exception e) {
		el("DOMImplementationRegistry.newInstance threw " + e);
	}
    }

 
    static void p(String s) {
    	System.out.print(s);
    }
    static void pl(String s) {
    	System.out.println(s);
    }
    static void e(String s) {
    	System.err.print(s);
    }
    static void el(String s) {
    	System.err.println(s);
    }
    static void d(String s) {
	if (debug) {
    		System.out.print(s);
	}
    }
    static void dl(String s) {
	if (debug) {
    		System.out.println(s);
	}
    }
 
}
