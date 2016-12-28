package org.meb.hashi.engine.tool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MenneskeDownloader {

	private final String URL_TEMPLATE = "http://www.menneske.no/hashi/eng/showpuzzle.html?number=&number;";
	private final String URL_TEMPLATE_RANDOM = "http://www.menneske.no/hashi/&size;x&size;/eng/random.html";

	private String homePath;

	public static void main(String[] args) throws MalformedURLException, IOException {
//		new MenneskeDownloader().downloadRandom(50);
		new MenneskeDownloader().download(66952);
	}

	public MenneskeDownloader() {
		this(null);
	}

	public MenneskeDownloader(String homePath) {
		if (homePath == null) {
			this.homePath = System.getProperty("hashi.home");
			if (StringUtils.isBlank(this.homePath)) {
				throw new IllegalStateException("hashi.home not set");
			}
			this.homePath += "/menneske";
		} else {
			this.homePath = homePath;
		}
	}

	public void downloadRandom(int quantity) throws MalformedURLException, IOException {
		int[] sizes = {7, 9, 11, 13, 17, 20, 25};
		Random random = new Random();
		for (int i = 0; i < quantity; i++) {
			int size = sizes[random.nextInt(sizes.length)];
			String url = URL_TEMPLATE_RANDOM.replace("&size;", Integer.toString(size));
			url = url.replace("/11x11", "");
			Document document = Jsoup.parse(new URL(url), 3000);
			parseAndSave(document);
		}
	}

	public void downloadRandom(int quantity, int size) throws MalformedURLException, IOException {
		for (int i = 0; i < quantity; i++) {
			String url = URL_TEMPLATE_RANDOM.replace("&size;", Integer.toString(size));
			url = url.replace("/11x11", "");
			Document document = Jsoup.parse(new URL(url), 3000);
			parseAndSave(document);
		}
	}

	public void download(int number) throws MalformedURLException, IOException {
		String url = URL_TEMPLATE.replace("&number;", Integer.toString(number));
		Document document = Jsoup.parse(new URL(url), 3000);
		parseAndSave(document);
	}

	public void parseAndSave(Document document) throws MalformedURLException, IOException {
		Elements hashiDiv = document.select("div.hashi");

		int number = parseNumber(hashiDiv);
		int size = parseSize(hashiDiv);
		String difficulty = parseDifficulty(hashiDiv);

		BufferedWriter writer = createWriter(number, size, difficulty);
		writer.write(Integer.toString(size));
		writer.newLine();

		Elements rows = hashiDiv.select("table tr");
		for (int i = 0; i < rows.size(); i++) {

			Element row = rows.get(i);
			Elements cells = row.getElementsByTag("td");
			if (cells.size() > 0) {
				writer.write(Integer.toString(i));

				for (int j = 0; j < cells.size(); j++) {
					Element cell = cells.get(j);
					if (cell.attr("class").equals("ring")) {
						String degree = cell.text();
						writer.write("," + j + "," + degree);
					}
				}

				writer.newLine();
			}
		}

		writer.flush();
		writer.close();
	}

	private BufferedWriter createWriter(int number, int size, String difficulty) throws IOException {
		File hashiDir = new File(homePath + "/" + difficulty + "/" + Integer.toString(size));
		if (hashiDir.exists()) {
			if (!hashiDir.isDirectory()) {
				throw new IllegalStateException("Not a directory");
			}
		} else {
			boolean success = hashiDir.mkdirs();
			if (!success) {
				throw new IllegalStateException("Unable to create target directory:" + hashiDir.getAbsolutePath());
			}
		}
		File hashiFile = new File(hashiDir, Integer.toString(number) + ".txt");
		
		return new BufferedWriter(new FileWriter(hashiFile));
	}

	private int parseNumber(Elements hashiDiv) {
		String number;
		
		Pattern p = Pattern.compile("(?i).+Showing puzzle number: ([0-9]+).+");
		Matcher m = p.matcher(hashiDiv.text());
		if (m.matches()) {
			number = m.group(1).toLowerCase().replace(" ", "");
		} else {
			throw new IllegalStateException("Unable to determine number");
		}
		
		return Integer.parseInt(number);
	}

	private int parseSize(Elements hashiDiv) {
		return hashiDiv.select("table tr").size();
	}

	private String parseDifficulty(Elements hashiDiv) {
		String difficulty;
		
		Pattern p = Pattern.compile("(?i).+Difficulty: (Very Easy|Easy|Medium|Hard|Very Hard|Super Hard).+");
		Matcher m = p.matcher(hashiDiv.text());
		if (m.matches()) {
			difficulty = m.group(1).toLowerCase().replace(" ", "");
		} else {
			throw new IllegalStateException("Unable to determine difficulty");
		}
		
		return difficulty;
	}
}
