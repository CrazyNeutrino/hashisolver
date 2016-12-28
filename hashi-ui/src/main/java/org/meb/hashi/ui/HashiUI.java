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
import java.io.IOException;

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
import org.meb.hashi.engine.Hashi;
import org.meb.hashi.engine.cfg.Globals;
import org.meb.hashi.engine.model.Edge;
import org.meb.hashi.engine.model.Node;
import org.meb.hashi.engine.model.Side;
import org.meb.hashi.engine.model.State;

public class HashiUI extends JFrame {

	private static final long serialVersionUID = 1L;

	static {
		ConsoleAppender consoleAppender = new ConsoleAppender();
		consoleAppender.setLayout(new PatternLayout("%-5p %m%n"));
		consoleAppender.setThreshold(Level.INFO);
		consoleAppender.activateOptions();
		org.apache.log4j.Logger.getRootLogger().addAppender(consoleAppender);
	}

	public static void main(String[] args) throws IOException {
		int idx = 0;

		String level = "medium";
		String size = "11";
		String number = "66952";
		String path = "E:/Dropbox/hashi/menneske/" + level + "/" + size + "/" + number + ".txt";
		Hashi hashi = new Hashi(new FileInputStream(path), null);
		hashi.initialize();

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

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
				hashi.getSolver().solve(1);
				hashi.getSolver().getState().updateEdges();
				HashiUI.this.repaint();
			}
		});
		controlPanel.add(nextButton, gbc);

		gbc.gridy = 2;
		JButton next5Button = new JButton("Next 5");
		next5Button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				hashi.getSolver().solve(5);
				hashi.getSolver().getState().updateEdges();
				HashiUI.this.repaint();
			}
		});
		controlPanel.add(next5Button, gbc);
	}
}
