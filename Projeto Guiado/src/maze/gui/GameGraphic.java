package maze.gui;


import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.*;

import maze.logic.*;

@SuppressWarnings("serial")
public class GameGraphic extends JPanel implements MouseListener, MouseMotionListener, KeyListener {

	public static final String gamesDir = "./games";

	private GameFrame frame;

	// Default window sizes. "Minimized" represents the window when there is no running game
	private static final int default_width = 880;
	private static final int default_width_minimized = 370;
	private static final int default_height = 940;
	private static final int default_height_minimized = 100;

	private int border = 10;	// Minimum border (distance to frame limits)

	/*
	 * These two variables represent the range of time for the dragons to move
	 * independantly, regardless of the user's choices
	 */
	public static final int dragon_timer_min = 600;		
	public static final int dragon_timer_max = 1800;
	/*
	 * These two variables define the range of delay to be caused by each
	 * DragonMoveThread when they are initialized. This prevents the dragons
	 * from starting their movement everytime the user presses the
	 * "Resume Game" button
	 */
	private static final int dragon_delay_min = 100;
	private static final int dragon_delay_max = 400;

	// ArrayList that stores the current running DragonMoveThread
	private ArrayList<DragonMoveThread> running_threads = new ArrayList<DragonMoveThread>();

	// Auxiliary variables for game management
	private Game.Direction hero_direction = Game.Direction.UP;
	private boolean paused = false;

	// Window elements
	private JLabel lblDarts;
	private JButton btnNewGame;
	private JButton btnLoadGame;
	private JButton btnSaveGame;
	private JButton btnDrawMaze;
	private JButton btnPauseGame;
	private JButton btnCloseGame;
	private JButton btnConfig;
	private static final String btnPauseText = "Pause Game";
	private static final String btnResumeText = "Resume Game";

	private Configuration config = new Configuration();		// Game settings
	Game game;												// Game itself

	public GameGraphic(GameFrame frame) throws IOException
	{		
		game = null;
		this.frame = frame;
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		int windowX = dim.width/2-default_width_minimized/2;
		int windowY = dim.height/2-default_height_minimized/2;
		frame.setBounds(windowX, windowY, default_width_minimized, default_height_minimized);

		Images.load();

		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addKeyListener(this);

		initializeWindowElements();
		loadSettings();

		add(btnNewGame);
		add(btnConfig);
		add(btnCloseGame);
		add(lblDarts);
		add(btnSaveGame);
		add(btnLoadGame);
		add(btnDrawMaze);
		add(btnPauseGame);
	}
	
	private void initializeWindowElements()
	{
		btnCloseGame = new JButton("Close Game");
		btnCloseGame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				int n = JOptionPane.showConfirmDialog(null, "Are you sure you want to close the game?", "Close Game", JOptionPane.YES_NO_OPTION);

				if(n == 0)
				{
					saveSettings();
					System.exit(0);
				}
			}
		});

		btnConfig = new JButton("Settings");
		btnConfig.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				String[] options = {"Settings", "Manage map"};

				int n = JOptionPane.showOptionDialog(null,
						"What would you like to do?",
						"Settings",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,
						options,
						options[0]);

				if(n == 0)
				{
					ConfigDialog sett = new ConfigDialog();
					sett.display(config);
					saveSettings();	
				}
				else if (n == 1)
				{
					ManageMapDialog mapDialog = new ManageMapDialog();
					mapDialog.display();
					String file = ManageMapDialog.mapName;
					if(file != null)
					{
						file = ManageMapDialog.mapName.toString();
						ManageMapDialog.mapName = null;
						loadGameFromMap(file + Game.mapFileExtension);
					}
				}

				requestFocus();
			}
		});

		lblDarts = new JLabel("Darts : 0");
		lblDarts.setToolTipText("Shoot darts at the dragons with W, A, S and D");

		btnNewGame = new JButton("New Game");
		btnNewGame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {


				int n = 0;

				if(game != null)
					n = JOptionPane.showConfirmDialog(null, "Are you sure you want to start a new game?", "New Game", JOptionPane.YES_NO_OPTION);

				/*
				 * The line above was made so that the program only asks the user's confirmation to create a new game
				 * when there is a game ongoing. If there are no current games, it creates a new one without asking.
				 */

				if(n == 0)
				{
					game = new Game(config.side, config.dragonNumber, config.dragonMode);
					startThreads();

					repaint();
					requestFocus();
				}
			}
		});

		btnLoadGame = new JButton("Load Saved Game");
		btnLoadGame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				pauseGame();

				File folder = new File(gamesDir);
				File[] listOfFiles = folder.listFiles();

				ArrayList<String> games = new ArrayList<String>();

				String extension = "";

				for (int i = 0; i < listOfFiles.length; i++) {

					String fileName = listOfFiles[i].getName();
					int index_ext = fileName.lastIndexOf('.');
					if(index_ext>0) extension = fileName.substring(index_ext);

					if (listOfFiles[i].isFile() && extension.equals(Game.gameFileExtension))
					{
						fileName = fileName.substring(0, index_ext);
						games.add(fileName);
					}
				}

				if(games.isEmpty())
				{
					JOptionPane.showMessageDialog(null, "There are no saved games.", "Warning", JOptionPane.WARNING_MESSAGE);
					return;
				}

				String files[] = new String[0];

				files = games.toArray(new String[games.size()]);

				String s = (String)JOptionPane.showInputDialog(null, "Please choose a game to load:", "Load Game", JOptionPane.PLAIN_MESSAGE, null, files, "files[0]");

				if ((s != null) && (s.length() > 0)) {

					String loadName = gamesDir + "/" + s + Game.gameFileExtension;
					Game newGame = Game.loadFromFile(loadName);
					if(newGame != null)
					{
						game = newGame;
						startThreads();
						pauseGame();
						repaint();
					}
					else
					{
						JOptionPane.showMessageDialog(null, "An error has occurred loading the game. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}

				requestFocus();
			}
		});

		btnSaveGame = new JButton("Save Game");
		btnSaveGame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				pauseGame();
				
				String file = new String();						

				while(file.isEmpty())
				{
					file = JOptionPane.showInputDialog(null, "Game name:", "Save game", JOptionPane.PLAIN_MESSAGE);

					if(file == null)
					{
						JOptionPane.showMessageDialog(null, "Game saving cancelled", "Warning", JOptionPane.WARNING_MESSAGE);
						return;
					}

					file = file + Game.gameFileExtension;
					File f = new File("./games/" + file);

					if(f.exists() && !f.isDirectory())
					{
						JOptionPane.showMessageDialog(null, "Game name already exists.", "Warning", JOptionPane.WARNING_MESSAGE);
						file = "";
					}
					else
					{
						try
						{
							ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
							oos.writeObject(game.getGameData());
							oos.close();
						}
						catch(Exception exc) {
							JOptionPane.showMessageDialog(null, "Error opening file. Please try a different name.", "Warning", JOptionPane.WARNING_MESSAGE);
							file = "";
						}
					}
				}

				JOptionPane.showMessageDialog(null, "Game saved!", "Action complete", JOptionPane.PLAIN_MESSAGE);
				requestFocus();
			}
		});

		btnSaveGame.setEnabled(false);

		btnDrawMaze = new JButton("Draw Maze");
		btnDrawMaze.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				Thread drawMazeThread = new Thread () {
					public void run() {
						DrawMapWindow win = new DrawMapWindow();
						win.start();
					}
				};
				drawMazeThread.start();
			}
		});

		btnPauseGame = new JButton("Pause Game");
		btnPauseGame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				togglePauseGame();
			}
		});
		btnPauseGame.setVisible(false);
	}
		
	private void togglePauseGame()
	{
		if(paused)
			resumeGame();
		else
			pauseGame();
	}
	
	private void pauseGame()
	{
		paused = true;
		btnPauseGame.setText(btnResumeText);
		stopThreads();
	}
		
	private void resumeGame()
	{
		paused = false;
		btnPauseGame.setText(btnPauseText);
		startThreads();
	}
		
	private void loadGameFromMap(String file)
	{
		char[][] matrix;
		try
		{
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File("./maps/" + file)));
			matrix = (char[][])ois.readObject();
			ois.close();
		}
		catch(Exception exc) {
			JOptionPane.showMessageDialog(null, "Error opening file", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Hero hero = new Hero(0, 0);
		ArrayList<Dragon> dragList = new ArrayList<Dragon>();
		Dragon[] dragons = new Dragon[0];
		Sword sword = new Sword(0, 0);
		Exit exit = new Exit(0, 0);
		Maze map;
		ArrayList<Dart> dartList = new ArrayList<Dart>();
		Dart[] darts;
		Shield shield = new Shield(0, 0);

		for(int lin = 1; lin < matrix.length-1; lin++)
		{
			for(int col = 1; col < matrix.length-1; col++)
			{				
				switch(matrix[lin][col])
				{
				case 'h':
					hero = new Hero(col, lin);
					break;
				case 'e':
					sword = new Sword(col, lin);
					break;
				case 'd':
					dragList.add(new Dragon(col, lin, config.dragonMode));
					break;
				case 's':
					shield = new Shield(col, lin);
					break;
				case 'a':
					dartList.add(new Dart(col, lin));
					break;
				}

				if(matrix[lin][col] != Maze.wallChar)
					matrix[lin][col] = ' ';
			}
		}

		int side = matrix.length;

		for(int i = 0; i < side; i++)
		{			
			if(matrix[i][0] == ' ')
			{
				exit = new Exit(0, i);
				matrix[i][0] = 'X';
				break;
			}

			if(matrix[i][side-1] == ' ')
			{
				exit = new Exit(side-1, i);
				matrix[i][side-1] = 'X';
				break;
			}

			if(matrix[0][i] == ' ')
			{
				exit = new Exit(i, 0);
				matrix[0][i] = 'X';
				break;
			}

			if(matrix[side-1][i] == ' ')
			{
				exit = new Exit(i, side-1);
				matrix[side-1][i] = 'X';
				break;
			}
		}

		map = new Maze(matrix, exit);
		dragons = dragList.toArray(new Dragon[dragList.size()]);
		darts = dartList.toArray(new Dart[dartList.size()]);

		GameData data = new GameData(map, hero, sword, dragons, darts, shield);
		game = new Game(data);
		startThreads();
		repaint();
		requestFocus();
	}
	
	public void saveSettings()
	{
		try
		{
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("./data/settings")));
			ConfigurationSerializable conf = new ConfigurationSerializable(config);
			oos.writeObject(conf);
			oos.close();
		}
		catch(Exception exc) {}
	}
	
	public void loadSettings()
	{
		try
		{
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File("./data/settings")));
			ConfigurationSerializable conf = (ConfigurationSerializable) ois.readObject();
			ois.close();

			this.config = conf.toConfig();
		}
		catch(Exception exc) {}
	}

	public void paintComponent(Graphics g) {
		
		super.paintComponent(g);
		g.setColor(Color.BLACK);
		if(game != null)
		{
			frame.setMinimumSize(new Dimension(468, 555));
			
			lblDarts.setVisible(true);
			lblDarts.setText("Darts : " + game.numDartsHero());
			if(!frame.isResizable())
			{
				frame.setResizable(true);
				Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
				int windowX = dim.width/2-default_width/2;
				int windowY = dim.height/2-default_height/2;
				frame.setBounds(windowX, windowY, default_width, default_height);

			}
			
			btnSaveGame.setEnabled(true);
			btnPauseGame.setVisible(true);
			showGame(game.getGameData(), g);
			
			if(paused) {
				int img_height = (int)((frame.getWidth()/Images.game_paused.getWidth())*Images.game_paused.getHeight());
				img_height = Images.game_paused.getHeight()*frame.getWidth()/Images.game_paused.getWidth();
				int y = (int)frame.getHeight()/2 - (int)img_height/2;
				g.drawImage(Images.game_paused, 0, (int)y,
						(int)frame.getWidth(), (int)(y + img_height), 
						0, 0, Images.game_paused.getWidth(), Images.game_paused.getHeight(), null);
				repaint();
			}
		}
		else
		{
			frame.setMinimumSize(new Dimension(370, 100));
			
			lblDarts.setVisible(false);
			if(frame.isResizable())
			{
				frame.setResizable(false);
				Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
				int windowX = dim.width/2-default_width_minimized/2;
				int windowY = dim.height/2-default_height_minimized/2;
				frame.setBounds(windowX, windowY, default_width_minimized, default_height_minimized);
			}
			btnSaveGame.setEnabled(false);
			btnPauseGame.setVisible(false);
		}
	}
	
	public boolean isPaused()
	{
		return paused;
	}

	private void resetGame()
	{
		game = null;
		stopThreads();
		paused = false;
		btnPauseGame.setText(btnPauseText);
		lblDarts.setText("Darts : 0");
		repaint();
	}

	public void actOnEvent(Game.event event)
	{
		switch(event)
		{
		case LOSE:
			resetGame();
			JOptionPane.showMessageDialog(null, "Ups... A dragon killed you...\nTry picking up a sword next time!", "Game over", JOptionPane.INFORMATION_MESSAGE);
			break;
		case LOSE_FIRE:
			resetGame();
			JOptionPane.showMessageDialog(null, "Ups... You were killed by dragon fire...\nTry protecting yourself with a shield next time!", "Game over", JOptionPane.INFORMATION_MESSAGE);
			break;
		case WIN:
			resetGame();
			JOptionPane.showMessageDialog(null, "CONGRATULATIONS! You escaped the maze!\nCome back and try again!", "Game won!", JOptionPane.PLAIN_MESSAGE);
			break;
		default:
			break;
		}

		repaint();
	}
	
	public boolean isGameRunning()
	{
		return game != null;
	}
	
	private void stopThreads()
	{
		while(!running_threads.isEmpty()){
			running_threads.get(0).end();
			running_threads.remove(0);
		}
	}
	
	private void startThreads()
	{
		int dragons = game.numDragoes();
		Random rand = new Random();
		int delay;

		for(int i = 0; i < dragons; i++)
		{
			delay = rand.nextInt(dragon_delay_max-dragon_delay_min) + dragon_delay_min;
			DragonMoveThread t = new DragonMoveThread(game, i, this, delay);
			running_threads.add(t);
			t.start();
			requestFocus();
			repaint();
		}
	}
	
	public void showGame(GameData gameData, Graphics g)
	{
		MapDrawer md = new MapDrawer(gameData.getMap());
		md.draw(gameData, hero_direction, g, border, border + btnPauseGame.getY() + btnPauseGame.getHeight(), this.getWidth() - 2 * border, this.getHeight() - (2 * border + btnPauseGame.getY() + btnPauseGame.getHeight()));
	}
	
	public void keyPressed(KeyEvent arg0)
	{		
		if(!paused)
		{
			Game.event ret = Game.event.NONE;

			if(game != null)
			{
				if(arg0.getKeyCode() == config.cmdUP)
				{
					ret = game.turnHero(Game.command.MOVE, Game.Direction.UP);
					hero_direction = Game.Direction.UP;
				}
				else if(arg0.getKeyCode() == config.cmdDOWN)
				{
					ret = game.turnHero(Game.command.MOVE, Game.Direction.DOWN);
					hero_direction = Game.Direction.DOWN;
				}
				else if(arg0.getKeyCode() == config.cmdLEFT)
				{
					ret = game.turnHero(Game.command.MOVE, Game.Direction.LEFT);
					hero_direction = Game.Direction.LEFT;
				}
				else if(arg0.getKeyCode() == config.cmdRIGHT)
				{
					ret = game.turnHero(Game.command.MOVE, Game.Direction.RIGHT);
					hero_direction = Game.Direction.RIGHT;
				}
				else if(arg0.getKeyCode() == config.dartUP)
					ret = game.turnHero(Game.command.FIRE, Game.Direction.UP);
				else if(arg0.getKeyCode() == config.dartDOWN)
					ret = game.turnHero(Game.command.FIRE, Game.Direction.DOWN);
				else if(arg0.getKeyCode() == config.dartRIGHT)
					ret = game.turnHero(Game.command.FIRE, Game.Direction.RIGHT);
				else if(arg0.getKeyCode() == config.dartLEFT)
					ret = game.turnHero(Game.command.FIRE, Game.Direction.LEFT);
			}

			actOnEvent(ret);
		}
	}
	
	public void mouseDragged(MouseEvent arg0) {}

	public void mouseMoved(MouseEvent arg0) {}
	
	public void mouseClicked(MouseEvent arg0) {}
	
	public void mouseEntered(MouseEvent arg0) {}
	
	public void mouseExited(MouseEvent arg0) {}
	
	public void mousePressed(MouseEvent arg0) {}
	
	public void mouseReleased(MouseEvent arg0) {}
	
	public void keyReleased(KeyEvent e) {}
	
	public void keyTyped(KeyEvent e) {}

}
