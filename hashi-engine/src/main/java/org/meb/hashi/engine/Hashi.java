package org.meb.hashi.engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.meb.hashi.engine.cfg.Globals;
import org.meb.hashi.engine.model.Node;
import org.meb.hashi.engine.schedule.BasicScheduler;
import org.meb.hashi.engine.tool.HashiReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hashi {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(Hashi.class);

	private HashiSolver solver;
	private String name;
	private InputStream stream;

	public Hashi(InputStream stream, String name) {
		this.name = name;
		this.stream = stream;
	}

	public Hashi initialize() throws IOException {
		HashiReader hashiReader = new HashiReader();
		hashiReader.read(stream);
		List<Node> nodeList = hashiReader.getNodeList();
		int size = hashiReader.getSize();

		stream.close();

		Node[] nodes = nodeList.toArray(new Node[nodeList.size()]);
		Globals globals = new Globals(size - 1, size - 1, nodes);
		globals.fillNeighbourNodes(nodes);

		solver = new HashiSolver(nodes, globals, new BasicScheduler(nodes));
		solver.getState().updateEdges();
		
		return this;
	}

	public String getName() {
		return name;
	}

	public HashiSolver getSolver() {
		return solver;
	}
}
