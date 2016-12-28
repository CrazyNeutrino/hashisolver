package org.meb.hashi.engine.tool;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.meb.hashi.engine.Hashi;

public class BatchHashiSolver {

	public static void main(String[] args) throws IOException {
		String homePath = System.getProperty("hashi.home");
		if (StringUtils.isBlank(homePath)) {
			throw new IllegalStateException("hashi.home not set");
		}
		homePath += "/menneske";

		File homeDir = new File(homePath);
		File[] levelDirs = homeDir.listFiles(new FileFilter() {

			public boolean accept(File file) {
				return file.isDirectory();
			}
		});

		ArrayList<Hashi> hashiList = new ArrayList<Hashi>();

		for (File levelDir : levelDirs) {
			File[] sizeDirs = levelDir.listFiles(new FileFilter() {

				public boolean accept(File file) {
					return file.isDirectory();
				}
			});

			for (File sizeDir : sizeDirs) {
				File[] hashiFiles = sizeDir.listFiles();
				for (File hashiFile : hashiFiles) {
					String fileName = hashiFile.getName();
					String dirName = levelDir.getName() + "_" + sizeDir.getName() + "x" + sizeDir.getName();

					Hashi hashi = new Hashi(new FileInputStream(hashiFile), dirName + ": " + fileName);
					hashi.initialize();
					// hashi.getSolver().setUsePreventDeadGroups121(false);
					System.out.println(hashi.getName());
					for (int i = 0; i < 10; i++) {
						hashi.getSolver().solve();
					}
					hashiList.add(hashi);
				}
			}
		}

		TreeMap<String, Integer[]> stats = new TreeMap<String, Integer[]>();

		for (Hashi hashi : hashiList) {
			String name = hashi.getName();
			int groupCount = hashi.getSolver().getState().getGroups().size();

			System.out.println(name + ": " + groupCount);

			Pattern p = Pattern.compile("([a-z]+)_([0-9]+x[0-9]+): [0-9]+\\.txt");
			Matcher m = p.matcher(name);

			String level;
			String size;

			if (m.matches()) {
				level = m.group(1);
				size = m.group(2);
			} else {
				throw new IllegalStateException("Illegal hashi name: " + name);
			}

			updateStats(stats, level, groupCount == 1);
			updateStats(stats, size, groupCount == 1);
			updateStats(stats, "total", groupCount == 1);
		}

		for (Entry<String, Integer[]> entry : stats.entrySet()) {
			int total = entry.getValue()[0];
			int solved = entry.getValue()[1];
			System.out.println(entry.getKey() + ": " + solved + " of " + total + " -> " + (100 * solved / total) + "%");
		}
	}

	private static void updateStats(TreeMap<String, Integer[]> stats, String key, boolean solved) {
		Integer[] stat = stats.get(key);
		if (stat == null) {
			stat = new Integer[] { 0, 0 };
			stats.put(key, stat);
		}
		stat[0] = stat[0] + 1;
		stat[1] = stat[1] + (solved ? 1 : 0);
	}
}
