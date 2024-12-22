package bsu.rfe.java.group10.lab4.Melnik.varB5;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.List;
import javax.swing.*;

@SuppressWarnings("serial")
public class GraphicsDisplay extends JPanel {
    private Double[][] graphicsData;
    private boolean showAxis = true;
    private boolean showMarkers = true;
    private BasicStroke graphicsStroke;

    private double minX, maxX, minY, maxY, scaleX, scaleY;
    private boolean isDragging = false;
    private Point dragStart = null;
    private Rectangle dragRect = null;

    private boolean showExtremums = false;

    public GraphicsDisplay() {
        setBackground(Color.WHITE);

        graphicsStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, new float[] {4, 1, 2, 1, 1, 1, 2, 1, 4}, 0.0f);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    resetScale();
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    isDragging = true;
                    dragStart = e.getPoint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDragging) {
                    if (dragRect != null && dragRect.width > 0 && dragRect.height > 0) {
                        scaleToArea(dragRect); // Масштабирование выделенной области
                    }
                    isDragging = false;
                    dragRect = null;
                    repaint();
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                showPointCoordinates(e); // Отображение координат точки при наведении
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDragging) {
                    dragRect = new Rectangle(dragStart);
                    dragRect.add(e.getPoint()); // Рисование рамки выделения
                    repaint();
                }
            }
        });
    }

    private void showPointCoordinates(MouseEvent e) {
        if (graphicsData == null) return;

        Point mousePoint = e.getPoint();
        for (Double[] point : graphicsData) {
            Point2D.Double graphPoint = xyToPoint(point[0], point[1]);
            if (Math.abs(graphPoint.x - mousePoint.x) < 5 && Math.abs(graphPoint.y - mousePoint.y) < 5) {
                Graphics2D g2d = (Graphics2D) getGraphics();
                g2d.setColor(Color.BLACK);
                g2d.drawString(String.format("(%.2f, %.2f)", point[0], point[1]),
                        (int) graphPoint.x + 5, (int) graphPoint.y - 5);
                break;
            }
        }
    }

    private void scaleToArea(Rectangle rect) {
        double newMinX = minX + (rect.x / scaleX);
        double newMaxX = minX + ((rect.x + rect.width) / scaleX);
        double newMinY = maxY - ((rect.y + rect.height) / scaleY);
        double newMaxY = maxY - (rect.y / scaleY);

        minX = newMinX;
        maxX = newMaxX;
        minY = newMinY;
        maxY = newMaxY;

        scaleX = getWidth() / (maxX - minX);
        scaleY = getHeight() / (maxY - minY);

        repaint();
    }

    private void resetScale() {
        calculateBounds(); // Восстановление границ
        repaint();
    }

    public void showGraphics(Double[][] graphicsData) {
        this.graphicsData = graphicsData;
        calculateBounds();
        repaint();
    }

    public void setShowAxis(boolean showAxis) {
        this.showAxis = showAxis;
        repaint();
    }

    public void setShowMarkers(boolean showMarkers) {
        this.showMarkers = showMarkers;
        repaint();
    }

    protected Point2D.Double xyToPoint(double x, double y) {
        double deltaX = x - minX;
        double deltaY = maxY - y;
        return new Point2D.Double(deltaX * scaleX, deltaY * scaleY);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (graphicsData == null || graphicsData.length == 0) return;

        Graphics2D canvas = (Graphics2D) g;
        if (showAxis) paintAxis(canvas);
        paintGraphics(canvas);
        if (showMarkers) paintMarkers(canvas);
        if (showExtremums) paintExtremums(canvas);

        if (dragRect != null) {
            canvas.setColor(Color.BLACK);
            canvas.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_BEVEL, 0, new float[]{6, 6}, 0));
            canvas.draw(dragRect);
        }
    }

    private void calculateBounds() {
        minX = graphicsData[0][0];
        maxX = graphicsData[0][0];
        minY = graphicsData[0][1];
        maxY = graphicsData[0][1];

        for (Double[] point : graphicsData) {
            if (point[0] < minX) minX = point[0];
            if (point[0] > maxX) maxX = point[0];
            if (point[1] < minY) minY = point[1];
            if (point[1] > maxY) maxY = point[1];
        }

        maxX += maxX * 0.25;
        minX -= maxX * 0.25;
        maxY += maxX * 0.2;
        minY -= maxX * 0.1;

        scaleX = getWidth() / (maxX - minX);
        scaleY = getHeight() / (maxY - minY);
    }

    protected void paintAxis(Graphics2D canvas) {
        canvas.setStroke(new BasicStroke(2.0f));
        canvas.setColor(Color.BLACK);

        Point2D.Double xStart = xyToPoint(minX, 0);
        Point2D.Double xEnd = xyToPoint(maxX, 0);
        canvas.draw(new Line2D.Double(xStart, xEnd));

        Point2D.Double yStart = xyToPoint(0, minY);
        Point2D.Double yEnd = xyToPoint(0, maxY);
        canvas.draw(new Line2D.Double(yStart, yEnd));
    }

    protected void paintGraphics(Graphics2D canvas) {
        canvas.setStroke(graphicsStroke);
        canvas.setColor(Color.black);

        GeneralPath graph = new GeneralPath();
        for (int i = 0; i < graphicsData.length; i++) {
            Point2D.Double point = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
            if (i == 0) graph.moveTo(point.x, point.y);
            else graph.lineTo(point.x, point.y);
        }
        canvas.draw(graph);
    }

    protected void paintMarkers(Graphics2D canvas) {
        canvas.setStroke(new BasicStroke(1.0f));
        for (Double[] point : graphicsData) {
            Point2D.Double center = xyToPoint(point[0], point[1]);

            boolean eventRes = event(point[1]);
            if (eventRes) {
                canvas.setColor(Color.blue);
            } else {
                canvas.setColor(Color.RED);
            }

            GeneralPath romb = new GeneralPath();
            romb.moveTo(center.x , center.y + 11 );
            romb.lineTo(center.x + 11 , center.y );
            romb.lineTo(center.x, center.y - 11);
            romb.lineTo(center.x - 11, center.y );
            romb.closePath();
            canvas.draw(romb);
        }
    }

    private boolean event(double number) {
        int intPart = (int) Math.abs(number);
        while (intPart > 0) {
            int digit = intPart % 10;
            if (digit % 2 != 0) {
                return false;
            }
            intPart /= 10;
        }
        return true;
    }

    //экстремум
    public void setshowExtremums(boolean showExtremums) {
        this.showExtremums = showExtremums;
        repaint();
    }

    private List<Double[]> findLocalExtremums() {
        java.util.List<Double[]> extremums = new java.util.ArrayList<>();
        if (graphicsData == null || graphicsData.length < 3) return extremums;

        for (int i = 1; i < graphicsData.length - 1; i++) {
            double prevY = graphicsData[i - 1][1];
            double currY = graphicsData[i][1];
            double nextY = graphicsData[i + 1][1];
            if ((currY > prevY && currY > nextY) || (currY < prevY && currY < nextY)) {
                extremums.add(graphicsData[i]);
            }
        }
        return extremums;
    }
    protected void paintExtremums(Graphics2D canvas) {
        canvas.setColor(Color.pink);
        canvas.setStroke(new BasicStroke(1.5f));

        java.util.List<Double[]> extremums = findLocalExtremums();
        for (Double[] point : extremums) {
            Point2D.Double center = xyToPoint(point[0], point[1]);
            GeneralPath romb = new GeneralPath();
            romb.moveTo(center.x - 5, center.y + 5);
            romb.lineTo(center.x + 10, center.y + 5);
            romb.lineTo(center.x, center.y - 5);
            romb.lineTo(center.x - 5, center.y + 5);
            romb.closePath();
            canvas.draw(romb);
            canvas.fill(romb);

            String label = String.format("(%.2f, %.2f)", point[0], point[1]);
            canvas.drawString(label, (float) center.x + 12, (float) center.y - 12);
        }
    }

    public void setShowExtremums(boolean showExtremums) {
        this.showExtremums = showExtremums;
        repaint();
    }
}
