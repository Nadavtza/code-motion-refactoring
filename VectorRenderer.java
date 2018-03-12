/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2008, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 *
 * -------------------
 * VectorRenderer.java
 * -------------------
 * (C) Copyright 2007, 2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 30-Jan-2007 : Version 1 (DG);
 * 24-May-2007 : Updated for method name changes (DG);
 * 25-May-2007 : Moved from experimental to the main source tree (DG);
 * 18-Feb-2008 : Fixed bug 1880114, arrows for horizontal plot
 *               orientation (DG);
 * 22-Apr-2008 : Implemented PublicCloneable (DG);
 *
 */

package org.jfree.chart.renderer.xy;

import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.xy.VectorXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.util.PublicCloneable;

/**
 * A renderer that represents data from an {@link VectorXYDataset} by drawing a
 * line with an arrow at each (x, y) point.
 *
 * @since 1.0.6
 */
public class VectorRenderer extends AbstractXYItemRenderer
        implements XYItemRenderer, Cloneable, PublicCloneable, Serializable {

    /** The length of the base. */
    private double baseLength = 0.10;

    /** The length of the head. */
    private double headLength = 0.14;

    /**
     * Creates a new <code>XYBlockRenderer</code> instance with default
     * attributes.
     */
    public VectorRenderer() {
    }

    /**
     * Returns the lower and upper bounds (range) of the x-values in the
     * specified dataset.
     *
     * @param dataset  the dataset (<code>null</code> permitted).
     *
     * @return The range (<code>null</code> if the dataset is <code>null</code>
     *         or empty).
     */
    public Range findDomainBounds(XYDataset dataset) {
        if (dataset == null) {
            throw new IllegalArgumentException("Null 'dataset' argument.");
        }
		double minimum = findMinimum(dataset,0);
        double maximum = findMaximum(dataset,0);   
        if (minimum > maximum) {
            return null;
        }
        else {
            return new Range(minimum, maximum);
        }
    }
	public Range findRangeBounds(XYDataset dataset) {
        if (dataset == null) {
            throw new IllegalArgumentException("Null 'dataset' argument.");
        }
		double minimum = findMinimum(dataset,1);
        double maximum = findMaximum(dataset,1);
        if (minimum > maximum) {
            return null;
        }
        else {
            return new Range(minimum, maximum);
        }
    }

    private double findMaximum(XYDataset dataset,int domainOrRange) {
        int seriesCount;
        double lvalue;
        double uvalue;
        double maximum = Double.NEGATIVE_INFINITY;
        seriesCount = dataset.getSeriesCount();
        if (dataset instanceof VectorXYDataset) {
            VectorXYDataset vdataset = (VectorXYDataset) dataset;
            for (int series = 0; series < seriesCount; series++){
                int itemCount = dataset.getItemCount(series);
                for (int item=0; item < itemCount; item++){
                    double delta = getVectorValue(vdataset,series, item,domainOrRange);
                    if (delta < 0.0) {
                        uvalue =getValue(vdataset,series, item,domainOrRange);
                    }
                    else {
                        lvalue = getValue(vdataset,series, item,domainOrRange);
                        uvalue = lvalue + delta;
                    }
                    maximum = Math.max(maximum, uvalue);
                }
            }
        }
        else {
           
            for ( int series = 0; series < seriesCount; series++) {
                int itemCount = dataset.getItemCount(series);
                for (int item = 0; item < itemCount; item++){
                    lvalue = getDataValue(dataset,series, item,domainOrRange);
                    uvalue = lvalue;
                    maximum = Math.max(maximum, uvalue);
                }
            }
        }
        return maximum;
    }  
    private double findMinimum(XYDataset dataset,int domainOrRange) {
        double minimum = Double.POSITIVE_INFINITY;
        int seriesCount = dataset.getSeriesCount();
        double lvalue;
        double uvalue;
        if (dataset instanceof VectorXYDataset) {
            VectorXYDataset vdataset = (VectorXYDataset) dataset; 
            for (int series = 0; series < seriesCount; series++){
                int itemCount = dataset.getItemCount(series);
                for (int item=0; item < itemCount; item++){
                    double delta =getVectorValue(vdataset,series, item,domainOrRange);
                    if (delta < 0.0) {
                        uvalue = getValue(vdataset,series, item,domainOrRange);
                        lvalue = uvalue + delta;
                    }
                    else {
                        lvalue =getValue(vdataset,series, item,domainOrRange);
                    }
                    minimum = Math.min(minimum, lvalue);
                }
            }
        }
        else {
            for (int series = 0; series < seriesCount; series++) {
                int itemCount = dataset.getItemCount(series);
                int item = 0;
                while (item < itemCount){
                    lvalue =getDataValue(dataset,series, item,domainOrRange);
                    minimum = Math.min(minimum, lvalue);
                    item++;
                }
            }
        }
        return minimum;
    }

	
	//instead of 2 method getXvalue and getYvalue --> one method getValue
	//bool==0 -->x  -->for domain method,  bool==1-->y -->for range method
	private double getValue(VectorXYDataset vdataset,int series, int item,int bool) {
		
		if(bool==0){
			return vdataset.getXValue(series, item);
		}
		else {
			return vdataset.getYValue(series, item);
		}
		
	}
	//instead of 2 method getVectorXValue and getVectorYValue --> one method getVectorValue
	//bool==0 -->x  -->for domain method,  bool==1-->y -->for range method
	private double getVectorValue(VectorXYDataset vdataset,int series, int item,int bool) {
		
		if(bool==0){
			return vdataset.getVectorXValue(series, item);
		}
		else {
			return vdataset.getVectorYValue(series, item);
		}
		
	}
	//instead of 2 method getXvalue and getYvalue --> one method getValue
	//bool==0 -->x  -->for domain method,  bool==1-->y -->for range method
	//this method with XYDataset dataset not VectorXYDataset vdataset --> almost the same
	private double getDataValue(XYDataset dataset,int series, int item,int bool) {
		
		if(bool==0){
			return dataset.getXValue(series, item);
		}
		else {
			return dataset.getYValue(series, item);
		}
		
	}

	

    /**
     * Draws the block representing the specified item.
     *
     * @param g2  the graphics device.
     * @param state  the state.
     * @param dataArea  the data area.
     * @param info  the plot rendering info.
     * @param plot  the plot.
     * @param domainAxis  the x-axis.
     * @param rangeAxis  the y-axis.
     * @param dataset  the dataset.
     * @param series  the series index.
     * @param item  the item index.
     * @param crosshairState  the crosshair state.
     * @param pass  the pass index.
     */
    public void drawItem(Graphics2D g2, XYItemRendererState state,
            Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot,
            ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset,
            int series, int item, CrosshairState crosshairState, int pass) {

        double x = dataset.getXValue(series, item);
        double y = dataset.getYValue(series, item);
        double dx = 0.0;
        double dy = 0.0;
        if (dataset instanceof VectorXYDataset) {
            dx = ((VectorXYDataset) dataset).getVectorXValue(series, item);
            dy = ((VectorXYDataset) dataset).getVectorYValue(series, item);
        }
        double xx0 = domainAxis.valueToJava2D(x, dataArea,
                plot.getDomainAxisEdge());
        double yy0 = rangeAxis.valueToJava2D(y, dataArea,
                plot.getRangeAxisEdge());
        double xx1 = domainAxis.valueToJava2D(x + dx, dataArea,
                plot.getDomainAxisEdge());
        double yy1 = rangeAxis.valueToJava2D(y + dy, dataArea,
                plot.getRangeAxisEdge());
        Line2D line;
        PlotOrientation orientation = plot.getOrientation();
        if (orientation.equals(PlotOrientation.HORIZONTAL)) {
            line = new Line2D.Double(yy0, xx0, yy1, xx1);
        }
        else {
            line = new Line2D.Double(xx0, yy0, xx1, yy1);
        }
        g2.setPaint(getItemPaint(series, item));
        g2.setStroke(getItemStroke(series, item));
        g2.draw(line);

        // calculate the arrow head and draw it...
        double dxx = (xx1 - xx0);
        double dyy = (yy1 - yy0);
        double bx = xx0 + (1.0 - this.baseLength) * dxx;
        double by = yy0 + (1.0 - this.baseLength) * dyy;

        double cx = xx0 + (1.0 - this.headLength) * dxx;
        double cy = yy0 + (1.0 - this.headLength) * dyy;

        double angle = 0.0;
        if (dxx != 0.0) {
            angle = Math.PI / 2.0 - Math.atan(dyy / dxx);
        }
        double deltaX = 2.0 * Math.cos(angle);
        double deltaY = 2.0 * Math.sin(angle);

        double leftx = cx + deltaX;
        double lefty = cy - deltaY;
        double rightx = cx - deltaX;
        double righty = cy + deltaY;

        GeneralPath p = new GeneralPath();
        if (orientation == PlotOrientation.VERTICAL) {
            p.moveTo((float) xx1, (float) yy1);
            p.lineTo((float) rightx, (float) righty);
            p.lineTo((float) bx, (float) by);
            p.lineTo((float) leftx, (float) lefty);
        }
        else {  // orientation is HORIZONTAL
            p.moveTo((float) yy1, (float) xx1);
            p.lineTo((float) righty, (float) rightx);
            p.lineTo((float) by, (float) bx);
            p.lineTo((float) lefty, (float) leftx);
        }
        p.closePath();
        g2.draw(p);


    }

    /**
     * Tests this <code>VectorRenderer</code> for equality with an arbitrary
     * object.  This method returns <code>true</code> if and only if:
     * <ul>
     * <li><code>obj</code> is an instance of <code>VectorRenderer</code> (not
     *     <code>null</code>);</li>
     * <li><code>obj</code> has the same field values as this
     *     <code>VectorRenderer</code>;</li>
     * </ul>
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof VectorRenderer)) {
            return false;
        }
        VectorRenderer that = (VectorRenderer) obj;
        if (this.baseLength != that.baseLength) {
            return false;
        }
        if (this.headLength != that.headLength) {
            return false;
        }
        return super.equals(obj);
    }

    /**
     * Returns a clone of this renderer.
     *
     * @return A clone of this renderer.
     *
     * @throws CloneNotSupportedException if there is a problem creating the
     *     clone.
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
