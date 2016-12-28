package org.meb.hashi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;

import org.meb.hashi.cfg.Globals;
import org.meb.hashi.model.Node;
import org.meb.hashi.schedule.BasicScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hashi {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(Hashi.class);

	private Solver solver;
	private String name;
	private InputStream stream;

	public static void main(String[] args) throws IOException {
		int idx = 0;

		String[] arr = new String[20];
		arr[idx++] = "/easy/11x11_247678.txt";
		arr[idx++] = "/easy/11x11_318964.txt";
		arr[idx++] = "/medium/11x11_232304.txt";
		arr[idx++] = "/medium/11x11_314196.txt";
		arr[idx++] = "/hard/11x11_177393.txt";

		arr[idx++] = "/hard/20x20_3694.txt";
		arr[idx++] = "/hard/25x25_17.txt";
		arr[idx++] = "/veryhard/25x25_27.txt";
		arr[idx++] = "/superhard/11x11_45740.txt"; // ***
		arr[idx++] = "/superhard/11x11_137264.txt";

		arr[idx++] = "/superhard/11x11_289825.txt";
		arr[idx++] = "/superhard/17x17_1415.txt";
		arr[idx++] = "/superhard/17x17_4540.txt";
		arr[idx++] = "/superhard/17x17_5654.txt";
		arr[idx++] = "/superhard/25x25_5.txt";

		int chosen = 5;
		InputStream stream = Hashi.class.getResourceAsStream(arr[chosen]);

		Hashi hashi = new Hashi(stream, arr[chosen]);
		hashi.initialize();
	}

	public Hashi(InputStream stream, String name) {
		this.name = name;
		this.stream = stream;
	}

	public void initialize() throws IOException {

		ArrayList<Node> nodesList = new ArrayList<Node>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		String line = reader.readLine();
		int max = Integer.parseInt(line);

		HashSet<Integer> lineNumbers = new HashSet<Integer>();

		while ((line = reader.readLine()) != null) {
			String[] tokens = line.split(",");
			int y = Integer.parseInt(tokens[0]);

			assert y >= 0 && y <= max : "line out of bounds: " + y;
			assert !lineNumbers.contains(y) : "duplicate line: " + y;
			assert tokens.length > 0 && tokens.length % 2 == 1 : "invalid tokens length for line: "
					+ y;

			for (int i = 1; i < tokens.length; i += 2) {
				int x = Integer.parseInt(tokens[i]);
				int weight = Integer.parseInt(tokens[i + 1]);
				Node node = new Node(x, y, weight);
				nodesList.add(node);
			}
		}

		stream.close();

		Node[] nodes = nodesList.toArray(new Node[nodesList.size()]);
		Globals globals = new Globals(max, max, nodes);
		globals.fillNeighbourNodes(nodes);

		solver = new Solver(nodes, globals, new BasicScheduler(nodes));
		solver.getState().updateEdges();
	}

	public String getName() {
		return name;
	}

	public Solver getSolver() {
		return solver;
	}
}
