package org.polarion.svnwebclient.web.support;

import java.util.ArrayList;
import java.util.List;

public class DifferenceArea {

	protected int leftIndex;
	protected int leftSize;
	protected int rightIndex;
	protected int rightSize;
	protected List data = new ArrayList();

	public DifferenceArea(int leftIndex, int leftSize, int rightIndex,
			int rightSize) {
		this.leftIndex = leftIndex;
		this.leftSize = leftSize;
		this.rightIndex = rightIndex;
		this.rightSize = rightSize;
	}

	public int getLeftIndex() {
		return this.leftIndex;
	}

	public int getLeftSize() {
		return this.leftSize;
	}

	public int getRightIndex() {
		return this.rightIndex;
	}

	public int getRightSize() {
		return this.rightSize;
	}

	public void addElement(String element) {
		this.data.add(element);
	}

	protected String getLine(int index) {
		String line = (String) this.data.get(index);
		return line = line.substring(1);
	}

	protected String getOperation(int index) {
		String line = (String) this.data.get(index);
		String operation = line.substring(0, 1);
		return operation;
	}

	/**
	 * Creates a list of lines which are marked as added, deleted, modified or
	 * not changed for passed side
	 * 
	 */
	public List getElements(boolean isLeftSide) {
		List ret = new ArrayList();
		int number = isLeftSide ? this.leftIndex : this.rightIndex;

		for (int i = 0; i < this.data.size(); i++) {
			String line = this.getLine(i);
			String operation = this.getOperation(i);

			if (" ".equals(operation)) {
				ret.add(new DifferenceLine(number++,
						DifferenceLine.NOT_CHANGED, line));
			} else if ("-".equals(operation)) {
				/*
				 * Processing blocks of modified elements
				 * 
				 * Examples:
				 * 
				 * 1. - a + a1
				 * 
				 * a and a1 are modified
				 * 
				 * 2. - a - b - c - d + a1 + b1
				 * 
				 * a, b and a1, b1 are modified
				 * 
				 * 3. - a - b + a1 + b1 + c1 + d1
				 * 
				 * a, b and a1, b1 are modified
				 */

				int minusesCount = this.getWithoutDistinctionOperationCount(
						"-", i);
				int plusesCount = this.getWithoutDistinctionOperationCount("+",
						i + minusesCount);

				if (plusesCount > 0) {
					int block = Math.min(minusesCount, plusesCount);

					// mark all lines inside block as modified
					for (int j = 0; j < block; j++) {
						int lineIndex = isLeftSide ? (i + j)
								: (i + j + minusesCount);
						line = this.getLine(lineIndex);
						ret.add(new DifferenceLine(number++,
								DifferenceLine.MODIFIED, line));
					}

					// mark other lines either as added or deleted, depending
					// on operation
					if (minusesCount < plusesCount) {
						int size = plusesCount - minusesCount;
						for (int j = 0; j < size; j++) {
							line = this.getLine(i + j + 2 * block);
							DifferenceLine dl = this.createLine(number, line,
									isLeftSide, true);
							ret.add(dl);
							if (dl.getNumber() != DifferenceLine.EMPTY_NUMBER) {
								number++;
							}
						}
					} else if (minusesCount > plusesCount) {
						int size = minusesCount - plusesCount;
						for (int j = 0; j < size; j++) {
							line = this.getLine(i + j + block);
							DifferenceLine dl = this.createLine(number, line,
									isLeftSide, false);
							ret.add(dl);
							if (dl.getNumber() != DifferenceLine.EMPTY_NUMBER) {
								number++;
							}
						}
					}

					// increment 'for' counter
					i += (minusesCount + plusesCount - 1);
				} else {
					DifferenceLine dl = this.createLine(number, line,
							isLeftSide, false);
					ret.add(dl);
					if (dl.getNumber() != DifferenceLine.EMPTY_NUMBER) {
						number++;
					}
				}
			} else if ("+".equals(operation)) {
				DifferenceLine dl = this.createLine(number, line, isLeftSide,
						true);
				ret.add(dl);
				if (dl.getNumber() != DifferenceLine.EMPTY_NUMBER) {
					number++;
				}
			}
		}

		return ret;
	}

	/**
	 * Calculate number of operation without distinction starting from
	 * currentIndex
	 * 
	 */
	protected int getWithoutDistinctionOperationCount(String operation,
			int currentIndex) {
		int count = 0;

		if (currentIndex < this.data.size()) {
			for (int i = currentIndex; i < this.data.size(); i++) {
				String op = this.getOperation(i);

				if (operation.equals(op)) {
					count++;
				} else {
					break;
				}
			}
		}
		return count;
	}

	/**
	 * We need differently handle create and delete operations for left and
	 * right sides
	 * 
	 * For left side: 'delete' is taken into account 'add' is not handled
	 * 
	 * For right side: 'delete' is not handled 'add' is taken into account
	 * 
	 * Note: if number is changed then caller should detect it and increment it
	 */
	protected DifferenceLine createLine(int number, String line,
			boolean isLeftSide, boolean isCreate) {
		DifferenceLine res = null;
		if (isLeftSide) {
			if (isCreate) {
				res = new DifferenceLine(DifferenceLine.EMPTY_NUMBER,
						DifferenceLine.NOT_CHANGED, "");
			} else {
				res = new DifferenceLine(number++, DifferenceLine.DELETED, line);
			}
		} else {
			if (isCreate) {
				res = new DifferenceLine(number++, DifferenceLine.ADDED, line);
			} else {
				res = new DifferenceLine(DifferenceLine.EMPTY_NUMBER,
						DifferenceLine.NOT_CHANGED, "");
			}
		}
		return res;
	}

	public List getLeftElements() {
		return this.getElements(true);
	}

	public List getRightElements() {
		return this.getElements(false);
	}

	/*
	 * public List getLeftElements() { List ret = new ArrayList(); int number =
	 * this.leftIndex; String previousOperation = " "; for (Iterator i =
	 * this.data.iterator(); i.hasNext(); ) { String line = (String) i.next();
	 * String operation = line.substring(0, 1); line = line.substring(1); if
	 * (" ".equals(operation)) { ret.add(new DifferenceLine(number++,
	 * DifferenceLine.NOT_CHANGED, line)); previousOperation = " "; } else if
	 * ("-".equals(operation)) { ret.add(new DifferenceLine(number++,
	 * DifferenceLine.DELETED, line)); previousOperation = "-"; } else if
	 * ("+".equals(operation)) { if ("-".equals(previousOperation)) {
	 * DifferenceLine l = (DifferenceLine) ret.get(ret.size() - 1);
	 * l.setType(DifferenceLine.MODIFIED); } else { ret.add(new
	 * DifferenceLine(DifferenceLine.EMPTY_NUMBER, DifferenceLine.NOT_CHANGED,
	 * "")); } previousOperation = "+"; } } return ret; }
	 * 
	 * 
	 * public List getRightElements() { List ret = new ArrayList(); int number =
	 * this.rightIndex; String previousOperation = " "; for (Iterator i =
	 * this.data.iterator(); i.hasNext(); ) { String line = (String) i.next();
	 * String operation = line.substring(0, 1); line = line.substring(1); if
	 * (" ".equals(operation)) { ret.add(new DifferenceLine(number++,
	 * DifferenceLine.NOT_CHANGED, line)); previousOperation = " "; } else if
	 * ("-".equals(operation)) { ret.add(new
	 * DifferenceLine(DifferenceLine.EMPTY_NUMBER, DifferenceLine.NOT_CHANGED,
	 * "")); previousOperation = "-"; } else if ("+".equals(operation)) { if
	 * ("-".equals(previousOperation)) { DifferenceLine l = (DifferenceLine)
	 * ret.get(ret.size() - 1); l.setLine(line);
	 * l.setType(DifferenceLine.MODIFIED); l.setNumber(number++); } else {
	 * ret.add(new DifferenceLine(number++, DifferenceLine.ADDED, line)); }
	 * previousOperation = "+"; } } return ret; }
	 */
}
