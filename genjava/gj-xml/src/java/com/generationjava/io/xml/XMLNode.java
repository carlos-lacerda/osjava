/*
 * Copyright (c) 2006, Henri Yandell
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or 
 * without modification, are permitted provided that the 
 * following conditions are met:
 * 
 * + Redistributions of source code must retain the above copyright notice, 
 *   this list of conditions and the following disclaimer.
 * 
 * + Redistributions in binary form must reproduce the above copyright notice, 
 *   this list of conditions and the following disclaimer in the documentation 
 *   and/or other materials provided with the distribution.
 * 
 * + Neither the name of Genjava-Core nor the names of its contributors 
 *   may be used to endorse or promote products derived from this software 
 *   without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.generationjava.io.xml;

import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * An xml tag. It can be a processing instructon, an empty tag or 
 * a normal tag. Currently, if the tag is inside a namespace then 
 * that is a part of the name. That is, all names of tags are 
 * fully qualified by the namespace.
 */
public class XMLNode {

    private static final Iterator EMPTY = new NullIterator();

    private HashMap myAttrs;
    private HashMap myNodes;  // allows quick lookup
    private ArrayList myNodeList;  // maintains order of myNodes
    private String name;
    private String value;
    private boolean pi;
    private boolean comment;
    private boolean doctype;

    /**
     * Empty Constructor.
     */
    public XMLNode() {
        this("");
    }
    
    /**
     * Create a new node with this name.
     */
    public XMLNode(String name) {
        this.name = name;
    }
    
    /**
     * Add a child node to this node.
     */
    public void addNode(XMLNode node) {
        if(this.myNodes == null) {
            this.myNodes = new HashMap();
            this.myNodeList = new ArrayList();
        }
        this.myNodeList.add(node);
        Object obj = this.myNodes.get( node.getName() );
        if(obj == null) {
            this.myNodes.put( node.getName(), node );
        } else
        if(obj instanceof XMLNode) {
            ArrayList list = new ArrayList();
            list.add(obj);
            list.add(node);
            this.myNodes.put( node.getName(), list );
        } else
        if(obj instanceof ArrayList) {
            ArrayList list = (ArrayList)obj;
            list.add(node);
        }
    }

    // Iterates a child node. Possibly needs renaming.
    // That is, it iterates a child nodes value.
    /**
     *
     */
    public Iterator iterateNode(String name) {
        if(this.myNodes == null) {
            return EMPTY;
        }
        Object obj = this.myNodes.get( name );
        if(obj == null) {
            return EMPTY;
        } else 
        if(obj instanceof ArrayList) {
            return ((ArrayList)obj).iterator();
        } else {
            return new SingleIterator(obj);
        }
    }

    /**
     * Add an attribute with specified name and value.
     */
    public void addAttr(String name, String value) {
        if(this.myAttrs == null) {
            this.myAttrs = new HashMap();
        }
        value = unescapeXml(value);
        this.myAttrs.put( name, value );
    }
    
    /**
     * Get the attribute with the specified name.
     */
    public String getAttr(String name) {
        if(myAttrs == null) {
            return null;
        }
        return (String)this.myAttrs.get(name);
    }
    
    /**
     * Iterate over all the attributes of this node.
     * In the order they were added.
     */
    public Iterator iterateAttr() {
        if(this.myAttrs == null) {
            return EMPTY;
        } else {
            return this.myAttrs.keySet().iterator();
        }
    }

    /**
     * Get the node with the specified name.
     */
    public XMLNode getNode(String name) {
        if(this.myNodes == null) {
            return null;
        }
        Object obj = this.myNodes.get(name);
        if(obj instanceof XMLNode) {
            return (XMLNode)obj;
        }
        return null;
    }
    
    /**
     * Iterate over all of this node's children nodes.
     */
    public Iterator iterateNode() {
        if(this.myNodes == null) {
            return EMPTY;
        } else {
//        return this.myNodes.iterator();
        return this.myNodeList.iterator();
        }
    }
    
    /**
     * Get the name of this node. Includes the namespace.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the namespace of this node.
     */
    public String getNamespace() {
        if(this.name.indexOf(":") != -1) {
            return this.name.substring(0,this.name.indexOf(":"));
        } else {
            return "";
        }
    }

    /**
     * Get the tag name of this node. Doesn't include namespace.
     */
    public String getTagName() {
        if(this.name.indexOf(":") != -1) {
            return this.name.substring(this.name.indexOf(":")+1);
        } else {
            return this.name;
        }
    }

    /**
     * Get the appended toString's of the children of this node.
     * For a text node, it will print out the plaintext.
     */
    public String getValue() {
        if(isComment()) {
            return "<!-- " + value + " -->";
        }
        if(isDocType()) {
            return "<!DOCTYPE " + value + ">";
        }
        if(this.value != null) {
            return this.value;
        }
        if(isInvisible()) {
            return "";
        }
        // QUERY: shouldn't call toString. Needs to improve
        if(this.myNodeList != null) {
            StringBuffer buffer = new StringBuffer();
            Iterator iter = iterateNode();
            while(iter.hasNext()) {
                buffer.append(iter.next().toString());
            }
            return buffer.toString();
        }
        return null;
    }
    
    /**
     * Set the plaintext contained in this node.
     */
    public void setPlaintext(String str) {
        this.value = unescapeXml(str);
    }

    /**
     * Is this a normal tag?
     * That is, not plaintext, not comment and not a pi.
     */
    public boolean isTag() {
        return !(this.pi || (this.name == null) || (this.value != null));
    }

    /**
     * Is it invisible
     */
    public boolean isInvisible() {
        return this.name == null;
    }
    
    /**
     * Set whether this node is invisible or not.
     */
    public void setInvisible(boolean b) {
        if(b) {
            this.name = null;
        }
    }
    
    /**
     * Is it a doctype
     */
    public boolean isDocType() {
        return this.doctype;
    }
    
    /**
     * Set whether this node is a doctype or not.
     */
    public void setDocType(boolean b) {
        this.doctype = b;
    }
    
    /**
     * Is it a comment
     */
    public boolean isComment() {
        return this.comment;
    }
    
    /**
     * Set whether this node is a comment or not.
     */
    public void setComment(boolean b) {
        this.comment = b;
    }
    
    /**
     * Is it a processing instruction    
     */
    public boolean isPI() {
        return this.pi;
    }
    
    /**
     * Set whether this node is a processing instruction or not.
     */
    public void setPI(boolean b) {
        this.pi = b;
    }
    
    // IMPL: Assumes that you're unable to remove nodes from 
    //          a parent node. removeNode and removeAttr is likely to 
    //          become a needed functionality.
    /**
     * Is this node empty.
     */
    public boolean isEmpty() {
        return (this.myNodes == null);
    }

    /**
     * Is this a text node.
     */
    public boolean isTextNode() {
        return ((this.value != null) && !comment && !doctype && !pi);
    }

    // not entirely necessary, but allows XMLNode's to be output 
    // int XML by calling .toString() on the root node.
    // Probably wants some indentation handling?
    /**
     * Turn this node into a String. Outputs the node as 
     * XML. So a large amount of output.
     */
    public String toString() {
        if(isComment()) {
            return "<!-- " + value + " -->";
        }
        if(isDocType()) {
            return "<!DOCTYPE " + value + ">";
        }
        if(value != null) {
            return value;
        }

        StringBuffer tmp = new StringBuffer();

        if(!isInvisible()) {
            tmp.append("<");
            if(isPI()) {
                tmp.append("?");
            }
            tmp.append(name);
        }
        
        Iterator iter = iterateAttr();
        while(iter.hasNext()) {
            tmp.append(" ");
            String obj = (String)iter.next();
            tmp.append(obj);
            tmp.append("=\"");
            tmp.append(getAttr(obj));
            tmp.append("\"");
        }
        if(isEmpty()) {
            if(isPI()) {
                tmp.append("?>");
            } else {
                if(!isInvisible()) {
                    tmp.append("/>");
                }
            }
        } else {
            if(!isInvisible()) {
                tmp.append(">");
            }

            tmp.append(bodyToString());

            if(!isInvisible()) {
                tmp.append("</"+name+">\n");
            }
        }
        return tmp.toString();
    }
            
    /**
     * Get the String version of the body of this tag.
     */
    public String bodyToString() {
        StringBuffer tmp = new StringBuffer();
        Iterator iter = iterateNode();
        while(iter.hasNext()) {
            Object obj = iter.next();
            if(obj instanceof XMLNode) {
                XMLNode node = (XMLNode)obj;
                tmp.append(node);
            } else
            if(obj instanceof ArrayList) {
                ArrayList nodelist = (ArrayList)obj;
                Iterator nodeItr = nodelist.iterator();
                while(nodeItr.hasNext()) {
                    XMLNode node = (XMLNode)nodeItr.next();
                    tmp.append(node);
                }
            }                
        }
        return tmp.toString();
    }

    private static String unescapeXml(String str) {
        str = replace(str,"&amp;","&");
        str = replace(str,"&lt;","<");
        str = replace(str,"&gt;",">");
        str = replace(str,"&quot;","\"");
        str = replace(str,"&apos;","'");
        return str;
    }

    // from Commons.Lang.StringUtils
    private static String replace(String text, String repl, String with) {   
        int max = -1;
        if (text == null || repl == null || with == null || repl.length() == 0 || max == 0) {
            return text;
        }
        
        StringBuffer buf = new StringBuffer(text.length());
        int start = 0, end = 0;
        while ((end = text.indexOf(repl, start)) != -1) {
            buf.append(text.substring(start, end)).append(with);
            start = end + repl.length();
            
            if (--max == 0) {
                break;
            }
        }
        buf.append(text.substring(start));
        return buf.toString();
    }
        
}

/**
 * An empty iterator. Nicer to return than just plain null. 
 */
class NullIterator implements Iterator {
    public Object next() {
        return null;
    }
 
    public boolean hasNext() {
        return false;
    }   

    public void remove() {
        throw new UnsupportedOperationException("There is nothing to remove");
    }
}
/**
 * A single iterator. Saves time on making an ArrayList.
 */
class SingleIterator implements Iterator {

    private Object obj;

    public SingleIterator(Object obj) {
        this.obj = obj;
    }

    public Object next() {
        Object tmp = this.obj;
        this.obj = null;
        return tmp;
    }
 
    public boolean hasNext() {
        return (obj != null);
    }   

    public void remove() {
        throw new UnsupportedOperationException("This is an immutable iterator");
    }
}
