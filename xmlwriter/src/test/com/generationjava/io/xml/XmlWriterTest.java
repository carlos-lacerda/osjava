package com.generationjava.io.xml;

import junit.framework.TestCase;

import java.io.StringWriter;
import java.io.IOException;

public class XmlWriterTest extends TestCase {

    public XmlWriterTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSimpleXmlWriter() throws IOException {

        // test just an empty tag
        StringWriter sw = new StringWriter();
        XmlWriter xw = new SimpleXmlWriter(sw);
        xw.writeEntity("unit");
        xw.endEntity();
        xw.close();
        assertEquals("SimpleXmlWriter not outputting test-1 correctly ",
                     "<unit/>",
                     sw.toString());


        sw = new StringWriter();
        xw = new SimpleXmlWriter(sw);
        writeTest2(xw);
        xw.close();
        assertEquals("SimpleXmlWriter not outputting test-2 correctly ",
                     getTest2Output(), sw.toString() );
    }

    public void testXmlEncXmlWriter() throws IOException {
        // test just an empty tag
        StringWriter sw = new StringWriter();
        XmlWriter xw = new XmlEncXmlWriter(sw);
        xw.writeEntity("unit");
        xw.endEntity();
        xw.close();
        assertEquals("XmlEncXmlWriter not outputting test-1 correctly ",
                     "<unit/>",
                     sw.toString());

        StringWriter sw2 = new StringWriter();
        xw = new XmlEncXmlWriter(sw2);
        writeTest2(xw);
        xw.close();
        assertEquals("XmlEncXmlWriter not outputting test-2 correctly ",
                     getTest2Output(), sw2.toString() );
    }

    private void writeTest2(XmlWriter xw) throws IOException {
        xw.writeXmlVersion("1.0", "UTF-8");
        xw.writeComment("Unit test");
        xw.writeEntity("unit");
        xw.writeEntity("test").writeAttribute("order","1").writeAttribute("language","english").endEntity();
        xw.writeEntity("again").writeAttribute("order","2").writeAttribute("language","english").writeEntity("andAgain").endEntity().endEntity();
        xw.endEntity();
    }

    private String getTest2Output() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
               "<!--Unit test-->" +
               "<unit>" +
               "<test order=\"1\" language=\"english\"/>" +
               "<again order=\"2\" language=\"english\"><andAgain/></again>" +
               "</unit>";
    }

    public void testPrettyPrinter() throws IOException {
        StringWriter sw = new StringWriter();
        XmlWriter xw = new PrettyPrinterXmlWriter(new SimpleXmlWriter(sw));
        xw.writeEntity("a");
        xw.writeEntity("unit");
        xw.writeText("test");
        xw.endEntity();
        xw.endEntity();
        xw.close();
        assertEquals("PrettyPrinterXmlWriter not outputting test correctly ",
                     "<a>\n  <unit>test</unit>\n</a>\n",
                     sw.toString());
    }

    public void testPrettyPrinterWithCData() throws IOException {
        StringWriter sw = new StringWriter();
        XmlWriter xw = new PrettyPrinterXmlWriter(new SimpleXmlWriter(sw));
        xw.writeEntity("a");
        xw.writeEntity("unit");
        xw.writeCData("test");
        xw.endEntity();
        xw.endEntity();
        xw.close();
        assertEquals("PrettyPrinterXmlWriter not outputting test correctly ",
                     "<a>\n  <unit><![CDATA[ test ]]></unit>\n</a>\n",
                     sw.toString());
    }

    public void testPrettyPrinterWithEntityWithText() throws IOException {
        StringWriter sw = new StringWriter();
        XmlWriter xw = new PrettyPrinterXmlWriter(new SimpleXmlWriter(sw));
        xw.writeEntity("a");
        xw.writeEntity("unit");
        xw.writeCData("test");
        xw.endEntity();
        xw.writeEntityWithText("entityWithText", "text");
        xw.endEntity();
        xw.close();
        assertEquals("PrettyPrinterXmlWriter not outputting test correctly ",
                     "<a>\n  <unit><![CDATA[ test ]]></unit>\n  <entityWithText>text</entityWithText>\n</a>\n",
                     sw.toString());
    }

    public void testPrettyPrinterWithEmptyEntity() throws IOException {
        StringWriter sw = new StringWriter();
        XmlWriter xw = new PrettyPrinterXmlWriter(new SimpleXmlWriter(sw));
        xw.writeEntity("a");
        xw.writeEntity("unit");
        xw.writeCData("test");
        xw.endEntity();
        xw.writeEmptyEntity("emptyEntity");
        xw.endEntity();
        xw.close();
        assertEquals("PrettyPrinterXmlWriter not outputting test correctly ",
                     "<a>\n  <unit><![CDATA[ test ]]></unit>\n  <emptyEntity/>\n</a>\n",
                     sw.toString());
    }

    public void testPrettyPrinterIssue6() throws IOException {
        StringWriter sw = new StringWriter();
        PrettyPrinterXmlWriter xw = new PrettyPrinterXmlWriter(new SimpleXmlWriter(sw));
        xw.setIndent("\t");
        xw.setNewline("\n");

        xw.writeEntity("Person");
        xw.writeEntityWithText("FirstName", "John");
        xw.writeEntityWithText("LastName", "Doe");
        xw.endEntity();
        xw.close();
        assertEquals("PrettyPrinterXmlWriter not outputting test correctly ",
                     "<Person>\n\t<FirstName>John</FirstName>\n\t<LastName>Doe</LastName>\n</Person>\n",
                     sw.toString());
    }

}
