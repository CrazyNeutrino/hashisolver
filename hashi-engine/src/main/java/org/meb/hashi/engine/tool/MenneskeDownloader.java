package org.meb.hashi.tool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MenneskeHashiLoader {

	private final String URL_TEMPLATE = "http://www.menneske.no/hashi/&size;x&size;/eng/showpuzzle.html?number=&num;";
	private final String PATH_TEMPLATE = "src/main/resources/&diff;/&size;x&size;_&num;.txt";

	private int size;
	private int num;

	public static void main(String[] args) throws MalformedURLException, IOException {
		new MenneskeHashiLoader(17, 9522).loadAndWrite();
	}

	public MenneskeHashiLoader(int size, int num) {
		this.size = size;
		this.num = num;
	}

	public void loadAndWrite() throws MalformedURLException, IOException {
		String url = URL_TEMPLATE.replace("&size;", Integer.toString(size)).replace("&num;",
				Integer.toString(num));
		if (size == 11) {
			url = url.replace("/11x11", "");
		}

		Document doc = Jsoup.parse(new URL(url), 3000);
		Elements hashiDiv = doc.select("div.hashi");

		String diff;
		Pattern p = Pattern.compile("(?i).+Difficulty: (Very Easy|Easy|Medium|Hard|Very Hard|Super Hard).+");
		Matcher m = p.matcher(hashiDiv.text());
		if (m.matches()) {
			diff = m.group(1).toLowerCase().replace(" ", "");
		} else {
			throw new IllegalStateException("Unable to determine difficulty");
		}

		String path = PATH_TEMPLATE.replace("&size;", Integer.toString(size))
				.replace("&num;", Integer.toString(num)).replace("&diff;", diff);

		File file = new File(path);
		if (file.exists()) {
			throw new IllegalStateException("File exists: " + path);
		}

		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.write(Integer.toString(size - 1));
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
}
