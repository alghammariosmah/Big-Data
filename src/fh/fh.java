package normal;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Window;
import java.io.*;
import java.lang.reflect.Array;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.JPanel;


import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Minute;
import org.jfree.data.time.Month;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.RefineryUtilities;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONArray;
import org.json.JSONObject;


public class normal extends ApplicationFrame  {

    private static final long serialVersionUID = 1L;
    private static final int NUMQUARTERS = 20;
    
    private static String title = new String ();
    private static final int period = 30;
    private static double [] webdata = new double [period]; // save for 16 days
    private static double [] movingAverage = new double[period];
    private static int [] dateInt = new int[period*3];

    
    private static TimeSeries s1 = new TimeSeries("Real Load");
    private static TimeSeries s2 = new TimeSeries("Predicted Load");
    private static TimeSeries s3 = new TimeSeries("Improved Predicted Load");;

    static {
        // set a theme using the new shadow generator feature available in
        // 1.0.14 - for backwards compatibility it is not enabled by default
        ChartFactory.setChartTheme(new StandardChartTheme("JFree/Shadow",
                true));
    }

    /**
     * A demonstration application showing how to create a simple time series
     * chart.  This example uses monthly data.
     *
     * @param title  the frame title.
     * @throws Exception 
     */
    public normal(String title) throws Exception {
        super(title);
        setLayout(new GridLayout(3, 1));
        ChartPanel chartPanel = (ChartPanel) createDemoPanel();
        chartPanel.setPreferredSize(new java.awt.Dimension(1200, 300));
        add(chartPanel);
        
        ChartPanel chartPanel2 = (ChartPanel) createDemoPanel2();
        chartPanel2.setPreferredSize(new java.awt.Dimension(1200, 300));
        add(chartPanel2);
        
        ChartPanel chartPanel3 = (ChartPanel) createDemoPanel3();
        chartPanel3.setPreferredSize(new java.awt.Dimension(1200, 300));
        add(chartPanel3);
        
    }
    
    /**
     * Creates a chart.
     *
     * @param dataset  a dataset.
     *
     * @return A chart.
     */
    private static JFreeChart createChart(XYDataset dataset, String title, int color) {

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
            title,  // title
            "Date",             // x-axis label
            "Load (Wh)",   // y-axis label
            dataset,            // data
            true,               // create legend?
            true,               // generate tooltips?
            false               // generate URLs?
        );

        chart.setBackgroundPaint(Color.white);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);

        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            
            if (color == 0) {
             renderer.setSeriesPaint(0, Color.RED);
             renderer.setSeriesPaint(1, Color.BLUE);
            } 
            if (color == 1) { 
             renderer.setSeriesPaint(0, Color.RED);
             renderer.setSeriesPaint(1, Color.BLACK);
             
            } 
            if (color == 2) { 
            	renderer.setSeriesPaint(0, Color.BLUE);
            	renderer.setSeriesPaint(1, Color.BLACK);
            } 
            
            renderer.setBaseShapesVisible(false);
            renderer.setBaseShapesFilled(false);
            renderer.setDrawSeriesLineAsPath(true);
        }

        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("dd-MM"));

        return chart;

    }
    
    
    private static XYDataset createDataset() throws ParseException {
    	
        SimpleDateFormat standardDateFormat = new SimpleDateFormat("dd-MM");
        Date myDate;
        
        reverse(webdata);
        for (int i = 0; i < webdata.length; i++) {
        	//output2[i] = Double.valueOf(i);
        	
        	DateTime yesterday = new DateTime().minusDays(i+1); // getting the last days
    		String days = yesterday.toString("dd-MM"); // it has to be int days and months
    		
    		myDate = standardDateFormat.parse(days);

    		s1.addOrUpdate(new Day(myDate), webdata[i]);	
        }   	
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(s1);
        return dataset;
    }
    
    private static XYDataset createDataset2() throws ParseException {
    	
        SimpleDateFormat standardDateFormat = new SimpleDateFormat("MM-dd");
        Date myDate;
        reverse(movingAverage);
        for (int i = 0; i < movingAverage.length; i++) {
        	
        	DateTime yesterday = new DateTime().minusDays(i); // getting the last days
    		String days = yesterday.toString("MM-dd");
    		myDate = standardDateFormat.parse(days);
    		s2.addOrUpdate(new Day(myDate), movingAverage[i]);	
        }
    	
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(s2);
        
        return dataset;
    	
    }
    
    private static XYDataset createDataset3() throws ParseException {
    	
        SimpleDateFormat standardDateFormat = new SimpleDateFormat("HH:mm:ss");

        String date3;
        Date myDate;
        long sum;
        
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    	
        double [] output2 = new double [NUMQUARTERS];
        for (int i = 0; i < NUMQUARTERS; i++) {
        	output2[i] = Double.valueOf(i);
            
        	String time1="00:00:00";
            String time2="0:" + 15*i + ":00";
    		Date date1 = timeFormat.parse(time1);
    		Date date2 = timeFormat.parse(time2);
    		sum = date1.getTime() + date2.getTime();
    		date3 = timeFormat.format(new Date(sum));
    		myDate = standardDateFormat.parse(date3);
    		s3.addOrUpdate(new Minute(myDate), output2[i]);
    		
        }
    	
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(s3);
        
        return dataset;
    	
    }

    
    /**
     * Creates a panel for the demo (used by SuperDemo.java).
     *
     * @return A panel.
     * @throws Exception 
     */
    // Original values
    public static JPanel createDemoPanel() throws Exception {
        JFreeChart chart = createChart(createDataset(), "Real Load", 0);
        ChartPanel panel = new ChartPanel(chart);
        panel.setFillZoomRectangle(true);
        panel.setMouseWheelEnabled(true);
        return panel;
    }
    
    // New strategy
    public static JPanel createDemoPanel2() throws Exception {
        JFreeChart chart = createChart(createDataset2(), "Improvement", 1);
        ChartPanel panel = new ChartPanel(chart);
        panel.setFillZoomRectangle(true);
        panel.setMouseWheelEnabled(true);
        return panel;
    }

    // Error
    public static JPanel createDemoPanel3() throws Exception {
        JFreeChart chart = createChart(createDataset3(), "Error", 2);
        ChartPanel panel = new ChartPanel(chart);
        panel.setFillZoomRectangle(true);
        panel.setMouseWheelEnabled(true);
        return panel;
    }
    
    public static double[] RestAPIValues() throws Exception {
		int index = 0;
	    // build a URL
	    String s = "http://api.apixu.com/v1/history.json?key=83df9f91cfd44eaebfb81207172010&q=Linz&dt=";

	    String temp1 = "";
	    
    	for (int d = 1; d < 31 ; d++ ){ // change 15 to 31
    		temp1 = "";
    		DateTime yesterday = new DateTime().minusDays(d); // getting the last days
    		String days = yesterday.toString("yyyy-MM-dd");
    		temp1 = s+ days;
    		URL url = new URL(temp1);
   		 
		    // read from the URL
		    Scanner scan = new Scanner(url.openStream());
		    String str = new String();
		    while (scan.hasNext())
		        str += scan.nextLine();
		    scan.close();
		    
		    JSONObject obj = new JSONObject(str);
		    JSONObject location  = obj.getJSONObject("location");
		    
		    JSONObject forcastdays = obj.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(0).getJSONObject("day");
		    Object ou =forcastdays.get("mintemp_c") ;
		    
		    
		    Double ou1 = (Double) ou;
		    
		    //System.out.println(ou1);
		    
		    webdata[index] = ou1;
		    index +=1;
    	}
	    
	    return webdata;	
	}

    public static void reverse(double[] array) {
        if (array == null) {
            return;
        }
        int i = 0;
        int j = array.length - 1;
        double tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }
    public static double[] simpleMovingAverage(int prd){
    	
    	double [] simplemovingAverage = new double[period];
    	double sum = 0.0;
    	for(int i = 0; i < prd; i++){
    		sum += webdata[i];
    	}  
    	simplemovingAverage[prd-1] = sum/prd;
	    int j = prd;   
	    for(int i = prd; i < period; i++){
	    	
	         sum = sum+webdata[i]-webdata[i-prd];
	         simplemovingAverage[j++] = sum/prd;
	    }
    	 	
//    	for (int i=0; i<movingAverage.length; i++)
//        {      
//	    	DecimalFormat df = new DecimalFormat("0.00");  
//    		System.out.println(df.format(movingAverage[i]));
//        }
		return simplemovingAverage;
    }
       
    /**
     * Starting point for the demonstration application.
     *
     * @param args  ignored.
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {

    	RestAPIValues();
    	movingAverage =  simpleMovingAverage(5);
    	
		    	    	
		title = "Time Series Lab";
    	normal demo = new normal("Time Series Lab v0.1");
        demo.pack();
                
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);

    }
    
	
}
