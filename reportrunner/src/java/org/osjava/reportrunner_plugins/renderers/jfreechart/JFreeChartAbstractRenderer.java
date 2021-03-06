package org.osjava.reportrunner_plugins.renderers.jfreechart;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.io.*;

import org.osjava.reportrunner.AbstractRenderer;
import org.osjava.reportrunner.Report;
import org.osjava.reportrunner.Result;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import org.apache.commons.lang.SerializationUtils;

public class JFreeChartAbstractRenderer extends AbstractRenderer {

    private JFreeChartCreator creator = null;

    public void setCreator(String name) {
        try {
            Class c = Class.forName(name);
            creator = (JFreeChartCreator) c.newInstance();
        } catch(ClassNotFoundException cnfe) {
            throw new RuntimeException("Unable to find class: "+name);
        } catch(InstantiationException ie) {
            throw new RuntimeException("Unable to instantiate class: "+name);
        } catch(IllegalAccessException iae) {
            throw new RuntimeException("Unable to access class: "+name);
        }
    }

	public void display(Result result, Report report, Writer out) throws IOException {
		throw new RuntimeException("This should not be used with a Writer. ");
	}

	protected final JFreeChart createChart(Result result, Report report) {
        // JFreeChart has threading problems
        synchronized (JFreeChartAbstractRenderer.class) {
            return this.creator.createChart(result, report, this);
        }
	}

}
