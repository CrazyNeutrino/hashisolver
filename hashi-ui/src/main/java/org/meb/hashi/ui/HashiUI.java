package org.meb.hashi.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.jsoup.helper.StringUtil;
import org.meb.hashi.engine.Hashi;
import org.meb.hashi.engine.StepsCounter;
import org.meb.hashi.engine.cfg.Globals;
import org.meb.hashi.engine.model.Edge;
import org.meb.hashi.engine.model.Node;
import org.meb.hashi.engine.model.Side;
import org.meb.hashi.engine.model.State;
import org.meb.hashi.engine.tool.MenneskeDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HashiUI extends JFrame {

	private static final Logger log = LoggerFactory.getLogger(HashiUI.class);

	private static final long serialVersionUID = 1L;
	private static String homePath;

	static {
		ConsoleAppender consoleAppender = new ConsoleAppender();
		consoleAppender.setLayout(new PatternLayout("%-5p %m%n"));
		consoleAppender.setThreshold(Level.INFO);
		consoleAppender.activateOptions();
		org.apache.log4j.Logger.getRootLogger().addAppender(consoleAppender);

		homePath = System.getProperty("hashi.home");
		if (StringUtil.isBlank(homePath)) {
			throw new IllegalStateException("hashi.home is not set");
		}
	}

	public static void main(String[] args) throws IOException {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		Hashi hashi = loadHashi(homePath);
		HashiUI hashiUI = new HashiUI();
		hashiUI.setTitle(hashi.getName());
		hashiUI.setLocation(300, 100);
		hashiUI.setResizable(false);
		hashiUI.addComponentsToPane(hashi);
		hashiUI.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		hashiUI.pack();
		hashiUI.setVisible(true);
	}

	private static Hashi loadHashi(String homePath) throws FileNotFoundException, IOException {
		Object[][] data = new Object[10][];
		data[0] = new Object[] { "superhard", 11, 283306 };
		data[1] = new Object[] { "superhard", 11, 133471 }; // ++
		data[2] = new Object[] { "hard", 25, 63 };
		data[3] = new Object[] { "hard", 13, 30868 };
		data[4] = new Object[] { "superhard", 13, 4776 }; // +++
		data[5] = new Object[] { "superhard", 13, 32435 }; // ++
		data[6] = new Object[] { "superhard", 13, 19472 }; // ++

		int idx = 6;
		String difficulty = (String) data[idx][0];
		int size = (Integer) data[idx][1];
		int number = (Integer) data[idx][2];

		return loadOrDownloadHashi(homePath, difficulty, size, number);
	}

	private static Hashi loadOrDownloadHashi(String homePath, String difficulty, int size, int number)
			throws MalformedURLException, IOException {

		String path = homePath + "/menneske/" + difficulty + "/" + size + "/" + number + ".txt";
		Hashi hashi = null;
		try {
			hashi = new Hashi(new FileInputStream(path), null);
		} catch (FileNotFoundException e) {
			log.info("Hashi file not found: {}, trying to download", path);
			new MenneskeDownloader(homePath).download(size, number);
			hashi = new Hashi(new FileInputStream(path), null);
		}
		return hashi.initialize();
	}

	private void addComponentsToPane(final Hashi hashi) {
		Container pane = getContentPane();
		pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));

		State state = hashi.getSolver().getState();
		Globals globals = hashi.getSolver().getGlobals();

		int dim;
		if (globals.xMax() <= 10) {
			dim = 400;
		} else if (globals.xMax() <= 15) {
			dim = 500;
		} else if (globals.xMax() <= 20) {
			dim = 600;
		} else {
			dim = 700;
		}

		final JTextArea leftTextArea = new JTextArea(10, 10);
		// leftTextArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		leftTextArea.setEditable(false);
		leftTextArea.setFont(new Font("Courier New", Font.PLAIN, 12));

		final JTextArea rightTextArea = new JTextArea(10, 10);
		// rightTextArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		rightTextArea.setEditable(false);
		rightTextArea.setFont(new Font("Courier New", Font.PLAIN, 12));

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new FlowLayout());
		pane.add(mainPanel);

		JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
		separator.setPreferredSize(new Dimension(0, 2));
		JPanel separatorPanel = new JPanel(new BorderLayout());
		separatorPanel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
		separatorPanel.add(separator);
		pane.add(separatorPanel, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.LINE_AXIS));
		bottomPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		pane.add(bottomPanel);
		// bottomPanel.add(separator);
		bottomPanel.add(new JScrollPane(leftTextArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
		// bottomPanel.add(Box.createHorizontalStrut(4));
		// bottomPanel.add(new JScrollPane(rightTextArea,
		// JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
		// JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));

		final HashiDrawPanel drawPanel = new HashiDrawPanel(state, globals);
		drawPanel.setPreferredSize(new Dimension(dim, dim));
		drawPanel.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent e) {
				HashiDrawPanel p = (HashiDrawPanel) e.getSource();
				Node node = p.getNodeAt(e.getPoint().x, e.getPoint().y);
				if (node != null) {
					StringBuilder sb = new StringBuilder();
					sb.append(node).append("\n");
					for (Side side : Side.values()) {
						Edge edge = node.edge(side);
						if (edge != null) {
							sb.append(side).append(": ").append(edge).append("\n");
						}
						Node neighbour = node.neighbour(side);
						if (neighbour != null) {
							sb.append(side).append(": ").append(neighbour).append("\n");
						}
					}
					leftTextArea.setText(sb.toString());
				}
			}

		});
		mainPanel.add(drawPanel);

		separator = new JSeparator(JSeparator.VERTICAL);
		separator.setPreferredSize(new Dimension(2, dim));
		mainPanel.add(separator);

		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new GridBagLayout());
		controlPanel.setPreferredSize(new Dimension(100, dim));
		mainPanel.add(controlPanel);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.weightx = 1;

		gbc.gridy = 0;
		JButton solveButton = new JButton("Solve");
		solveButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// hashi.getSolver().setUsePreventDeadGroups121(false);
				hashi.getSolver().solve();
				hashi.getSolver().getState().updateEdges();
				HashiUI.this.repaint();
			}
		});
		controlPanel.add(solveButton, gbc);

		gbc.gridy = 1;
		JButton nextButton = new JButton("Next");
		nextButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				hashi.getSolver().solve(new StepsCounter(1));
				hashi.getSolver().getState().updateEdges();
				HashiUI.this.repaint();
			}
		});
		controlPanel.add(nextButton, gbc);

		gbc.gridy = 2;
		JButton next5Button = new JButton("Next 5");
		next5Button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				hashi.getSolver().solve(new StepsCounter(5));
				hashi.getSolver().getState().updateEdges();
				HashiUI.this.repaint();
			}
		});
		controlPanel.add(next5Button, gbc);

		// gbc.gridy = 3;
		// JButton resetButton = new JButton("Reset");
		// resetButton.addActionListener(new ActionListener() {
		//
		// public void actionPerformed(ActionEvent e) {
		// Hashi hashi = null;
		// try {
		// hashi = loadHashi(homePath);
		// } catch (FileNotFoundException e1) {
		// throw new IllegalStateException(e1);
		// } catch (IOException e1) {
		// throw new IllegalStateException(e1);
		// }
		// HashiUI.this.getContentPane().removeAll();
		// HashiUI.this.addComponentsToPane(hashi);
		// HashiUI.this.repaint();
		// }
		// });
		// controlPanel.add(resetButton, gbc);
	}
}
