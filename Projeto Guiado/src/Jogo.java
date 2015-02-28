import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class Jogo {

	////////////////////////////////
	///////   Atributes   //////////
	////////////////////////////////
	

	private static final int ELEM_DIST_FACTOR = 2;			// Fator usado para determinar a dist. m�nima entre elementos

	private static final char dragao_char = 'D';			// S�mbolo representativo do drag�o
	private static final char espada_char = 'E';			// S�mbolo da espada
	private static final char dragao_espada_char = 'F';		// S�mbolo a representar quando drag�o e espada est�o coincidentes
	private static final char heroi_char = 'H';				// S�mbolo do her�i sem espada
	private static final char heroi_armado_char = 'A';		// S�mbolo do her�i com espada

	private static int heroi_x, heroi_y;			// Coordenadas do her�i
	private static int dragao_x, dragao_y;			// Coordenadas do drag�o
	private static int espada_x, espada_y;			// Coordenadas da espada
	private static boolean heroi_armado;			// True se o heroi j� est� armado
	private static boolean dragao;					// True se o drag�o est� vivo

	private static Scanner s;						// Used to read from the console throughout the program

	private static Labirinto map;					// Represents the game map

	
	

	////////////////////////////////
	///////   Functions   //////////
	////////////////////////////////
	
	
	public static void main(String[] args) {

		heroi_armado = false;
		dragao = true;

		s = new Scanner(System.in);


		System.out.print("For this game, you can choose the dimmensions of the game map. The minimum size is 8,\nsince smaller sizes would make the game impossible to finish.\n");
		System.out.print("Please indicate the map size (minimum " + Labirinto.MIN_REC_SIDE + ") : ");
		int map_side = s.nextInt();

		while(map_side < Labirinto.MIN_REC_SIDE)
		{
			System.out.print("\nGiven value is greater than " + Labirinto.MIN_REC_SIDE + ".\nPlease insert new value : ");
			map_side = s.nextInt();
		}

		int minElemDist = (int) (map_side/ELEM_DIST_FACTOR);
		minElemDist = minElemDist*minElemDist;

		map = new Labirinto(map_side);
		generateMapElements(minElemDist);

		do{
			drawMap();
		}while(turn());
		 
		s.close();
	}

	private static void drawMap()
	{
		boolean saida = !dragao;
		
		int[] coords = new int[6];
		Arrays.fill(coords, -1);
		char[] chars = new char[3];
		Arrays.fill(chars, ' ');
		
		
		coords[0] = heroi_x;
		coords[1] = heroi_y;
		if(heroi_armado)
			chars[0] = heroi_armado_char;
		else
			chars[0] = heroi_char;
		
		int i = 1;
		

		if(espada_x == dragao_x && espada_y == dragao_y)
		{
			coords[2*i] = espada_x;
			coords[2*i+1] = espada_y;
			
			if(!heroi_armado)
				chars[i] = dragao_espada_char;
			else
				chars[i] = dragao_char;
			i++;
		}
		else
		{
			if(!heroi_armado)
			{
				coords[2*i] = espada_x;
				coords[2*i + 1] = espada_y;
				chars[i] = espada_char;
				i++;
			}
			
			if(dragao)
			{
				coords[2*i] = dragao_x;
				coords[2*i + 1] = dragao_y;
				chars[i] = dragao_char;
				i++;
			}
		}
		
		map.drawMatrix(coords, chars, saida);
		
		
		
		
		
		
		

		/*if(espada_x == dragao_x && espada_y == dragao_y)
		{
			coords = new int[4];
			chars = new char[2];
			
			coords[0] = heroi_x;
			coords[1] = heroi_y;
			if(heroi_armado)
				chars[0] = heroi_armado_char;
			else
				chars[0] = heroi_char;
			
			coords[2] = espada_x;
			coords[3] = espada_y;
			chars[1] = dragao_espada_char;
		}
		else
		{
			coords = new int[6];
			chars = new char[3];
			
			coords[0] = heroi_x;
			coords[1] = heroi_y;
			if(heroi_armado)
				chars[0] = heroi_armado_char;
			else
				chars[0] = heroi_char;
			
			coords[2] = espada_x;
			coords[3] = espada_y;
			chars[1] = dragao_espada_char;
		}
		
		
		
		/*for(int i = 0; i < MAP_SIDE; i++)
		{
			for(int j = 0; j < MAP_SIDE; j++)
			{
				if(j==heroi_x && i==heroi_y)
				{
					if(heroi_armado)
						System.out.print(heroi_armado_char);
					else
						System.out.print(heroi_char);						
				}
				else if (j == saida_x && i == saida_y && !dragao)
					System.out.print(saida_char);
				else if(j == espada_x && i == espada_y && !heroi_armado)
				{
					if(j == dragao_x && i == dragao_y && dragao)
						System.out.print(dragao_espada_char);
					else
						System.out.print(espada_char);
				}
				else if (j == dragao_x && i == dragao_y && dragao)
					System.out.print(dragao_char);
				else				
					System.out.print(matrix[i][j]);

				System.out.print(' ');
			}
			System.out.print('\n');
		}*/
	}

	private static boolean turn()
	{
		String key = s.next();

		if(key.toUpperCase().equals("A"))
			moverHeroi(heroi_x-1, heroi_y);
		else if(key.toUpperCase().equals("W"))
			moverHeroi(heroi_x, heroi_y - 1);
		else if(key.toUpperCase().equals("S"))
			moverHeroi(heroi_x, heroi_y + 1);
		else if(key.toUpperCase().equals("D"))
			moverHeroi(heroi_x+1, heroi_y);
		
		if(map.isSaida(heroi_x, heroi_y)) 
		{
			System.out.print("\n\n Congratulations! You escaped the maze!\n\n");
			return false;
		}

		if(heroi_x == espada_x && heroi_y == espada_y)
			heroi_armado = true;

		if(combateDragao())
		{
			if(dragao)
				turnDragao();
		}
		else
		{
			System.out.print("\n\n Bad luck! The dragon killed you... Take revenge next time!\n\n");
			return false;
		}

		if(combateDragao())
		{
			return true;
		}

		System.out.print("\n\n Bad luck! The dragon killed you... Take revenge next time!\n\n");
		return false;
	}

	private static void turnDragao()
	{
		Random x = new Random();
		boolean posicaoValida = false;
		do
		{
			switch(x.nextInt(5))
			{
			case 0:
				posicaoValida = true;
				break;
			case 1: // cima
				posicaoValida = moverDragao(dragao_x, dragao_y - 1);
				break;
			case 2: // baixo
				posicaoValida = moverDragao(dragao_x, dragao_y + 1);
				break;
			case 3: // esquerda
				posicaoValida = moverDragao(dragao_x - 1, dragao_y);
				break;
			case 4: // direita
				posicaoValida = moverDragao(dragao_x + 1, dragao_y);
				break;
			}
		} while (!posicaoValida);
	}

	private static boolean moverDragao(int x, int y) {
		if (!map.isParede(x, y))
		{
			dragao_x = x;
			dragao_y = y;
			return true;
		}
		return false;
	}

	private static boolean moverHeroi(int x, int y) {
		if(!map.isParede(x, y) || (map.isSaida(x,  y) && !dragao))
		{
			heroi_x=x;
			heroi_y=y;
			return true;
		}
		return false;
	}

	private static boolean combateDragao()
	{
		if(Labirinto.areAdjacent(heroi_x, heroi_y, dragao_x, dragao_y) && dragao)
		{
			if(heroi_armado)
				dragao = false;
			else
				return false;
		}
		return true;
	}

	private static void generateMapElements(int min_dist)
	{
		generatePosHeroi();
		generatePosEspada(min_dist);
		generatePosDragao(min_dist);

	}

	private static void generatePosHeroi() {
		Random rand = new Random();

		int randX, randY;

		do
		{
			randX = rand.nextInt(map.matrixSide-2) + 1;
			randY = rand.nextInt(map.matrixSide-2) + 1;			
		} while(map.isParede(randX, randY) || randX >= map.matrixSide || randY >= map.matrixSide);

		heroi_x = randX;
		heroi_y = randY;
	}

	private static void generatePosEspada(int min_dist) {
		Random rand = new Random();

		int randX, randY;

		do
		{
			randX = rand.nextInt(map.matrixSide-2) + 1;
			randY= rand.nextInt(map.matrixSide-2) + 1;			
		} while(map.isParede(randX, randY) || Labirinto.coordDistSquare(randX, randY, heroi_x, heroi_y) < min_dist);

		espada_x = randX;
		espada_y = randY;
	}

	private static void generatePosDragao(int min_dist) {
		Random rand = new Random();

		int randX, randY;

		do
		{
			randX = rand.nextInt(map.matrixSide-2) + 1;
			randY= rand.nextInt(map.matrixSide-2) + 1;			
		} while(map.isParede(randX, randY) || Labirinto.coordDistSquare(randX, randY, heroi_x, heroi_y) < min_dist);

		dragao_x = randX;
		dragao_y = randY;
	}

}