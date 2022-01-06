package fmg.jfx.utils;

import java.util.List;

import javafx.geometry.Bounds;
import javafx.scene.shape.*;
import javafx.scene.text.Text;

public class ShapeConverter {

    private static double CONTROL_POINT_DISTANCE = 4.0/3.0 * (Math.sqrt(2)-1);

    public static String toSvg(Shape shape) {
        StringBuilder fxPath = new StringBuilder();
        Class<? extends Shape> shapeClass = shape.getClass();
        if (Line.class == shapeClass)
            fxPath.append(convertLine((Line)shape));
        else if (Arc.class == shapeClass)
            fxPath.append(convertArc((Arc)shape));
        else if (QuadCurve.class == shapeClass)
            fxPath.append(convertQuadCurve((QuadCurve)shape));
        else if (CubicCurve.class == shapeClass)
            fxPath.append(convertCubicCurve((CubicCurve)shape));
        else if (Rectangle.class == shapeClass)
            fxPath.append(convertRectangle((Rectangle)shape));
        else if (Circle.class == shapeClass)
            fxPath.append(convertCircle((Circle)shape));
        else if (Ellipse.class == shapeClass)
            fxPath.append(convertEllipse((Ellipse)shape));
        else if (Text.class == shapeClass)
            fxPath.append(convertPath((Path)Shape.subtract(shape, new Rectangle(0, 0))));
        else if (Path.class == shapeClass)
            fxPath.append(convertPath((Path)shape));
        else if (Polygon.class == shapeClass)
            fxPath.append(convertPolygon((Polygon)shape));
        else if (Polyline.class == shapeClass)
            fxPath.append(convertPolyline((Polyline)shape));
        else if (SVGPath.class == shapeClass)
            fxPath.append(((SVGPath) shape).getContent());
        else
            throw new RuntimeException("Unsupported shape class " + shapeClass.getSimpleName());
        return fxPath.toString();
    }

    public static SVGPath toSvgPath(Shape shape) {
        SVGPath svgGPath = new SVGPath();
        svgGPath.setContent(toSvg(shape));
        return svgGPath;
    }

    public static String convertLine(Line line) {
        StringBuilder fxPath = new StringBuilder();
        fxPath.append("M ").append(line.getStartX()).append(" ").append(line.getStartY()).append(" ")
              .append("L ").append(line.getEndX  ()).append(" ").append(line.getEndY());
        return fxPath.toString();
    }

    public static String convertArc(Arc arc) {
        StringBuilder fxPath = new StringBuilder();
        double centerX    = arc.getCenterX();
        double centerY    = arc.getCenterY();
        double radiusX    = arc.getRadiusX();
        double radiusY    = arc.getRadiusY();
        double startAngle = arc.getStartAngle();
        double length     = arc.getLength();
        double alpha      = arc.getLength() + startAngle;
        startAngle        = Math.toRadians(startAngle);
        alpha             = Math.toRadians(alpha);
        double phiOffset  = Math.toRadians(-90); // -90 needed for JavaFX

        double startX = centerX + Math.cos(phiOffset) * radiusX * Math.cos(startAngle) + Math.sin(-phiOffset) * radiusY * Math.sin(startAngle);
        double startY = centerY + Math.sin(phiOffset) * radiusX * Math.cos(startAngle) + Math.cos(phiOffset) * radiusY * Math.sin(startAngle);

        double endX   = centerX + Math.cos(phiOffset) * radiusX * Math.cos(alpha) + Math.sin(-phiOffset) * radiusY * Math.sin(alpha);
        double endY   = centerY + Math.sin(phiOffset) * radiusX * Math.cos(alpha) + Math.cos(phiOffset) * radiusY * Math.sin(alpha);

        int xAxisRot  = 0;
        int largeArc  = (length > 180) ? 1 : 0;
        int sweep     = (length > 0) ? 1 : 0;

        fxPath.append("M ").append(centerX).append(" ").append(centerY).append(" ");
        if (ArcType.ROUND == arc.getType()) {
            fxPath.append("h ").append(startX - centerX).append(" v ").append(startY - centerY);
        }
        fxPath.append("A ").append(radiusX).append(" ").append(radiusY).append(" ")
              .append(xAxisRot).append(" ").append(largeArc).append(" ").append(sweep).append(" ")
              .append(endX).append(" ").append(endY).append(" ");
        if (ArcType.CHORD == arc.getType() || ArcType.ROUND == arc.getType()) {
            fxPath.append("Z");
        }
        return fxPath.toString();
    }

    public static String convertQuadCurve(QuadCurve quadCurve) {
        StringBuilder fxPath = new StringBuilder();
        fxPath.append("M ").append(quadCurve.getStartX()).append(" ").append(quadCurve.getStartY()).append(" ")
              .append("Q ").append(quadCurve.getControlX()).append(" ").append(quadCurve.getControlY())
              .append(quadCurve.getEndX()).append(" ").append(quadCurve.getEndY());
        return fxPath.toString();
    }

    public static String convertCubicCurve(CubicCurve cubicCurve) {
        StringBuilder fxPath = new StringBuilder();
        fxPath.append("M ").append(cubicCurve.getStartX()).append(" ").append(cubicCurve.getStartY()).append(" ")
              .append("C ").append(cubicCurve.getControlX1()).append(" ").append(cubicCurve.getControlY1()).append(" ")
              .append(cubicCurve.getControlX2()).append(" ").append(cubicCurve.getControlY2()).append(" ")
              .append(cubicCurve.getEndX()).append(" ").append(cubicCurve.getEndY());
        return fxPath.toString();
    }

    public static String convertRectangle(Rectangle rect) {
        StringBuilder fxPath = new StringBuilder();
        Bounds        bounds = rect.getBoundsInLocal();
        if (Double.compare(rect.getArcWidth(), 0.0) == 0 && Double.compare(rect.getArcHeight(), 0.0) == 0) {
            fxPath.append("M ").append(bounds.getMinX()).append(" ").append(bounds.getMinY()).append(" ")
                  .append("H ").append(bounds.getMaxX()).append(" ")
                  .append("V ").append(bounds.getMaxY()).append(" ")
                  .append("H ").append(bounds.getMinX()).append(" ")
                  .append("V ").append(bounds.getMinY()).append(" ")
                  .append("Z");
        } else {
            double x         = bounds.getMinX();
            double y         = bounds.getMinY();
            double width     = bounds.getWidth();
            double height    = bounds.getHeight();
            double arcWidth  = rect.getArcWidth();
            double arcHeight = rect.getArcHeight();
            double r         = x + width;
            double b         = y + height;
            fxPath.append("M ").append(x + arcWidth).append(" ").append(y).append(" ")
                  .append("L ").append(r - arcWidth).append(" ").append(y).append(" ")
                  .append("Q ").append(r).append(" ").append(y).append(" ").append(r).append(" ").append(y + arcHeight).append(" ")
                  .append("L ").append(r).append(" ").append(y + height - arcHeight).append(" ")
                  .append("Q ").append(r).append(" ").append(b).append(" ").append(r - arcWidth).append(" ").append(b).append(" ")
                  .append("L ").append(x + arcWidth).append(" ").append(b).append(" ")
                  .append("Q ").append(x).append(" ").append(b).append(" ").append(x).append(" ").append(b - arcHeight).append(" ")
                  .append("L ").append(x).append(" ").append(y + arcHeight).append(" ")
                  .append("Q ").append(x).append(" ").append(y).append(" ").append(x + arcWidth).append(" ").append(y).append(" ")
                  .append("Z");
        }
        return fxPath.toString();
    }

    private static String convertEllipse(double centerX,
                                         double centerY,
                                         double radiusX,
                                         double radiusY,
                                         double controlDistanceX,
                                         double controlDistanceY
    ) {
        StringBuilder fxPath = new StringBuilder();
        // Move to first point
        fxPath.append("M ").append(centerX).append(" ").append(centerY - radiusY).append(" ");
        // 1. quadrant
        fxPath.append("C ").append(centerX + controlDistanceX).append(" ").append(centerY - radiusY).append(" ")
              .append(centerX + radiusX).append(" ").append(centerY - controlDistanceY).append(" ")
              .append(centerX + radiusX).append(" ").append(centerY).append(" ");
        // 2. quadrant
        fxPath.append("C ").append(centerX + radiusX).append(" ").append(centerY + controlDistanceY).append(" ")
              .append(centerX + controlDistanceX).append(" ").append(centerY + radiusY).append(" ")
              .append(centerX).append(" ").append(centerY + radiusY).append(" ");
        // 3. quadrant
        fxPath.append("C ").append(centerX - controlDistanceX).append(" ").append(centerY + radiusY).append(" ")
              .append(centerX - radiusX).append(" ").append(centerY + controlDistanceY).append(" ")
              .append(centerX - radiusX).append(" ").append(centerY).append(" ");
        // 4. quadrant
        fxPath.append("C ").append(centerX - radiusX).append(" ").append(centerY - controlDistanceY).append(" ")
              .append(centerX - controlDistanceX).append(" ").append(centerY - radiusY).append(" ")
              .append(centerX).append(" ").append(centerY - radiusY).append(" ");
        // Close path
        fxPath.append("Z");
        return fxPath.toString();
    }

    public static String convertCircle(Circle circle) {
        double centerX = circle.getCenterX() == 0 ? circle.getRadius() : circle.getCenterX();
        double centerY = circle.getCenterY() == 0 ? circle.getRadius() : circle.getCenterY();
        double radius  = circle.getRadius();
        double controlDistance = radius * CONTROL_POINT_DISTANCE;
        return convertEllipse(centerX, centerY, radius, radius, controlDistance, controlDistance);
    }

    public static String convertEllipse(Ellipse ellipse) {
        double centerX = ellipse.getCenterX() == 0 ? ellipse.getRadiusX() : ellipse.getCenterX();
        double centerY = ellipse.getCenterY() == 0 ? ellipse.getRadiusY() : ellipse.getCenterY();
        double radiusX = ellipse.getRadiusX();
        double radiusY = ellipse.getRadiusY();
        double controlDistanceX = radiusX * CONTROL_POINT_DISTANCE;
        double controlDistanceY = radiusY * CONTROL_POINT_DISTANCE;
        return convertEllipse(centerX, centerY, radiusX, radiusY, controlDistanceX, controlDistanceY);
    }

    public static String convertPath(Path path) {
        StringBuilder fxPath = new StringBuilder();
        for (PathElement element : path.getElements()) {
            Class<? extends PathElement> elementClass = element.getClass();
            if (MoveTo.class == elementClass) {
                fxPath.append("M ")
                      .append(((MoveTo) element).getX()).append(" ")
                      .append(((MoveTo) element).getY()).append(" ");
            } else if (LineTo.class == elementClass) {
                fxPath.append("L ")
                      .append(((LineTo) element).getX()).append(" ")
                      .append(((LineTo) element).getY()).append(" ");
            } else if (CubicCurveTo.class == elementClass) {
                fxPath.append("C ")
                      .append(((CubicCurveTo) element).getControlX1()).append(" ")
                      .append(((CubicCurveTo) element).getControlY1()).append(" ")
                      .append(((CubicCurveTo) element).getControlX2()).append(" ")
                      .append(((CubicCurveTo) element).getControlY2()).append(" ")
                      .append(((CubicCurveTo) element).getX()).append(" ")
                      .append(((CubicCurveTo) element).getY()).append(" ");
            } else if (QuadCurveTo.class == elementClass) {
                fxPath.append("Q ")
                      .append(((QuadCurveTo) element).getControlX()).append(" ")
                      .append(((QuadCurveTo) element).getControlY()).append(" ")
                      .append(((QuadCurveTo) element).getX()).append(" ")
                      .append(((QuadCurveTo) element).getY()).append(" ");
            } else if (ArcTo.class == elementClass) {
                fxPath.append("A ")
                      .append(((ArcTo) element).getX()).append(" ")
                      .append(((ArcTo) element).getY()).append(" ")
                      .append(((ArcTo) element).getRadiusX()).append(" ")
                      .append(((ArcTo) element).getRadiusY()).append(" ");
            } else if (HLineTo.class == elementClass) {
                fxPath.append("H ")
                      .append(((HLineTo) element).getX()).append(" ");
            } else if (VLineTo.class == elementClass) {
                fxPath.append("V ")
                      .append(((VLineTo) element).getY()).append(" ");
            } else if (ClosePath.class == elementClass) {
                fxPath.append("Z");
            } else
                throw new RuntimeException("Unsupported PathElement class " + elementClass.getSimpleName());
        }
        return fxPath.toString();
    }

    private static String convertPoly(List<Double> points, boolean itsClose) {
        StringBuilder fxPath = new StringBuilder();
        for (int i = 0 ; i < points.size(); i += 2)
            fxPath.append(i == 0 ? "M " : "L ")
                  .append(points.get(i)).append(" ")
                  .append(points.get(i + 1)).append(" ");
        fxPath.append("Z");
        return fxPath.toString();
    }
    public static String convertPolygon(Polygon polygon) {
        return convertPoly(polygon.getPoints(), true);
    }
    public static String convertPolyline(Polyline polyline) {
        return convertPoly(polyline.getPoints(), false);
    }

}
