package com.hc.mtw.common.cv;

import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.List;


public class ImageUtils {

    public static final Scalar RED = new Scalar(0F, 0F, 255F);
    public static final Scalar GREEN = new Scalar(0F, 255F, 0F);
    public static final Scalar BLUE = new Scalar(255F, 0F, 0F);

    static {
        OpenCV.loadShared();
    }

    public static Mat transToMat(byte[] imageBytes) {
        return Imgcodecs.imdecode(new MatOfByte(imageBytes), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
    }

    public static Mat cropImage(Mat image, int x, int y, int w, int h) {
        Rect crop = new Rect(x, y, w, h);
        return new Mat(image, crop);
    }

    public static void drawRectangle(Mat image, RectPoint point, int thickness, Scalar color) {
//        Imgproc.rectangle(image, new Point(x, y), new Point(x+w, y+h), RED, 5, COLOR_GRAY2BGR, 0);
        Imgproc.rectangle(image, point.getStartPoint(), point.getEndPoint(), color, thickness);
    }

    public static void drawRectangles(Mat image, List<RectPoint> points, int thickness, Scalar color) {
        for (RectPoint point : points) {
            drawRectangle(image, point, thickness, color);
        }
    }

    public static void drawRectangle(BufferedImage image, RectPoint point, int thickness, Color color) {
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(thickness));
        g2d.drawRect(point.getX(), point.getY(), point.getWidth(), point.getHeight());
        g2d.dispose();
    }

    public static BufferedImage resize(BufferedImage image, int w, int h) {
        Image tmp = image.getScaledInstance(w, h, Image.SCALE_FAST);
        BufferedImage resizedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return resizedImage;
    }

    public static BufferedImage transMatToBufferedImage(Mat m) {
        int channels = m.channels();
        int type = 0;
        switch (channels) {
            case 1:
                type = BufferedImage.TYPE_BYTE_GRAY;
                break;
            case 3:
                type = BufferedImage.TYPE_3BYTE_BGR;
                break;
            case 4:
                type = BufferedImage.TYPE_4BYTE_ABGR;
                break;
        }
        int bufferSize = m.channels() * m.cols() * m.rows();
        byte[] b = new byte[bufferSize];
        m.get(0, 0, b); // get all the pixels

        BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);

        return image;
    }

    public static void displayImage(Image image) {
        JLabel lbl = new JLabel();
        lbl.setIcon(new ImageIcon(image));

        JFrame frame = new JFrame();
        frame.setLayout(new FlowLayout());
        frame.setSize(image.getWidth(null) + 50, image.getHeight(null) + 50);
        frame.add(lbl);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void writeImage(Mat image, String file) {
        Imgcodecs.imwrite(file, image);
    }

    public static void writeImage(BufferedImage image, String file) throws IOException {
        ImageIO.write(image, "png", new File(file));
    }

}
