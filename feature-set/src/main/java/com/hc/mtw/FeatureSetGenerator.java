package com.hc.mtw;

import com.hc.mtw.feature.VisualFeature;
import com.hc.mtw.feature.VisualFeatureExtractor;
import org.conqueror.common.utils.db.DBConnector;
import org.conqueror.common.utils.file.FileUtils;
import org.conqueror.drone.selenium.webdriver.RGB;
import org.conqueror.drone.selenium.webdriver.WebBrowser;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.*;

import static com.hc.mtw.TagExtractor.*;
import static org.conqueror.drone.selenium.webdriver.WebBrowser.WebDriverName.CHROME;
import static org.conqueror.drone.selenium.webdriver.WebBrowser.CHROME_DEFAULT_OPTIONS;
import static org.conqueror.drone.selenium.webdriver.WebBrowser.getElementSize;
import static org.conqueror.drone.selenium.webdriver.WebBrowser.getNumberRatio;
import static org.conqueror.drone.selenium.webdriver.WebBrowser.getSpecialCharRatio;


public class FeatureSetGenerator implements AutoCloseable {

	protected final WebBrowser browser;
	protected final VisualFeatureExtractor extractor;
	protected final Connection connection;

	protected final String htmlFileDir;

	public static class TagAndValue {

		private String tagName;
		private Tag tag;

		public TagAndValue(String tagName, Tag tag) {
			this.tagName = tagName;
			this.tag = tag;
		}

		public String getTagName() {
			return tagName;
		}

		public WebElement getElement() {
			return tag.getElement();
		}

		public WebBrowser.RelLocation getLocation() {
			return tag.getLocation();
		}

	}

	public FeatureSetGenerator(WebBrowser browser, Connection connection, String htmlFileDir) {
		this.browser = browser;

		extractor = new VisualFeatureExtractor(browser);
		this.connection = connection;

		this.htmlFileDir = htmlFileDir;
	}

	public void generate(String url, Tags tags, List<TagAndValue> tagAndValues, String insertQuery) {
		try {
			String domain = new URI(url).getHost();
			String path = "";
			if (htmlFileDir != null) {
				path = htmlFileDir + '/' + domain + '/' + toHash(url);
				String html = browser.getPageSource();

				FileUtils.writeContent(path, html, StandardOpenOption.CREATE);
			}

			for (TagAndValue tagAndValue : tagAndValues) {
				extractAndInsert(domain, url, path, tags, tagAndValue, insertQuery);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		browser.close();
	}

	private void extractAndInsert(String domain, String url, String path, Tags tags, TagAndValue tagAndValue, String insertQuery) throws SQLException {
		WebElement element = tagAndValue.getElement();
		VisualFeature feature = extractor.extractFeature(element);

		String htmlTagAttribute = browser.getHtmlTagAttributes(element);
		String htmlTag = element.getTagName();

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
		statement.setString(++num, htmlTag);
		statement.setString(++num, htmlTagAttribute);
		statement.setString(++num, path);

		statement.executeUpdate();
		statement.close();
	}

	private static boolean isFilteredOut(VisualFeature feature) {
		return feature.getX() > feature.getWindowWidth() || feature.getX() == 0
			|| feature.getY() > feature.getWindowHeight() || feature.getY() == 0
			|| feature.getW() > feature.getWindowWidth() || feature.getW() == 0
			|| feature.getH() > feature.getWindowHeight() || feature.getH() == 0;
	}

	private static String toHash(String url) throws NoSuchAlgorithmException {
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		md5.update(StandardCharsets.UTF_8.encode(url));
		return String.format("%032x", new BigInteger(1, md5.digest()));
	}


	public static void main(String[] args) throws URISyntaxException, IOException, SQLException, NoSuchAlgorithmException, ClassNotFoundException {
		String jdbcUrl = args[0];
		String user = args[1];
		String password = args[2];
		String selectQuery = args[3];
		String insertQuery = args[4];
		String htmlFilePath = args[5].length() > 0 ? args[5] : null;

//        String selectQuery = "select w_url, specdocfromeditor_pname, specdocfromeditor_pprice, specdocfromeditor_pdesc from top_n_site_and_url_2 left join features3 features on top_n_site_and_url_2.w_url = features.url where features.url is null";
//        String insertQuery = "insert into features3 values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
//        String htmlFilePath = "/tmp";

		System.setProperty("webdriver.chrome.driver", args[6]);
//        System.setProperty("DISPLAY", args[7]);

		WebBrowser browser = WebBrowser.createWebBrowser(CHROME, CHROME_DEFAULT_OPTIONS, true);
		browser.setWindowSize(1800, 1200);

		List<TagAndValue> tagAndValues = new ArrayList<>(30);

		RuleBasedTagExtractor extractor = new RuleBasedTagExtractor(browser, 3, 3);

		try (DBConnector connector = new DBConnector(jdbcUrl, user, password)) {
			try (FeatureSetGenerator generator = new FeatureSetGenerator(browser, connector.getConnection(), htmlFilePath)) {
				try (ResultSet result = connector.select(selectQuery)) {
					while (result.next()) {
						String url = result.getString(1);
						System.out.println(url);

						if (!browser.visit(url) || browser.isRedirected()) continue;

						Tags tags = extractor.extract(url, 3);
						if (tags == null || !tags.hasPnames() || !tags.hasPrices() || !tags.hasDescs()) continue;

						if (tags.hasPnames())
							tagAndValues.add(new TagAndValue("product_name", tags.getPnames().get(0)));
						if (tags.hasDescs()) tagAndValues.add(new TagAndValue("product_desc", tags.getDescs().get(0)));
						if (tags.hasPrices()) {
							for (Tag tag : tags.getPrices()) {
								tagAndValues.add(new TagAndValue("product_price", tag));
							}
						}
						for (Tag tag : tags.getNones()) {
							tagAndValues.add(new TagAndValue("none", tag));
						}

						generator.generate(url, tags, tagAndValues, insertQuery);
						System.out.printf("=>%d\n", tagAndValues.size());

						tagAndValues.clear();
					}
				}
			}
		}
	}

}
