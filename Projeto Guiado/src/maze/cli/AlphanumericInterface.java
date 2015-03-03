package maze.cli;

import java.util.Scanner;

import maze.logic.Game;
import maze.logic.MazeBuilder;

public class AlphanumericInterface {
	private Scanner s;
	
	public void start()
	{
		s = new Scanner(System.in);
		Game game = createGame();
		
		do{
			game.drawMap();
		} while(game.turn(s.next()));
		s.close();
	}

	private Game createGame()
	{
		System.out.print("For this game, you can choose the dimmensions of the game map. The minimum size is 8,\nsince smaller sizes would make the game impossible to finish.\n");
		System.out.print("Please indicate the map size (minimum " + MazeBuilder.MIN_REC_SIDE + ") : ");
		int map_side = s.nextInt();

		while(map_side < MazeBuilder.MIN_REC_SIDE)
		{
			System.out.print("\nGiven value is less than " + MazeBuilder.MIN_REC_SIDE + ".\nPlease insert new value : ");
			map_side = s.nextInt();
		}
		
		return new Game(map_side);
	}

}
