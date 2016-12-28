package org.meb.hashi.engine.tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.meb.hashi.engine.model.Node;

public class HashiReader {

	private List<Node> nodeList;
	private int size;

	public void read(InputStream stream) throws IOException {
		nodeList = new ArrayList<Node>();

		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		String line = reader.readLine();
		size = Integer.parseInt(line);

		HashSet<Integer> lineNumbers = new HashSet<Integer>();

		while ((line = reader.readLine()) != null) {
			String[] tokens = line.split(",");
			int y = Integer.parseInt(tokens[0]);

			assert y >= 0 && y < size : "line out of bounds: " + y;
			assert !lineNumbers.contains(y) : "duplicate line: " + y;
			assert tokens.length > 0 && tokens.length % 2 == 1 : "invalid tokens length for line: " + y;

			for (int i = 1; i < tokens.length; i += 2) {
				int x = Integer.parseInt(tokens[i]);
				int weight = Integer.parseInt(tokens[i + 1]);
				Node node = new Node(x, y, weight);
				nodeList.add(node);
			}
		}
	}

	public List<Node> getNodeList() {
		return nodeList;
	}

	public int getSize() {
		return size;
	}
}
