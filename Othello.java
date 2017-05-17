import javax.swing.JOptionPane;

public class Othello {
	private boolean gameOver; // flag to record if the game is over
	private int playerTurn = 1; // whose turn it is
	private int winner; // who the winner is (0 if no winner)
	private int cols, rows; // # of rows and cols in game
	private int[][] grid; // the grid that stores the pieces
	private int move = -1, move2 = -1, playOne = 0, playTwo = 0;

	// The constructor initializes the game
	public Othello(int r, int c) {
		// Create the board
		this.cols = c;
		this.rows = r;
		grid = new int[r][c];

		grid[3][3] = 2;
		grid[3][4] = 1;
		grid[4][3] = 1;
		grid[4][4] = 2;
		// Set that the game is not over
		gameOver = false;
	}

	/*
	 * Return true if r, c is a valid move for the game.
	 */
	public boolean isValidMove(int playerNum, int r, int c) {
		if (playerNum != playerTurn) {
			//System.out.println("Invalid move: " + playerNum + " : " + r + " " + c);
			return false;
		}

		if (isInGrid(r, c) == false) { // if outside grid, not valid
			//System.out.println("Invalid move: " + playerNum + " : " + r + " " + c);
			return false;
		}
		if (grid[r][c] == 1 || grid[r][c] == 2) { // if the position is already
													// taken, not valid
			//System.out.println("Invalid move: " + playerNum + " : " + r + " " + c);
			return false;
		}
		if (enclose(r, c) == false) { // if it's not enclosed, not valid
			//System.out.println("Invalid move: " + playerNum + " : " + r + " " + c);
			return false;
		}
		return true;
	}

	/*
	 * Return true if the location at row, col is in the bounds of the grid.
	 * Return false otherwise.
	 */
	public boolean isInGrid(int row, int col) {
		/* you create this method */
		if (col >= this.cols || col < 0 || row >= this.rows || row < 0) {
			return false;
		}
		return true;
	}

	public boolean isInGrid(int col) {
		/* you create this method */
		if (col >= this.cols || col < 0) {
			return false;
		}
		return true;
	}

	/*
	 * Return true if the location l is in the bounds of the grid. Note: this
	 * method calls the other isInGrid to do the work.
	 */
	public boolean isInGrid(Location l) {
		return isInGrid(l.getRow(), l.getCol());
	}

	// makes the move
	// returns false if no move was made, true if the move was successful.
	public boolean move(int playerNum, int r, int c) {
		System.out.println("Trying to move player " + playerNum + " to " + r + " " + c);
		if (playerNum != playerTurn)
			return false;
		if (isValidMove(playerNum, r, c) == false)
			return false; // if not valid, exit

		int distLeft = getDist(r, c, 0, -1);
		int distUp = getDist(r, c, -1, 0);
		int distDown = getDist(r, c, 1, 0);
		int distRight = getDist(r, c, 0, 1);
		int distUpLeft = getDist(r, c, -1, -1);
		int distDownLeft = getDist(r, c, 1, -1);
		int distDownRight = getDist(r, c, 1, 1);
		int distUpRight = getDist(r, c, -1, 1);

		if (distDown != -1) {
			for (int i = 0; i < distDown; i++) {
				int lr = r + i;
				grid[lr][c] = playerTurn;
			}
		}

		if (distRight != -1) {
			for (int i = 0; i < distRight; i++) {
				int lc = c + i;
				grid[r][lc] = playerTurn;
			}
		}

		if (distDownRight != -1) {
			for (int i = 0; i < distDownRight; i++) {
				int lr = r + i;
				int lc = c + i;
				grid[lr][lc] = playerTurn;
			}
		}

		if (distUp != -1) {
			for (int i = 0; i < distUp; i++) {
				int lr = r - i;
				grid[lr][c] = playerTurn;
			}
		}

		if (distUpLeft != -1) {
			for (int i = 0; i < distUpLeft; i++) {
				int lr = r - i;
				int lc = c - i;
				grid[lr][lc] = playerTurn;
			}
		}

		if (distLeft != -1) {
			for (int j = 0; j < distLeft; j++) {
				int lc = c - j;
				grid[r][lc] = playerTurn;
			}

		}

		if (distDownLeft != -1) {
			for (int i = 0; i < distDownLeft; i++) {
				int lr = r + i;
				int lc = c - i;
				grid[lr][lc] = playerTurn;
			}
		}

		if (distUpRight != -1) {
			for (int i = 0; i < distUpRight; i++) {
				int lr = r - i;
				int lc = c + i;
				grid[lr][lc] = playerTurn;
			}
		}

		move2 = 0;
		for (int row = 0; row < grid.length; row++) {
			for (int col = 0; col < grid[0].length; col++) {
				if (isValidMove(playerTurn, row, col)) {
					move++;
				}
			}
		}

		// Switch player turn
		if (playerTurn == 1) {
			playerTurn = 2;
		} else {
			playerTurn = 1;
		}

		move = 0;
		for (int row = 0; row < grid.length; row++) {
			for (int col = 0; col < grid[0].length; col++) {
				if (isValidMove(playerTurn, row, col)) {
					move++;
				}
			}
		}

		if (move == 0 && move2 == 0) {
			JOptionPane.showMessageDialog(null, "No valid moves left, game over.");
			gameOver = true;
			checkWinner();
			System.out.println(playOne + " " + playTwo);
			JOptionPane.showMessageDialog(null, "Player " + winner + " is the winner");
		} else if (move == 0) {
			JOptionPane.showMessageDialog(null, "No valid moves player " + playerTurn + ", next player's turn.");
			if (playerTurn == 1) {
				playerTurn = 2;
			} else {
				playerTurn = 1;
			}
			return false;
		}
		return false;
	}

	/*
	 * Return true if the game is over. False otherwise.
	 */
	// private boolean checkForWinner() {
	// }

	public boolean isGameOver() {
		return gameOver;
	}

	public int[][] getGrid() {
		return grid;
	}

	public void toggleLight(int[][] grid, int r, int c) {
		if (isInGrid(r, c)) {
			if (grid[r][c] == 1) {
				grid[r][c] = 0;
			} else {
				grid[r][c] = 1;
			}
		}
	}

	public int getDist(int sr, int sc, int dr, int dc) {
		int lr = sr;
		int lc = sc;
		int count = 0;
		do {
			lr += dr;
			lc += dc;
			count++;
		} while (isInGrid(lr, lc) && grid[lr][lc] == getOtherPlayer(playerTurn));

		if (!isInGrid(lr, lc) || grid[lr][lc] != playerTurn || count == 1) {
			return -1;
		}
		return count;
	}

	private int getOtherPlayer(int playerTurn2) {
		if (playerTurn == 1)
			return 2;
		return 1;
	}

	public boolean enclose(int r, int c) {
		if (!(getDist(r, c, 1, 0) == -1)) {
			return true;
		}

		if (!(getDist(r, c, 1, 1) == -1)) {
			return true;
		}

		if (!(getDist(r, c, 0, 1) == -1)) {
			return true;
		}

		if (!(getDist(r, c, -1, 0) == -1)) {
			return true;
		}

		if (!(getDist(r, c, -1, -1) == -1)) {
			return true;
		}

		if (!(getDist(r, c, 0, -1) == -1)) {
			return true;
		}

		if (!(getDist(r, c, 1, -1) == -1)) {
			return true;
		}

		if (!(getDist(r, c, -1, 1) == -1)) {
			return true;
		}
		return false;
	}

	public void checkWinner() {

		if (gameOver) {
			for (int row = 0; row < grid.length; row++) {
				for (int col = 0; col < grid[0].length; col++) {
					if (grid[row][col] == 1)
						playOne++;
				}
			}
			for (int row = 0; row < grid.length; row++) {
				for (int col = 0; col < grid[0].length; col++) {
					if (grid[row][col] == 2)
						playTwo++;
				}
			}
			if (playOne > playTwo) {
				winner = 1;
			} else {
				winner = 2;
			}
		}
	}

	public int whoseTurn() {
		return playerTurn;
	}

	public int opponentTo(int playerNum) {
		if (playerNum == 1)
			return 2;
		return 1;
	}
}