package maze.cli;

import java.util.Scanner;

import maze.logic.Dragon;
import maze.logic.Game;
import maze.logic.GameData;
import maze.logic.Hero;
import maze.logic.Maze;
import maze.logic.MazeBuilder;
import maze.logic.Sword;

public class AlphanumericInterface {
	private static final char dragon_char = 'D';				// S�mbolo representativo do drag�o
	private static final char dragon_sleeping_char = 'd';
	private static final char sword_char = 'E';					// S�mbolo da espada
	private static final char dragon_sword_char = 'F';			// S�mbolo a representar quando drag�o e espada est�o coincidentes
	private static final char dragon_sleeping_sword_char = 'f';
	private static final char hero_char = 'H';					// S�mbolo do her�i sem espada
	private static final char armed_hero_char = 'A';			// S�mbolo do her�i com espada
	private static final char wall_char = 'X';
	private static final char exit_char = 'S';
	
	private Scanner s;
	
	public void start()
	{
		s = new Scanner(System.in);
		Game game = createGame();
		
		do{
			drawGame(game.getGameData());
		} while(game.turn(s.next()));
		s.close();
	}

	private Game createGame()
	{
		System.out.print("For this game, you can choose the dimensions of the game map. The minimum size is 8,\nsince smaller sizes would make the game impossible to finish.\n");
		System.out.print("Please indicate the map size (minimum " + MazeBuilder.MIN_REC_SIDE + ") : ");
		int map_side = s.nextInt();

		while(map_side < MazeBuilder.MIN_REC_SIDE)
		{
			System.out.print("\nGiven value is less than " + MazeBuilder.MIN_REC_SIDE + ".\nPlease insert new value : ");
			map_side = s.nextInt();
		}
		
		System.out.print("\nPlease indicate the number of dragons (max 5) : ");
		int dragon_number = s.nextInt();
		
		while(dragon_number > 5 && dragon_number < 1)
		{
			System.out.print("\nGiven value is out of range.\nPlease insert new value : ");
			dragon_number = s.nextInt();
		}
		
		System.out.print("\nPlease indicate the dragon mode (0-still, 1-moving, 2-moving & sleeping) : ");
		int dragon_mode = s.nextInt();
		
		while(dragon_mode > 3 && dragon_mode < 0)
		{
			System.out.print("\nGiven value is out of range.\nPlease insert new value : ");
			dragon_mode = s.nextInt();
		}

		return new Game(map_side, dragon_number, dragon_mode);
	}

	public char[][] placeMaze(char[][] matrix, Maze maze)
	{
		// Walls
		matrix = maze.getMatrix().clone();
		for (int i = 0; i < matrix.length; i++)
		{
			matrix[i] = matrix[i].clone();
		}
		
		// Exit
		if (maze.getExit().isVisible())
			matrix[maze.getExit().getY()][maze.getExit().getX()] = exit_char;
		
		return matrix;
	}

	public char[][] placeEntities(char[][] matrix, Hero hero, Sword sword, Dragon[] dragons)
	{
		// Hero
		if (hero.isArmed())
			matrix[hero.getY()][hero.getX()] = armed_hero_char;
		else
			matrix[hero.getY()][hero.getX()] = hero_char;
		
		// Dragons
		for (int i = 0; i < dragons.length; i++)
		{
			if (dragons[i].isAlive())
			{
				if (dragons[i].isSleeping())
					matrix[dragons[i].getY()][dragons[i].getX()] = dragon_sleeping_char;
				else
					matrix[dragons[i].getY()][dragons[i].getX()] = dragon_char;
			}
		}

		// Sword
		if (sword.isDropped())
		{
			if (matrix[sword.getY()][sword.getX()] == dragon_char)
				matrix[sword.getY()][sword.getX()] = dragon_sword_char;
			else if (matrix[sword.getY()][sword.getX()] == dragon_sleeping_char)
				matrix[sword.getY()][sword.getX()] = dragon_sleeping_sword_char;
			else
				matrix[sword.getY()][sword.getX()] = sword_char;
		}
		return matrix;
	}
	
	public void drawGame(GameData gameData)
	{
		int side = gameData.getMap().getSide();
		char[][] matrix = new char[side][side];
		matrix = placeMaze(matrix, gameData.getMap());
		matrix = placeEntities(matrix, gameData.getHero(), gameData.getSword(), gameData.getDragons());
		drawMatrix(matrix);
	}
	
	public void drawMatrix(char[][] matrix)
	{
		for (int y = 0; y < matrix.length; y++)
		{
			for (int x = 0; x < matrix.length; x++)
			{
				System.out.print(matrix[y][x] + " ");
			}
			System.out.println();
		}
	}
}
