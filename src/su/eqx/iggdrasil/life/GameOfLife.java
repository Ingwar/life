package su.eqx.iggdrasil.life;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;


public class GameOfLife extends Activity {

	private GameView mGameView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.life_layout);
		mGameView = (GameView) findViewById(R.id.life_view);
		mGameView.setState(GameView.GameState.MODELING);

		final ToggleButton button = (ToggleButton) findViewById(R.id.button);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (button.isChecked()) {
					mGameView.setState(GameView.GameState.RUNNING);
				} else {
					mGameView.setState(GameView.GameState.MODELING);
				}
			}
		});

		mGameView.setGenerationViewer((TextView) findViewById(R.id.generation));
	}
}