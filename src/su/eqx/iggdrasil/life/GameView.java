package su.eqx.iggdrasil.life;


import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class GameView extends View{

	private Set<Cell> mAliveCells = new HashSet<Cell>();

	private int mRowCount = 70;
	private int mColumnCount = 50;

	private Handler mUpdateHandler = new Handler();
	private Runnable mUpdateRunnable = new Runnable() {
		@Override
		public void run() {
			GameView.this.invalidate();
			GameView.this.mGenerationText.setText("Generation: " + mGeneration);
			GameView.this.calculateNextGeneration();
			GameView.this.mUpdateHandler.removeCallbacks(this);
			GameView.this.mUpdateHandler.postDelayed(this, mDELAY_TIME);
		}
	};

	enum GameState {MODELING, RUNNING};
	private GameState mGameState;

	private long mDELAY_TIME = 100;

	private TextView mGenerationText;

	private long mGeneration = 0;

	private Random rand = new Random();


	public GameView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public GameView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GameView(Context context) {
		super(context);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawColor(Color.WHITE);

		int cwidth = canvas.getWidth();
		int cheight = canvas.getHeight();
		int width = getWidth();
		int height = getHeight();
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.BLACK);
		if (mGameState == GameState.MODELING) {

			canvas.drawLines(generateGrid(width, height, mRowCount, mColumnCount), paint);
		}
		for (Cell c: mAliveCells) {
			Point p = c.getCenter();
			canvas.drawCircle(p.x, p.y, 3, paint);
		}


	}

//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		int a = 2 * 2;
//		if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
//			mUpdateHandler.removeCallbacks(mUpdateRunnable);
//			mUpdateHandler.postDelayed(mUpdateRunnable, 3000);
//			return true;
//		}
//		return super.onKeyDown(keyCode, event);
//	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			Cell touchedCell = getRowColumn(event.getX(), event.getY());
			boolean contained =!mAliveCells.add(touchedCell);
			if (contained) {
				mAliveCells.remove(touchedCell);
			}
			invalidate();
//			mUpdateHandler.removeCallbacks(mUpdateRunnable);
//			mUpdateHandler.postDelayed(mUpdateRunnable, 3000);
			return true;
		}
		return super.onTouchEvent(event);
	}

	void setState(GameState newState) {
		mUpdateHandler.removeCallbacks(mUpdateRunnable);
		mGameState = newState;
		invalidate();
		if (newState == GameState.RUNNING) {
			mUpdateHandler.postDelayed(mUpdateRunnable, mDELAY_TIME);
		}
	}

	void setGenerationViewer(TextView view) {
		mGenerationText = view;
	}

	private void calculateNextGeneration() {
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
		if (mAliveCells.equals(nextGeneration)) {
			setState(GameState.MODELING);
			return;
		}
		mAliveCells = nextGeneration;
		mGeneration++;
	}
//	private void calculateNextGeneration() {
//		Set<Cell> nextGeneration = new HashSet<Cell>();
//		Set<Cell> reviewedCells = new HashSet<Cell>();
//		for (Cell cell: mAliveCells) {
////			Set<Cell> aliveNeighbours = new HashSet<Cell>();
//			int aliveNeighboursCount = 0;
//			Set<Cell> deadNeighbours = new HashSet<Cell>();
//			for (Cell neighbour : cell.calculateNeighbours()) {
//
//				if (mAliveCells.contains(neighbour)) {
//					aliveNeighboursCount++;
//				}
//				else {
//					deadNeighbours.add(neighbour);
//				}
//
//				if ((aliveNeighboursCount == 2) || (aliveNeighboursCount == 3)) {
//					nextGeneration.add(cell);
//				}
//
//				for (Cell deadCell : deadNeighbours) {
//					if (reviewedCells.contains(deadCell)) {
//						continue;
//					}
//
//					int aliveCount = 0;
//
//					for (Cell newNeigh: deadCell.calculateNeighbours()) {
//						if (mAliveCells.contains(newNeigh)) {
//							aliveCount++;
//						}
//					}
//
//					if (aliveCount == 3) {
//						nextGeneration.add(deadCell);
//					}
//
//					reviewedCells.add(deadCell);
//				}
//			}
//
//		}
//		mAliveCells = nextGeneration;
//	}

	private float[] generateGrid(int canwas_width, int canvas_height, int row_count, int column_count) {
		int vertical_lines_count = column_count - 1;
		int horizontal_lines_count = row_count - 1;

		float[] grid = new float[4 * (horizontal_lines_count + vertical_lines_count)];

		float vertical_step = ((float) canvas_height) / row_count;
		float horizontal_step = ((float) canwas_width) / column_count;

		float current_y = 0;
		for (int i = 0; i < 4 * horizontal_lines_count; i += 4) {
			current_y += vertical_step;
			grid[i] = 0;
			grid[i + 1] = current_y;
			grid[i + 2] = canwas_width;
			grid[i + 3] = current_y;
		}

		float current_x = 0;
		for (int j = 4 * horizontal_lines_count; j < grid.length; j += 4) {
			current_x += horizontal_step;
			grid[j] = current_x;
			grid[j + 1] = 0;
			grid[j + 2] = current_x;
			grid[j + 3] = canvas_height;
		}

		return grid;
	}

	private Cell getRowColumn(float x, float y) {
		float h_step = ((float) getWidth()) / mColumnCount;
		float v_step = ((float) getHeight()) / mRowCount;

		int column = (int) (x / h_step);
		int row = (int) (y/ v_step);
		return new Cell(row, column);
	}

//	private Point getCenter(int row, int column) {
//		float h_step = ((float) getWidth()) / mColumnCount;
//		float v_step = ((float) getHeight()) / mRowCount;
//
//		int center_x = (int) (h_step * column + h_step / 2);
//		int center_y = (int) (v_step * row + v_step / 2);
//
//		return new Point(center_x, center_y);
//	}

	private class Cell {
		private int myRow;
		private int myColumn;

		Cell(int row, int column) {
			myRow = row;
			myColumn = column;
		}

		Point getCenter() {
			float h_step = ((float) GameView.this.getWidth()) / GameView.this.mColumnCount;
			float v_step = ((float) GameView.this.getHeight()) / GameView.this.mRowCount;

			int center_x = (int) (h_step * myColumn + h_step / 2);
			int center_y = (int) (v_step * myRow + v_step / 2);

			return new Point(center_x, center_y);
		}

		Set<Cell> calculateNeighbours() {
			Set<Cell> neighbours = new HashSet<Cell>();
			for (int i = -1; i < 2; i++) {
				for (int j = -1; j < 2; j++) {
					int row = (i + myRow) % GameView.this.mRowCount;
					int column = (j + myColumn) % GameView.this.mColumnCount;
					row = (row >= 0 ? row : row + GameView.this.mRowCount);
					column = (column >= 0 ? column : column + GameView.this.mColumnCount);
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
