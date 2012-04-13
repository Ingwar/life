package su.eqx.iggdrasil.life;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class GameGeneration {
	private Set<Cell> mAliveCells = new HashSet<Cell>();

	private int mRowCount = 70;
	private int mColumnCount = 50;


	private long mGeneration = 0;

	private Random rand = new Random();

	long getGeneration() {
		return mGeneration;
	}

	Set<Cell> getAliveCells() {
		return mAliveCells;
	}

	int getRowCount() {
		return mRowCount;
	}

	int getColumnCount() {
		return mColumnCount;
	}

	void calculateNextGeneration() {
		Set<Cell> nextGeneration = new HashSet<Cell>();
		Set<Cell> interestingCells = new HashSet<Cell>();
		for (Cell cell : mAliveCells) {
			interestingCells.addAll(cell.calculateNeighbours());
		}
		interestingCells.addAll(mAliveCells);
		for (Cell currentCell : interestingCells ) {
			Set<Cell> neighbours = currentCell.calculateNeighbours();
			int aliveNegboursCount = 0;

			for (Cell neigbour : neighbours) {
				if (mAliveCells.contains(neigbour)) {
					aliveNegboursCount++;
				}
			}
			boolean alive = mAliveCells.contains(currentCell);
			if ((aliveNegboursCount == 3) || (aliveNegboursCount == 2 && alive)) {
				nextGeneration.add(currentCell);
			}
		}
		mAliveCells = nextGeneration;
		mGeneration++;
	}

	void addOrRemoveCell(int row, int column) {
		Cell cell = new Cell(row, column);
		if (!mAliveCells.add(cell)) {
			mAliveCells.remove(cell);
		}
	}

	class Cell {
		private int myRow;
		private int myColumn;

		Cell(int row, int column) {
			myRow = row;
			myColumn = column;
		}

		int getRow() {
			return myRow;
		}

		int getColumn() {
			return myColumn;
		}

		Set<Cell> calculateNeighbours() {
			Set<Cell> neighbours = new HashSet<Cell>();
			for (int i = -1; i < 2; i++) {
				for (int j = -1; j < 2; j++) {
					int row = (i + myRow) % GameGeneration.this.mRowCount;
					int column = (j + myColumn) % GameGeneration.this.mColumnCount;
					row = (row >= 0 ? row : row + GameGeneration.this.mRowCount);
					column = (column >= 0 ? column : column + GameGeneration.this.mColumnCount);
					neighbours.add(new Cell(row, column));
				}
			}
			neighbours.remove(this);
			return neighbours;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Cell cell = (Cell) o;

			if (myColumn != cell.myColumn) return false;
			if (myRow != cell.myRow) return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = myRow;
			result = 31 * result + myColumn;
			return result;
		}

		@Override
		public String toString() {
			return "Cell{" +
					"myRow=" + myRow +
					", myColumn=" + myColumn +
					'}';
		}
	}
}
