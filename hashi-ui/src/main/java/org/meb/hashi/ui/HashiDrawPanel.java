package org.meb.hashi.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.util.HashMap;

import javax.swing.JPanel;

import org.meb.hashi.engine.cfg.Globals;
import org.meb.hashi.engine.model.Edge;
import org.meb.hashi.engine.model.Group;
import org.meb.hashi.engine.model.Node;
import org.meb.hashi.engine.model.State;

public class HashiDrawPanel extends JPanel {

	private static final long serialVersionUID = -3543590513553062113L;
	private static final String[] colorCodes = { "FF0000", "009900", "FF00FF", "8E8ECC", "FF7700",
			"FF66CC", "005CE6", "00FF00", "C7C71F", "5C8533" };

	private State state;
	private Globals globals;

	private HashMap<Group, Color> groupColors = new HashMap<Group, Color>();

	private int xMargin;
	private int yMargin;
	private int xScale;
	private int yScale;

	public HashiDrawPanel(State state, Globals globals) {
		this.state = state;
		this.globals = globals;
	}

	public State getState() {
		return state;
	}

	public Node getNodeAt(int x, int y) {
		int nodeX = (int)(x - xMargin) / xScale;
		int nodeY = (int)(y - yMargin) / yScale;
		return globals.nodeAt(nodeX, nodeY);
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

		Dimension dim = getSize();
		xMargin = 20;
		yMargin = 20;
		xScale = (dim.width - 2 * xMargin) / (globals.xMax() + 1);
		yScale = (dim.height - 2 * yMargin) / (globals.yMax() + 1);

		Font legendFont = new Font("Arial", Font.PLAIN, 11);
		g2.setFont(legendFont);
		FontMetrics legendFm = g2.getFontMetrics(legendFont);
		g2.setPaint(Color.GRAY);

		for (int i = 0; i <= globals.xMax(); i++) {
			String string = Integer.toString(i);
			int w = legendFm.stringWidth(string);
			int h = legendFm.getAscent();
			g2.drawString(string, (int) ((i + 0.5) * xScale + xMargin - w / 2.0f),
					(int) (yMargin / 2.0f + h / 2.0f));
			g2.drawString(string, (int) ((i + 0.5) * xScale + xMargin - w / 2.0f),
					(int) (dim.height - yMargin / 2.0f + h / 2.0f));
		}

		for (int i = 0; i <= globals.yMax(); i++) {
			String string = Integer.toString(i);
			int w = legendFm.stringWidth(string);
			int h = legendFm.getAscent();
			g2.drawString(string, (int) (xMargin / 2.0f - w / 2.0f), (int) ((i + 0.5) * yScale
					+ yMargin + h / 2.0f));
			g2.drawString(string, (int) (dim.width - xMargin / 2.0f - w / 2.0f), (int) ((i + 0.5)
					* xScale + xMargin + h / 2.0f));
		}

		g2.setPaint(Color.BLACK);
		for (Edge edge : state.getEdges()) {
			g2.setPaint(Color.BLACK);
			if (edge.isComplete()) {
				g2.setStroke(new BasicStroke(2));
			} else {
				// g2.setPaint(Color.RED);
				g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0,
						new float[] { 3 }, 0));
			}
			int startX = (int) ((edge.node1().position.x + 0.5) * xScale + xMargin);
			int startY = (int) ((edge.node1().position.y + 0.5) * yScale + yMargin);
			int endX = (int) ((edge.node2().position.x + 0.5) * xScale + xMargin);
			int endY = (int) ((edge.node2().position.y + 0.5) * yScale + yMargin);

			int halfRange = edge.degree() - 1;
			for (int i = halfRange * -1; i <= halfRange; i += 2) {
				if (edge.isHorizontal()) {
					g2.drawLine(startX, startY + (i * 2), endX, endY + (i * 2));
				} else if (edge.isVertical()) {
					g2.drawLine(startX + (i * 2), startY, endX + (i * 2), endY);
				} else {
					assert false;
				}
			}

		}

		for (Group group : state.getGroups()) {
			if (group.getNodesCount() >= 3 && !groupColors.containsKey(group)) {
				String colorCode = colorCodes[groupColors.size() % colorCodes.length];
				groupColors.put(group, new Color(Integer.parseInt(colorCode, 16)));
			}
		}

		g2.setPaint(Color.BLACK);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setStroke(new BasicStroke(2));
		Font font = new Font("Arial", Font.BOLD, (int) (Math.min(xScale, yScale) * 0.5));
		g2.setFont(font);
		FontMetrics fm = g2.getFontMetrics(font);

		Paint fillOvalPaint;
		Paint drawOvalPaint;

		for (Node node : state.getNodes()) {

			if (node.equals(state.getLastSolvedNode())) {
				Color fillOvalColor = groupColors.get(node.group());
				if (fillOvalColor == null) {
					fillOvalColor = Color.BLACK;
				}
				fillOvalPaint = fillOvalColor;
//				fillOvalPaint = new RadialGradientPaint((node.coords.x + 0.5f) * xScale + xMargin,
//						(node.coords.y + 0.5f) * yScale + yMargin, xScale / 2.0f, new float[] {
//								0.4f, 1.0f }, new Color[] { Color.WHITE, fillOvalColor });
			} else {
				fillOvalPaint = Color.WHITE;
			}

			g2.setPaint(fillOvalPaint);
			g2.fillOval(node.position.x * xScale + xMargin, node.position.y * yScale + yMargin, xScale,
					yScale);

			drawOvalPaint = groupColors.get(node.group());
			if (drawOvalPaint == null) {
				drawOvalPaint = Color.BLACK;
			}

			g2.setPaint(drawOvalPaint);
			g2.drawOval(node.position.x * xScale + xMargin, node.position.y * yScale + yMargin, xScale,
					yScale);
			String string = String.valueOf(node.initialDegree());
			int w = fm.stringWidth(string);
			int h = fm.getAscent();
			g2.setPaint(fillOvalPaint == Color.BLACK ? Color.WHITE : Color.BLACK);
			g2.drawString(string,
					(int) ((node.position.x + 0.5) * xScale + xMargin - (w / 2.0f) + 1),
					(int) ((node.position.y + 0.5) * yScale + yMargin + (h / 2.0f)));
		}
	}
}
