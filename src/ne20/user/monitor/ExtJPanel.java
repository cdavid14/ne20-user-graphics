/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ne20.user.monitor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultTreeCellEditor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.plot.dial.DialBackground;
import org.jfree.chart.plot.dial.DialCap;
import org.jfree.chart.plot.dial.DialPlot;
import org.jfree.chart.plot.dial.DialTextAnnotation;
import org.jfree.chart.plot.dial.DialValueIndicator;
import org.jfree.chart.plot.dial.StandardDialFrame;
import org.jfree.chart.plot.dial.StandardDialRange;
import org.jfree.chart.plot.dial.StandardDialScale;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.GradientPaintTransformType;
import org.jfree.ui.StandardGradientPaintTransformer;

/**
 *
 * @author edsongley
 */
public class ExtJPanel extends JPanel {
    private String ip,login;
    private NE20Server server;
    private JLabel status = new JLabel("Inicializando");
    private boolean running = true;
    
    private Speedometer oMeterUpload = new Speedometer("Upload");
    private Speedometer oMeterDownload = new Speedometer("Download");
    
    private ArrayList<NE20UserTraff> traff = new ArrayList<>();
    
    private JTextField jtub,jtdb,jtiur,jtidr;
    
    private Histogram jph;
    
    public ExtJPanel(String ip, String login){
        this.ip = ip; 
        this.login = login;
        
        this.setLayout(new BorderLayout());

        add(status, BorderLayout.SOUTH );
        
        ////////////////////////////////
        JPanel jp = new JPanel(new GridLayout(10,1));
        
        
        TitledBorder blo = new TitledBorder("Info: ");
        blo.setTitleJustification(TitledBorder.CENTER);
        blo.setTitlePosition(TitledBorder.TOP);
        jp.setBorder(blo);
        //
        TitledBorder bub = new TitledBorder("Upload Bytes");
        bub.setTitleJustification(TitledBorder.CENTER);
        bub.setTitlePosition(TitledBorder.TOP);
        jtub = new JTextField();
        jtub.setColumns(15);
        jtub.setEditable(false);
        JPanel jpub = new JPanel();
        jpub.add(jtub);
        jpub.setBorder(bub);
        jp.add(jpub);
        
        //
        TitledBorder bdb = new TitledBorder("Download Bytes");
        bdb.setTitleJustification(TitledBorder.CENTER);
        bdb.setTitlePosition(TitledBorder.TOP);
        jtdb = new JTextField();
        jtdb.setColumns(15);
        jtdb.setEditable(false);
        JPanel jpdb = new JPanel();
        jpdb.add(jtdb);
        jpdb.setBorder(bdb);
        jp.add(jpdb);
        //
        TitledBorder bur = new TitledBorder("Upload rate(mbps)");
        bur.setTitleJustification(TitledBorder.CENTER);
        bur.setTitlePosition(TitledBorder.TOP);
        jtiur = new JTextField();
        jtiur.setColumns(15);
        jtiur.setEditable(false);
        JPanel jpiur = new JPanel();
        jpiur.add(jtiur);
        jpiur.setBorder(bur);
        jp.add(jpiur);
        
        //
        TitledBorder bdr = new TitledBorder("Download rate(mbps)");
        bdr.setTitleJustification(TitledBorder.CENTER);
        bdr.setTitlePosition(TitledBorder.TOP);
        jtidr = new JTextField();
        jtidr.setColumns(15);
        jtidr.setEditable(false);
        JPanel jpidr = new JPanel();
        jpidr.add(jtidr);
        jpidr.setBorder(bdr);
        jp.add(jpidr);
        
        
        
        //
        jph = new Histogram();
        
        //
        TitledBorder tsu = new TitledBorder("Upload");
        tsu.setTitleJustification(TitledBorder.CENTER);
        tsu.setTitlePosition(TitledBorder.TOP);
        JPanel jpsu = new JPanel();
        jpsu.setBorder(tsu);
        jpsu.add(oMeterUpload);
        
        //
        TitledBorder tsd = new TitledBorder("Download");
        tsd.setTitleJustification(TitledBorder.CENTER);
        tsd.setTitlePosition(TitledBorder.TOP);
        JPanel jpsd = new JPanel();
        jpsd.setBorder(tsd);
        jpsd.add(oMeterDownload);
        
        JPanel jpcenter  = new JPanel(new GridLayout(2,1));
        jpcenter.add(jph);
        
        JPanel bjp = new JPanel(new GridLayout(1,2));
        bjp.add(jpsu);
        bjp.add(jpsd);
        
        jpcenter.add(bjp);
        
        
        add(jpcenter,BorderLayout.CENTER);
        add(jp,BorderLayout.EAST);
        
    }
    
    public boolean isThis(String ip, String login){
        return this.ip.equals(ip) && this.login.equals(login);
    }
    
    public void setServer(NE20Server server){
        this.server = server;
    }
    
    public void doYourThings(){
        status.setText("Perguntando ao servidor...");
        
        while(running){
            try {
                if(server.hasData(login)){
                    
                    NE20UserInfo info = server.NE20UserInfo(login);
                    //obter valores
                    NE20UserTraff usertraff = server.getUserTraff(info);
                    if(usertraff!=null){
                        jtub.setText(""+usertraff.getUploadBytes());
                        jtdb.setText(""+usertraff.getDownloadBytes());
                        
                        if(traff.size()==0){
                            traff.add(usertraff);
                        }
                        if(traff.size()>0) {
                            status.setText("Prosseguindo");
                            //System.out.println("testing 1");
                            if(traff.get(traff.size()-1).getDownloadBits() != usertraff.getDownloadBits()){
                                traff.add(usertraff);
                                if(traff.size()>2){
                                    NE20UserTraff older = traff.get(traff.size()-2);
                                    long difftime = usertraff.getMillis() - older.getMillis();

                                    long diffupbytes = usertraff.getUploadBytes() - older.getUploadBytes();
                                    long diffdownbytes = usertraff.getDownloadBytes() - older.getDownloadBytes();

                                    System.out.println("up="+diffupbytes+" down="+diffdownbytes+" time="+difftime);
                                    double uprate = ( (diffupbytes*8)/(difftime/1000) ) / 1000000;
                                    double downrate = ( (diffdownbytes*8)/(difftime/1000) ) / 1000000;

                                    oMeterUpload.update(uprate);
                                    oMeterDownload.update(downrate);
                                    jph.update(uprate, downrate);

                                    jtiur.setText(""+uprate);
                                    jtidr.setText(""+downrate);
                                }
                            }
                            
                        }
                        
                        if(traff.size()<2){
                            status.setText("Aguardando próximo...");
                        }
                        
                        if(traff.size()>=2000){
                            traff.remove(0);
                        }
                        //TODO: checar se o id realmente eh o mesmo
                    }
                }else{
                    status.setText("Servidor afirma que usuario nao esta online...");
                }
                
                
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(ExtJPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
}
class Histogram extends JPanel{
    private TimeSeries seriesUpload,seriesDownload;
    private TimeSeriesCollection dataset;
    
    public void update(double upload,double download){
        //GregorianCalendar calendar = new GregorianCalendar();
        //calendar.setTimeInMillis(millis);
        seriesDownload.add(new Millisecond(  ), download);
        seriesUpload.add(new Millisecond(  ), upload);
    }
    public Histogram(){
        seriesUpload = new TimeSeries("Upload");
        seriesDownload = new TimeSeries("Download");
        
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(seriesUpload);
        dataset.addSeries(seriesDownload);
        
        JFreeChart chart = ChartFactory.createTimeSeriesChart("Histograma","", "Rate(mbps)", 
                dataset);
        
        final XYPlot plot = chart.getXYPlot();
        ValueAxis axis = plot.getDomainAxis();
        axis.setAutoRange(true);
        axis.setFixedAutoRange(180000.0); 
        
        axis = plot.getRangeAxis();
        axis.setFixedAutoRange(10);
        axis.setAutoRange(true);
        
        ChartPanel chartpanel = new ChartPanel(chart);
        chartpanel.setPreferredSize(new Dimension(400, 200));
        
        add(chartpanel);
        
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                //System.out.println("x="+e.getComponent().getSize().width+" y="+e.getComponent().getSize().height);
                chartpanel.setSize(e.getComponent().getSize());
                chartpanel.setPreferredSize(e.getComponent().getSize());
                chartpanel.validate();
            }
        });
    }
}

class Speedometer extends JPanel{
    
    private JFreeChart chart;
    private String title;
    private DefaultValueDataset dataset1 = new DefaultValueDataset();

    public void update(double val){
        dataset1.setValue(val);
    }
    public Speedometer(String t) {
        this.title = t;
        
        DialPlot dialplot = new DialPlot();

        dialplot.setView(0.0D, 0.0D, 1.0D, 1.0D);
        dialplot.setDataset(0, dataset1);

        StandardDialFrame standarddialframe = new StandardDialFrame();
        standarddialframe.setBackgroundPaint(Color.lightGray);
        standarddialframe.setForegroundPaint(Color.darkGray);
        dialplot.setDialFrame(standarddialframe);

        GradientPaint gradientpaint = new GradientPaint(new Point(), new Color(255, 255, 255), new Point(), new Color(170, 170, 220));
        DialBackground dialbackground = new DialBackground(gradientpaint);

        dialbackground.setGradientPaintTransformer(new StandardGradientPaintTransformer(GradientPaintTransformType.VERTICAL));
        dialplot.setBackground(dialbackground);

        DialValueIndicator dialvalueindicator = new DialValueIndicator(0);
        dialvalueindicator.setFont(new Font("Dialog", 0, 10));
        dialvalueindicator.setOutlinePaint(Color.darkGray);
        dialvalueindicator.setRadius(0.59999999999999998D);
        //dialvalueindicator.setAngle(-103D);
        dialplot.addLayer(dialvalueindicator);

        
        StandardDialScale standarddialscale = new StandardDialScale(0D, 100D, -120D, -300D, 10D, 4);
        standarddialscale.setTickRadius(0.88D);
        standarddialscale.setTickLabelOffset(0.14999999999999999D);
        standarddialscale.setTickLabelFont(new Font("Dialog", 0, 14));
        dialplot.addScale(0, standarddialscale);

        StandardDialRange standarddialrange = new StandardDialRange(0D, 100D, Color.blue);
        standarddialrange.setScaleIndex(0);
        standarddialrange.setInnerRadius(0.58999999999999997D);
        standarddialrange.setOuterRadius(0.58999999999999997D);
        dialplot.addLayer(standarddialrange);

        org.jfree.chart.plot.dial.DialPointer.Pin pin = new org.jfree.chart.plot.dial.DialPointer.Pin(1);
        pin.setRadius(0.55000000000000004D);
        dialplot.addPointer(pin);

        org.jfree.chart.plot.dial.DialPointer.Pointer pointer = new org.jfree.chart.plot.dial.DialPointer.Pointer(0);
        dialplot.addPointer(pointer);

        DialCap dialcap = new DialCap();
        dialcap.setRadius(0.10000000000000001D);
        dialplot.setCap(dialcap);
        
        
        chart = new JFreeChart(dialplot);
        ChartPanel chartpanel = new ChartPanel(chart);
        chartpanel.setPreferredSize(new Dimension(200, 200));
        add(chartpanel);
    }
    
    
    
}