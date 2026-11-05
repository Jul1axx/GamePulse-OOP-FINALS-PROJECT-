import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JPanel;

public class WeeklyChartPanel extends JPanel {
    private Map<String, Long> values = new LinkedHashMap<>();
    public WeeklyChartPanel() {
        setBackground(AppTheme.PANEL);
        setPreferredSize(new Dimension(520, 240));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
    }
    public void setValues(Map<String, Long> values) {
        this.values = values == null ? new LinkedHashMap<>() : new LinkedHashMap<>(values);
        repaint();
    }
    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int left=48, right=18, top=24, bottom=42;
        int chartWidth=Math.max(100,getWidth()-left-right), chartHeight=Math.max(80,getHeight()-top-bottom);
        long max=1;
        for (long value:values.values()) max=Math.max(max,value);
        max=((max+3599)/3600)*3600;
        g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        for (int line=0; line<=4; line++) {
            int y=top+(chartHeight*line/4);
            g.setColor(AppTheme.BORDER); g.setStroke(new BasicStroke(1)); g.drawLine(left,y,left+chartWidth,y);
            double hours=(max/3600.0)*(4-line)/4.0;
            g.setColor(AppTheme.MUTED); g.drawString(String.format("%.0fh",hours),8,y+4);
        }
        int count=Math.max(1,values.size());
        int slot=chartWidth/count; int barWidth=Math.max(10,(int)(slot*0.48));
        int index=0;
        for (Map.Entry<String,Long> entry:values.entrySet()) {
            int barHeight=(int)Math.round((entry.getValue()/(double)max)*chartHeight);
            int x=left+index*slot+(slot-barWidth)/2;
            int y=top+chartHeight-barHeight;
            g.setColor(AppTheme.ACCENT); g.fillRoundRect(x,y,barWidth,Math.max(2,barHeight),8,8);
            g.setColor(AppTheme.TEXT); g.drawString(entry.getKey(),x-2,top+chartHeight+19);
            index++;
        }
        g.dispose();
    }
}
