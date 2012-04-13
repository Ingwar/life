package su.eqx.iggdrasil.life;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import su.eqx.iggdrasil.life.GameGeneration.Cell;

public class GameView extends SurfaceView implements SurfaceHolder.Callback{

	 class GameThread extends Thread{

		private GameGeneration mGameGeneration;

		private GameState mGameState;

		private Touchedstate mTouchedState;

		private final SurfaceHolder mSurfaceHolder;

		private Paint mPaint;

		private Handler mUpdateHandler = new Handler();

		private Runnable mUpdateRunnable = new Runnable() {
			@Override
			public void run() {
//				GameView.this.mGenerationText.setText("Generation: " + GameThread.this.mGameGeneration.getGeneration());
				GameThread.this.updateScreen();
				GameThread.this.mGameGeneration.calculateNextGeneration();
				GameThread.this.mUpdateHandler.removeCallbacks(this);
				GameThread.this.mUpdateHandler.postDelayed(this, GameThread.this.mDELAY_TIME);
			}
		};

		private long mDELAY_TIME = 100;

		GameThread(SurfaceHolder holder) {
			mSurfaceHolder = holder;
			mGameGeneration = new GameGeneration();
			mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mPaint.setColor(Color.BLACK);
		}

		@Override
		public void run() {
			mUpdateRunnable.run();
		}

		void setState(GameState newState) {
			synchronized (mSurfaceHolder) {
				mUpdateHandler.removeCallbacks(mUpdateRunnable);
				mGameState = newState;
				updateScreen();
				if (newState == GameState.RUNNING) {
					mUpdateHandler.postDelayed(mUpdateRunnable, mDELAY_TIME);
				}
			}
		}

		private void updateScreen() {
			Canvas canvas = mSurfaceHolder.lockCanvas();
			try {
				synchronized (mSurfaceHolder) {
					doDraw(canvas);
				}
			} finally {
				if (canvas != null) {
					mSurfaceHolder.unlockCanvasAndPost(canvas);
				}
			}
//			mSurfaceHolder.unlockCanvasAndPost(canvas);
		}

		private void doDraw(Canvas canvas) {
			canvas.drawColor(Color.WHITE);

			int width = canvas.getWidth();
			int height = canvas.getHeight();
			if (mGameState == GameState.MODELING) {

				canvas.drawLines(generateGrid(width, height, mGameGeneration.getRowCount(), mGameGeneration.getColumnCount()), mPaint);
			}
			for (Cell c: mGameGeneration.getAliveCells()) {
				Point p = getCellCenter(c);
				canvas.drawCircle(p.x, p.y, 3, mPaint);
			}
		}

		private boolean doTouchEvent(MotionEvent event) {
			synchronized (mSurfaceHolder) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					Point point = getCorrespondingCell(event.getX(), event.getY());
					mGameGeneration.addOrRemoveCell(point.x, point.y);
					updateScreen();
					return true;
				} else {
					return false;
				}
			}
		}

		private Point getCorrespondingCell(float x, float y) {
			float h_step = ((float) getWidth()) / mGameGeneration.getColumnCount();
			float v_step = ((float) getHeight()) / mGameGeneration.getRowCount();

			int column = (int) (x / h_step);
			int row = (int) (y/ v_step);
			return new Point(row, column);
		}

		private Point getCellCenter(Cell cell) {
			float h_step = ((float) getWidth()) / mGameGeneration.getColumnCount();
			float v_step = ((float) getHeight()) /mGameGeneration.getRowCount();

			int center_x = (int) (h_step * cell.getColumn() + h_step / 2);
			int center_y = (int) (v_step * cell.getRow() + v_step / 2);

			return new Point(center_x, center_y);
		}

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
	}

	private GameThread mThread;

	enum Touchedstate {TOUCHED, MOVING, FREE}

	enum GameState {MODELING, RUNNING}

	private TextView mGenerationText;

	public GameView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView();
	}

	public GameView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	public GameView(Context context) {
		super(context);
		initView();
	}

	private void initView() {
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		mThread = new GameThread(holder);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mThread.doTouchEvent(event)) {
			return true;
		}
		return super.onTouchEvent(event);
	}

	void setGenerationViewer(TextView view) {
		mGenerationText = view;
	}

	GameThread getThread(){
		return mThread;
	}
	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		mThread.setState(GameState.MODELING);
		mThread.start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
		mThread.stop();
	}



}
