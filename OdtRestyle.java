/**
 * OdtRestyle -- report and repair para- and style-type mismatches
 *
 *   Copyright (C) 2005 David Collier-Brown
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

 */
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.util.Arrays;
/* Rest of the DOM */
import org.w3c.dom.Node;
import org.w3c.dom.*;
import java.io.*;

public class OdtRestyle {
    private HashMap<String,String> paraMappings = 
	new HashMap<String,String>();
    private HashMap<String,String> spanMappings = 
	new HashMap<String,String>();
    private HashMap<String,String> saveMap = null;
    private ReadMapFile rm = new ReadMapFile();
    private Pattern p = Pattern.compile("P[0-9]"); /* Known bad para type. */
    private Pattern t = Pattern.compile("T[0-9]"); /* Known bad style. */
    private Matcher m, n;
    boolean debug = false;
    boolean verbose = false;

   /**
    * Instantiate a restyler with specific mappings and a templeate.
    */
    public OdtRestyle(boolean debug, boolean verbose,
	String paraMapFile, String spanMapFile, String templateFile) {
	this.debug = debug;
	this.verbose = verbose;
	/* Initialize hash tables. */
	loadMap(paraMappings, paraMapFile, "para");
	loadMap(spanMappings, spanMapFile, "span");
    }

  /**
    * Find all text paras in a document and ensure they are converted into
    * one of the legal values.
    */
    public void remapParaStyles(Document doc) {
	/* Save the para map, and then augment it from <style> tags. */
	saveMap = new HashMap(paraMappings);
	augmentMap(paraMappings, doc, "paragraph");

	NodeList paraTagList =  doc.getElementsByTagName("text:p");
	if (paraTagList.getLength() == 0) {
		dl("No <text:p> tags to process, returning.");
		paraMappings = saveMap;
		return;
	}
	for (int i = 0; i < paraTagList.getLength(); i++ ) {
		Element tag = (Element) paraTagList.item(i);
		String value = tag.getAttribute("text:style-name");
		if (value != null) {
			repairStyle(tag, value, 
				allowedParaStyles, paraMappings);
                }
               // else ill-formed
	}
	paraMappings = saveMap;
	return;
    }
    
  /**
    * Conditionally repair a paragraph node by changing it's style,
    * guided by a provided map and an allowed set
    */
    private void repairStyle(Element tag, String from, 
		HashSet allowedStyles, HashMap<String,String> reMappings) {
	String to = null;
	dl("style is " + from);
	if (allowedStyles.contains(from)) {
		/* This is a supported style */
		dl("It's ok");
	}
	else if ((to = reMappings.get(from)) != null) {
		/* Found replacement. */
                try {
			dl("remapped " + from + " to " + to);
                        tag.setAttribute("text:style-name", to);
                } catch (DOMException e) {
                        /* Should never happen, but if it does,,, */
                        throw new RuntimeException(e);
                }
	}
	else {
		el("Warning: no remapping provided for \""
			+ from + "\", left unchanged.");
	}
    }

   /**
    * Find all the <style> tags, and for each that extends
    * another tag, add a mapping from the child to the
    * parent. If the parent is already mapped to a
    * new value, map the child to that same value.
    */
    private void augmentMap(HashMap<String,String> map, 
					Document doc, String styleFamily) {
	 
	NodeList styleTagList = doc.getElementsByTagName("style:style");
	if (styleTagList.getLength() == 0) {
                dl("no <style:style> tags found, returning.");
        }
	for (int i = 0; i < styleTagList.getLength(); i++ ) {
		Element tag = (Element) styleTagList.item(i);
		String family = tag.getAttribute("style:family");
		if (family.equals(styleFamily)) {
			String from = tag.getAttribute("style:name");
			String to = tag.getAttribute("style:parent-style-name");
			String tmp;
			if ((tmp = map.get(to)) != null) {;
				to = tmp;
			}
			dl("added " + from + "=" + to);
			map.put(from, to);
		}
	}
    }

  /**
    * Find all spans in a document and ensure they are converted into
    * one of the legal values.
    */
    public void remapSpanStyles(Document doc) {
	/* Save the span map, and then augment it from <style> tags. */
	saveMap = new HashMap(spanMappings);
	augmentMap(spanMappings, doc, "span");
	NodeList spanTagList = doc.getElementsByTagName("text:span");
	if (spanTagList.getLength() == 0) {
		dl("No <text:span> tags to process, returning.");
		paraMappings = saveMap;
		return;
	}
	for (int i = 0; i < spanTagList.getLength(); i++ ) {
		Element tag = (Element) spanTagList.item(i);
		String value = tag.getAttribute("text:style-name");
		if (value != null) {
			repairStyle(tag, value, 
				allowedSpanStyles, spanMappings);
                }
               // else ill-formed
	}
	paraMappings = saveMap;
	return;
    }

  /**
   * Load a map from a file. Currently takes any name-value pair found
   * between <type> and </type> tags.
   */
    private HashMap loadMap(HashMap m, String fileName, String type) {
	if (rm.readMapFile(fileName, type, m, debug)) {
		return m;
	}
	else {
		return null;
	}
    }


    /* Allowed paragraph and table style-names. */
    // This needs to be extended!
    private HashSet<String> allowedParaStyles = 
	new HashSet<String>(Arrays.asList(

        "AA", "AU ", "BB", "BH", "BI", "BIO", "BL", "BL1", "Body",
        "BodyNoIndent", "BT", "BT1", "BX", "C1", "CALLOUT", "CDATE",
        "CDT", "CDT1", "CDTX", "CEDITS", "CL", "CO", "CQ", "CR",
        "CT", "CTTOC", "DED", "EH", "EL", "EL2", "ELX", "EMH", "EMT",
        "EN", "EQUATION", "ESH", "ET", "ETBL", "ETBL1", "ETBX", "ETNB",
        "ETNL", "ETNL1", "ETNX", "EX", "FC", "FGFN", "FN", "FT", "FTN",
        "GlossDef", "GlossFT", "GlossHead", "GlossTerm", "GlossTitle",
        "GroupTitlesIX", "HA", "HB", "HC", "HD", "HE", "HF", "HG",
        "INH", "ISBN", "IT", "LC", "LC2", "Level 1", "Level 2", "Level 3",
        "LH", "LN", "LOC", "LSH", "LX", "MH", "MN", "NCP", "NCP1", "NCPX",
        "NI", "NL", "NL1", "NLB", "NLC", "NLC1", "NLCX", "NLX", "NO",
        "NOTEH", "NOX", "NumCDT", "NumCDT1", "NumCDTX", "PD", "PN",
        "PREFACE", "PT", "PTFT", "PTTOC", "PUBNAME", "QQ", "QUOTATION",
        "RHR", "RHV", "SB", "SBBL", "SBBL", "SBBX", "SBNL", "SBNL1",
        "SBNLX", "SBX", "SC", "SC1", "SCX", "SH", "TI", "TIH", "TIX",
        "TOCHB", "TOCHC", "TOCHD", "TOCHE", "TOCHF", "TOCHG", "TOCPART",
        "UC", "UL", "UL1", "ULX", "WA", "WAH", "WAX", "XREF",
        /* and table styles. */
        "TN", "TH", "TS", "TCH", "TB1", "TB", "TBX", "TCEM", "TCCP", "TBFN"
	)); 

    /* Allowed span style-names. */
    private HashSet<String> allowedSpanStyles = 
    	new HashSet<String>(Arrays.asList(
        	"CD1", "CD2", "CD3", "CD4", "E1", "E2", "E3", "SUB1", 
		"SUP1", "XIND", "IXI"
	));

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
    void d(String s) {
	if (debug == true) {
    		System.err.print(s);
	}
    }
    void dl(String s) {
	if (debug == true) {
    		System.err.println(s);
	}
    }
}
