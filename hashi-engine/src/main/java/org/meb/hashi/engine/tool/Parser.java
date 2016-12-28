package org.meb.hashi.engine.tool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

	public static void main(String[] args) throws IOException {
		new Parser().parseAndWrite();
	}

	public void parseAndWrite() throws IOException {
		InputStream stream = this.getClass().getResourceAsStream("/raw.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
//		String line = reader.readLine();

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));

		int lineNumber = 0;
		int fieldNumber = 0;

		Pattern p = Pattern.compile("[^0-9]+([0-9])[^0-9]+");

		String line;
		while ((line = reader.readLine()) != null) {
			if (line.startsWith("<tr")) {
				writer.write(Integer.toString(lineNumber));
				fieldNumber = 0;
				continue;
			} else if (line.startsWith("</tr")) {
				writer.newLine();
				lineNumber++;
				continue;
			} else if (line.startsWith("<td")) {
				if (line.contains("ring")) {
					Matcher m = p.matcher(line);
					if (m.matches()) {
						writer.write(",");
						writer.write(Integer.toString(fieldNumber));
						writer.write(",");
						writer.write(m.group(1));
					}
				}
				fieldNumber++;
			}
		}
		writer.flush();
	}
}
