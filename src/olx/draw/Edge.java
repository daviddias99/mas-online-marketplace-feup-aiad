package olx.draw;

import olx.utils.Util;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.QuadCurve2D;
import uchicago.src.sim.gui.DrawableEdge;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.network.DefaultEdge;
import uchicago.src.sim.network.Node;

public class Edge extends DefaultEdge implements DrawableEdge {
    private Color color = Color.WHITE;
    private static final float DEFAULT_STRENGTH = 1;
    private int scalar = Util.randomBetween(-100, 100); 

    public Edge() { }

    public Edge(Node from, Node to) {
        super(from, to, "", DEFAULT_STRENGTH);
    }

    public Edge(Node from, Node to, float strength) {
        super(from, to, "", strength);
    }

    public void setColor(Color c) {
        color = c;
    }

    public Color getColor() {
        return color;
    }

    public void draw(SimGraphics g, int fromX, int toX, int fromY, int toY) {
        Graphics2D g2 = g.getGraphics();
        // Color (drawInit is private)
        if(g2.getPaint() != color)
            g2.setPaint(color);
        
        // Calculate Control Point
        float y_vect = toX - fromX;
        float x_vect = fromY - toY;
        double vect_size = Math.sqrt(y_vect * y_vect + x_vect * x_vect);
        y_vect /= vect_size;
        x_vect /= vect_size;
        float med_x = (fromX + toX) / 2;
        float med_y = (fromY + toY) / 2;
                    
        // Link
        QuadCurve2D link = new QuadCurve2D.Float(fromX, fromY, med_x + x_vect * scalar, med_y + y_vect * scalar, toX, toY);
        g2.draw(link);
        g.setGraphics(g2);
    }
}

