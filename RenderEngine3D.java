/*This Java application is a simple 3D render engine that uses Java Swing for graphical user interface (GUI) components and custom rendering of 3D objects. It supports horizontal and vertical rotation of a 3D shape (tetrahedron) using sliders. The rendered object is displayed using the Graphics2D class. The program simulates shading by calculating the angle between the triangle’s normal and the Z-axis and uses a Z-buffer algorithm to handle hidden surface removal.
*
* Key Features:
*
* 1. Real-time 3D rendering
* 2. Adjustable horizontal and vertical rotations
* 3. Simple shading based on triangle normal
* 4. Subdivision of triangles for a more detailed mesh (inflation)*/

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

public class RenderEngine3D {

    public static void main(String[] args) {
        // Create a window with sliders for rotation control
        JFrame frame = new JFrame();
        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

        // Horizontal rotation slider (-180 to 180 degrees)
        JSlider headingSlider = new JSlider(-180, 180, 0);
        pane.add(headingSlider, BorderLayout.SOUTH);

        // Vertical rotation slider (-90 to 90 degrees)
        JSlider pitchSlider = new JSlider(SwingConstants.VERTICAL, -90, 90, 0);
        pane.add(pitchSlider, BorderLayout.EAST);

        // Panel to display the rendered object
        JPanel renderPanel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                // Setup the 2D drawing environment
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Define the 3D triangles (tetrahedron)
                List<Triangle> tris = createInitialTetrahedron();

                // Subdivide triangles for smoother rendering
                for (int i = 0; i < 4; i++) {
                    tris = inflate(tris);
                }

                // Apply rotation transformations based on slider values
                double heading = Math.toRadians(headingSlider.getValue());
                double pitch = Math.toRadians(pitchSlider.getValue());
                Matrix3 transform = createTransformationMatrix(heading, pitch);

                // Create an image and Z-buffer for rendering
                BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                double[] zBuffer = initializeZBuffer(img);

                // Render each triangle
                for (Triangle t : tris) {
                    renderTriangle(t, transform, img, zBuffer);
                }

                // Display the rendered image
                g2.drawImage(img, 0, 0, null);
            }
        };

        // Add panel to frame
        pane.add(renderPanel, BorderLayout.CENTER);

        // Add listeners for sliders to trigger re-rendering
        headingSlider.addChangeListener(e -> renderPanel.repaint());
        pitchSlider.addChangeListener(e -> renderPanel.repaint());

        // Finalize the frame setup
        frame.setSize(400, 400);
        frame.setVisible(true);
    }

    // Creates initial tetrahedron with 4 triangles
    public static List<Triangle> createInitialTetrahedron() {
        List<Triangle> tris = new ArrayList<>();
        tris.add(new Triangle(new Vertex(100, 100, 100),
                              new Vertex(-100, -100, 100),
                              new Vertex(-100, 100, -100),
                              Color.WHITE));
        tris.add(new Triangle(new Vertex(100, 100, 100),
                              new Vertex(-100, -100, 100),
                              new Vertex(100, -100, -100),
                              Color.RED));
        tris.add(new Triangle(new Vertex(-100, 100, -100),
                              new Vertex(100, -100, -100),
                              new Vertex(100, 100, 100),
                              Color.GREEN));
        tris.add(new Triangle(new Vertex(-100, 100, -100),
                              new Vertex(100, -100, -100),
                              new Vertex(-100, -100, 100),
                              Color.BLUE));
        return tris;
    }

    // Creates combined heading and pitch transformation matrix
    public static Matrix3 createTransformationMatrix(double heading, double pitch) {
        Matrix3 headingTransform = new Matrix3(new double[]{
            Math.cos(heading), 0, -Math.sin(heading),
            0, 1, 0,
            Math.sin(heading), 0, Math.cos(heading)
        });
        Matrix3 pitchTransform = new Matrix3(new double[]{
            1, 0, 0,
            0, Math.cos(pitch), Math.sin(pitch),
            0, -Math.sin(pitch), Math.cos(pitch)
        });
        return headingTransform.multiply(pitchTransform);
    }

    // Initializes the Z-buffer with negative infinity (far distance)
    public static double[] initializeZBuffer(BufferedImage img) {
        double[] zBuffer = new double[img.getWidth() * img.getHeight()];
        for (int i = 0; i < zBuffer.length; i++) {
            zBuffer[i] = Double.NEGATIVE_INFINITY;
        }
        return zBuffer;
    }

    // Renders a triangle onto the image, updating the Z-buffer
    public static void renderTriangle(Triangle t, Matrix3 transform, BufferedImage img, double[] zBuffer) {
        // Transform vertices
        Vertex v1 = transformAndCenter(t.v1, img);
        Vertex v2 = transformAndCenter(t.v2, img);
        Vertex v3 = transformAndCenter(t.v3, img);

        // Compute triangle's normal and shading factor
        Vertex normal = computeNormal(v1, v2, v3);
        double angleCos = Math.abs(normal.z);

        // Render the triangle using Z-buffer for depth management
        rasterizeTriangle(v1, v2, v3, t.color, img, zBuffer, angleCos);
    }

    // Transforms and centers a vertex on the screen
    public static Vertex transformAndCenter(Vertex v, BufferedImage img) {
        v.x += img.getWidth() / 2;
        v.y += img.getHeight() / 2;
        return v;
    }

    // Computes the normal vector of a triangle
    public static Vertex computeNormal(Vertex v1, Vertex v2, Vertex v3) {
        Vertex ab = new Vertex(v2.x - v1.x, v2.y - v1.y, v2.z - v1.z);
        Vertex ac = new Vertex(v3.x - v1.x, v3.y - v1.y, v3.z - v1.z);
        Vertex normal = new Vertex(ab.y * ac.z - ab.z * ac.y, ab.z * ac.x - ab.x * ac.z, ab.x * ac.y - ab.y * ac.x);
        double normalLength = Math.sqrt(normal.x * normal.x + normal.y * normal.y + normal.z * normal.z);
        normal.x /= normalLength;
        normal.y /= normalLength;
        normal.z /= normalLength;
        return normal;
    }

    // Rasterizes a triangle using the Z-buffer for hidden surface removal
    public static void rasterizeTriangle(Vertex v1, Vertex v2, Vertex v3, Color color, BufferedImage img, double[] zBuffer, double angleCos) {
        int minX = (int) Math.max(0, Math.ceil(Math.min(v1.x, Math.min(v2.x, v3.x))));
        int maxX = (int) Math.min(img.getWidth() - 1, Math.floor(Math.max(v1.x, Math.max(v2.x, v3.x))));
        int minY = (int) Math.max(0, Math.ceil(Math.min(v1.y, Math.min(v2.y, v3.y))));
        int maxY = (int) Math.min(img.getHeight() - 1, Math.floor(Math.max(v1.y, Math.max(v2.y, v3.y))));

        double triangleArea = (v1.y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - v1.x);

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                double b1 = ((y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - x)) / triangleArea;
                double b2 = ((y - v1.y) * (v3.x - v1.x) + (v3.y - v1.y) * (v1.x - x)) / triangleArea;
                double b3 = ((y - v2.y) * (v1.x - v2.x) + (v1.y - v2.y) * (v2.x - x)) / triangleArea;
                if (b1 >= 0 && b1 <= 1 && b2 >= 0 && b2 <= 1 && b3 >= 0 && b3 <= 1) {
                    double depth = b1 * v1.z + b2 * v2.z + b3 * v3.z;
                    int zIndex = y * img.getWidth() + x;
                    if (zBuffer[zIndex] < depth) {
                        img.setRGB(x, y, getShade(color, angleCos).getRGB());
                        zBuffer[zIndex] = depth;
                    }
                }
            }
        }
    }

    // Returns a shaded version of the input color based on the angle cosine
    public static Color getShade(Color color, double shadeFactor) {
        int r = (int) Math.round(color.getRed() * shadeFactor);
        int g = (int) Math.round(color.getGreen() * shadeFactor);
        int b = (int) Math.round(color.getBlue() * shadeFactor);
        return new Color(r, g, b);
    }

    // Triangle class representing a 3D triangle with vertices and color
    static class Triangle {
        Vertex v1, v2, v3;
        Color color;

        public Triangle(Vertex v1, Vertex v2, Vertex v3, Color color) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.color = color;
        }
    }

    // Vertex class representing a point in 3D space
    static class Vertex {
        double x, y, z;

        public Vertex(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    // Matrix3 class representing a 3x3 transformation matrix
    static class Matrix3 {
        double[] values;

        public Matrix3(double[] values) {
            this.values = values;
        }

        public Vertex multiply(Vertex in) {
            return new Vertex(
                    in.x * values[0] + in.y * values[1] + in.z * values[2],
                    in.x * values[3] + in.y * values[4] + in.z * values[5],
                    in.x * values[6] + in.y * values[7] + in.z * values[8]
            );
        }

        public Matrix3 multiply(Matrix3 m) {
            double[] result = new double[9];
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    result[row * 3 + col] = this.values[row * 3] * m.values[col] +
                                            this.values[row * 3 + 1] * m.values[col + 3] +
                                            this.values[row * 3 + 2] * m.values[col + 6];
                }
            }
            return new Matrix3(result);
        }
    }

    // Inflates a list of triangles by subdividing each into 4 smaller triangles
    public static List<Triangle> inflate(List<Triangle> tris) {
        List<Triangle> result = new ArrayList<>();
        for (Triangle tri : tris) {
            Vertex v1 = tri.v1;
            Vertex v2 = tri.v2;
            Vertex v3 = tri.v3;
            Vertex v12 = midpoint(v1, v2);
            Vertex v23 = midpoint(v2, v3);
            Vertex v31 = midpoint(v3, v1);

            result.add(new Triangle(v1, v12, v31, tri.color));
            result.add(new Triangle(v12, v2, v23, tri.color));
            result.add(new Triangle(v31, v23, v3, tri.color));
            result.add(new Triangle(v12, v23, v31, tri.color));
        }
        return result;
    }

    // Computes the midpoint between two vertices
    public static Vertex midpoint(Vertex v1, Vertex v2) {
        return new Vertex((v1.x + v2.x) / 2, (v1.y + v2.y) / 2, (v1.z + v2.z) / 2);
    }
}
