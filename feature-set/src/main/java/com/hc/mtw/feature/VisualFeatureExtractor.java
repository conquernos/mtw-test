package com.hc.mtw.feature;

//import nu.pattern.OpenCV;
//import org.apache.commons.io.FileUtils;
//import org.opencv.core.Mat;
//import org.opencv.core.MatOfKeyPoint;
//import org.opencv.core.Rect;
//import org.opencv.features2d.DescriptorExtractor;
//import org.opencv.features2d.FeatureDetector;
import org.conqueror.drone.selenium.webdriver.RGB;
    import org.conqueror.drone.selenium.webdriver.WebBrowser;
import org.openqa.selenium.*;

import static org.conqueror.drone.selenium.webdriver.WebBrowser.*;


public class VisualFeatureExtractor {

    private final int width;
    private final int height;

    public VisualFeatureExtractor(WebBrowser browser) {
        this.width = browser.getWindowWidth();
        this.height = browser.getWindowHeight();
    }

    public VisualFeature extractFeature(WebElement element) {
        Point point = element.getLocation();
        Dimension dimension = element.getSize();
        RGB rgb = getFontColor(element);

        return new VisualFeature(
            width
            , height
            , point.getX()
            , point.getY()
            , dimension.getWidth()
            , dimension.getHeight()
            , rgb.getR()
            , rgb.getG()
            , rgb.getB()
            , getFontSize(element)
            , getFontStyle(element)
            , getFontFamily(element)
            , getFontWeight(element)
            , getWordCount(element));
    }



//    private Mat extractImageFeature(Mat image) {
//        // Declare key point of images
//        MatOfKeyPoint keypoints = new MatOfKeyPoint();
//        Mat descriptors = new Mat();
//
//        // Definition of ORB key point detector and descriptor extractors
//        FeatureDetector detector = FeatureDetector.create(FeatureDetector.SURF);
//        DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.SURF);
//
//        // Detect key points
//        detector.detect(image, keypoints);
//
//        // Extract descriptors
//        extractor.compute(image, keypoints, descriptors);
//
//        return descriptors;
//    }



}

