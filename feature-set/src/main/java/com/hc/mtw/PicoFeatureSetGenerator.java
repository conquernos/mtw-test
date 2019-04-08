package com.hc.mtw;

import com.hc.mtw.feature.VisualFeature;
import org.conqueror.common.utils.db.DBConnector;
import org.conqueror.drone.selenium.webdriver.RGB;
import org.conqueror.drone.selenium.webdriver.WebBrowser;
import org.openqa.selenium.WebElement;

import java.net.URI;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.hc.mtw.TagExtractor.Tag;
import static com.hc.mtw.TagExtractor.Tags;
import static org.conqueror.drone.selenium.webdriver.WebBrowser.WebDriverName.CHROME;
import static org.conqueror.drone.selenium.webdriver.WebBrowser.getElementSize;
import static org.conqueror.drone.selenium.webdriver.WebBrowser.getNumberRatio;
import static org.conqueror.drone.selenium.webdriver.WebBrowser.getSpecialCharRatio;


public class PicoFeatureSetGenerator extends FeatureSetGenerator {

    public PicoFeatureSetGenerator(WebBrowser browser, Connection connection) {
        super(browser, connection, null);
    }

    @Override
    public void generate(String url, Tags tags, List<TagAndValue> tagAndValues, String insertQuery) {
        try {
            String domain = new URI(url).getHost();

            for (TagAndValue tagAndValue : tagAndValues) {
                extractAndInsert(domain, url, tags, tagAndValue, insertQuery);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void extractAndInsert(String domain, String url, Tags tags, TagAndValue tagAndValue, String insertQuery) throws SQLException {
        WebElement element = tagAndValue.getElement();
        VisualFeature feature = extractor.extractFeature(element);

        String tag = tagAndValue.getTagName();
        String value = element.getText();
        String xpath = browser.getXPathFromElement(element);
        String selector = browser.getCssSelectorFromElement(element);

        PreparedStatement statement = connection.prepareStatement(insertQuery);
        int num = 0;
        statement.setString(++num, url);
        statement.setString(++num, domain);
        statement.setString(++num, tag);
        statement.setString(++num, value);
        statement.setString(++num, xpath);
        statement.setString(++num, selector);
        statement.setInt(++num, feature.getWindowWidth());
        statement.setInt(++num, feature.getWindowHeight());
        statement.setInt(++num, feature.getW());
        statement.setInt(++num, feature.getH());
        statement.setInt(++num, feature.getX());
        statement.setInt(++num, feature.getY());
        statement.setInt(++num, feature.getR());
        statement.setInt(++num, feature.getG());
        statement.setInt(++num, feature.getB());
        statement.setFloat(++num, feature.getFontSize());
        statement.setString(++num, feature.getFontStyle());
        statement.setInt(++num, feature.getFontStyleNumber());
        statement.setString(++num, feature.getFontFamily());
        statement.setInt(++num, feature.getFontWeight());
        statement.setInt(++num, feature.getWordCount());
        statement.setInt(++num, value.length());
        statement.setFloat(++num, feature.getFontSize() / tags.getMaxFontSize());
        statement.setFloat(++num, (float) getElementSize(element) / tags.getMaxElementSize());
        statement.setFloat(++num, (float) RGB.distance(feature.getRGB(), tags.getAverageColor()));
        statement.setString(++num, tagAndValue.getLocation().name());
        statement.setInt(++num, tagAndValue.getLocation().ordinal());
        statement.setFloat(++num, getNumberRatio(value));
        statement.setFloat(++num, getSpecialCharRatio(value));
        statement.setFloat(++num, 0);
        statement.setFloat(++num, 0);
        statement.setFloat(++num, 0);
        statement.setFloat(++num, 0);
        statement.setFloat(++num, 0);
        statement.setFloat(++num, 0);
        statement.setFloat(++num, 0);
        statement.setFloat(++num, 0);

        statement.executeUpdate();
        statement.close();
    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
//        JdbcInfo jdbcInfo = new JdbcInfo(args[0], args[1], args[2]);
//        String selectQuery = args[3];
//        String insertQuery = args[4];
//        String jdbcUrl = "jdbc:mysql://localhost:3306/mtw?characterEncoding=UTF-8&serverTimezone=UTC";
//        String user = "mtw";
//        String password = "mtw!@#123";
        String jdbcUrl = args[0];
        String user = args[1];
        String password = args[2];
//        String selectQuery = "SELECT url, title, price, description FROM (SELECT url, site, title, price, description, @site_rank := IF(@current_site = site, @site_rank + 1, 1) AS site_rank, @current_site := site FROM pico ORDER BY site) ranked WHERE site_rank <= 10;";
//        String selectQuery = "SELECT url, title, price, description FROM pico_sample WHERE url = 'http://psyche.co.uk/men-c1/footwear-c10/springcourt-b2-canvas-hi-sneakers-p20193#attribute%5B1%5D=29'";
        String selectQuery = "SELECT url, title, price, description FROM pico_sample2 WHERE site IN (select domain from (select a.domain, count(*) cnt from (select domain, url from features3 group by domain, url) a group by a.domain) b where cnt < 10)";
        String insertQuery = "INSERT INTO features3 VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
//        System.setProperty("webdriver.chrome.driver", args[5]);

        Class.forName("com.mysql.jdbc.Driver");

        final int minClusters = 3;
        final int maxCenters = 3;
        final int attempts = 3;

        WebBrowser browser = WebBrowser.createWebBrowser(CHROME, true);
        browser.setWindowSize(1800, 1200);

        PicoTagExtractor extractor = new PicoTagExtractor(browser, minClusters, attempts);

        List<TagAndValue> tagAndValues = new ArrayList<>(30);

//        RuleBasedTagExtractor extractor = new RuleBasedTagExtractor(browser, 3, 3);

        try (DBConnector connector = new DBConnector(jdbcUrl, user, password)) {
            try (FeatureSetGenerator generator = new PicoFeatureSetGenerator(browser, connector.getConnection())) {
                try (ResultSet result = connector.select(selectQuery)) {
                    while (result.next()) {
                        try {
                            String url = result.getString(1);
                            String productName = result.getString(2).trim().toLowerCase(Locale.ENGLISH);
                            String productPrice = result.getString(3).trim();
                            String productDesc = result.getString(4).trim().toLowerCase(Locale.ENGLISH);
                            System.out.println(url);

//                            if (!browser.visit(url) || browser.isRedirected()) continue;
                            if (!browser.visit(url)) continue;
                            browser.waitForPageLoad();

                            Tags tags = extractor.extract(productName, productPrice, productDesc, maxCenters);
                            if (tags == null || !tags.hasPnames() || !tags.hasPrices() || !tags.hasDescs()) continue;

                            tagAndValues.add(new TagAndValue("product_name", tags.getPnames().get(0)));
                            tagAndValues.add(new TagAndValue("product_price", tags.getPrices().get(0)));
                            tagAndValues.add(new TagAndValue("product_desc", tags.getDescs().get(0)));

                            for (Tag tag : tags.getNones()) {
                                tagAndValues.add(new TagAndValue("none", tag));
                            }

                            generator.generate(url, tags, tagAndValues, insertQuery);
                            System.out.printf("=>%d\n", tagAndValues.size());

                            tagAndValues.clear();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

}
