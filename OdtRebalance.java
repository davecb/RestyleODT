/**
 * OdtRebalance -- turn selected tags into children of H[0-9]
 * 
 *   Copyright 2009  David Collier-Brown
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashSet; /* For sets of font families. */
import java.util.HashMap;
import java.util.Arrays;
/* Rest of the DOM */
import org.w3c.dom.Node;
import org.w3c.dom.*;
import java.io.*;

public class OdtRebalance {
    private Pattern p = Pattern.compile("^(H)([0-9]+)$"); /* Heading. */
    private Pattern q = null;  /* For the bad para type. */
    private Matcher m, n;
    private boolean debug = false;
    private boolean verbose = false;
    private String rebalancePattern = null;
    private miniStack stack = null;

   /**
    * Create a class that will fold the specified tags into the
    * normal H1-H9 hierarchy. Runs after the remapping, so one
    * can insure the heading tags aer already H1-9.
    */
    public OdtRebalance(boolean debug, boolean verbose, 
					String rebalancePattern) {
	this.debug = debug;
	this.verbose = verbose;
	/* Initialize pattern matcher. */
	q = Pattern.compile("^(" + rebalancePattern +")([0-9]+)$");
	dl("rebalance pattern was ^(" + rebalancePattern + ")([0-9]+)$");
	stack = new miniStack();
    }

   /**
    * Apply the rebalancing to a particular document.
    */
    public void rebalance(Document doc) {
	int lastH = 0; /* The number from the last H[0-9] */

	NodeList paraList =  doc.getElementsByTagName("text:p");
	if (paraList.getLength() == 0) {
		dl("No <text:p> tags to rebalance, returning.");
		return;
	}
	for (int i = 0; i < paraList.getLength(); i++ ) {
		Element tag =  (Element)paraList.item(i);
		String value = tag.getAttribute("text:style-name");
		if (value == null) {
			dl("<text:p> with no text:style-name seen, ignored");
			continue;
		}
		dl("<text:p " + value + " seen");
		m = p.matcher(value); /* H[0-9]+ */
		n = q.matcher(value); /* T[0-9]+ */
		if (m.find()) {
			/* Then we have a header */
			String numberPart = m.group(2);
			int n = Integer.parseInt(numberPart);
			lastH = n;
			dl("Header " + value + " = " + n);
	
			int tos = stack.top();
			if (n > tos) {
				; //if numeric part deeper that the TOS, push
				dl(" push");
				stack.push(n);
			}
			else if (n == tos) {
				; //if == to TOS, continue
				dl(" continue");
			}
			else if (n < tos) {
				;//if shallower, pop stack to match
				dl(" pop");
				stack.popTo(n);
			}
		}
		else if (n.find()) {
			/* Then we have a bogon. */
			String numberPart = n.group(2);
			int n = Integer.parseInt(numberPart);
			dl("Bogon " + value + " = " + n);
			tag.setAttribute("text:style-name", "H" + (n+lastH));
			int tos = stack.top();
			if ((n + lastH) > tos) {
				stack.push(n + lastH);
				dl(" push " + (n + lastH));
			}
			else if ((n + lastH) == tos) {
				dl(" continue");
			}
			else if ((n + lastH) > tos) {
				
				dl(" pop");
				stack.popTo(n + lastH);
				dl(" popped To " + (n + lastH));
			}
		}
		else {
			/* It's some other text:style-name */
		}
	}
	return;
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

   /**
    * Create a short stack of ints, with bounds checking but no locks.
    * This could be a simple depth counter, but I wanted more debugging.
    */
    class miniStack {
	private int[] stack =  { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	public int depth = 0; /* 0  1  2  3  4  5  6  7  8  9  10 11 */

	public miniStack() { }

	public int top() {
		return stack[depth];
	}
	public void push(int i) {
		stack[++depth] = i;
		if (depth > 10) {
			dl("stack overflow");
			depth = 10;
		}
		else if (i != depth) {
			dl("in push, i = " + i + " but depth = " + depth);
		}
	}
	public void popTo(int i) {
		while (stack[depth] != i) {
			depth--;
			if (depth < 1) {
				dl("stack underflow");
				depth = 1;
			}
		}
	}
    }
}
