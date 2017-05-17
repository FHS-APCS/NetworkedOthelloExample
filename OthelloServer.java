
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A server for a network multi-player Othello game, based on the TicTacToe
 * server at http://cs.lmu.edu/~ray/notes/javanetexamples/#tictactoe
 * 
 * The Othello game originally written by Jake Denfeld (2017 block 5 Intro Java)
 * based on my grid game framework.
 * 
 * Instead of passing *data* between the client and server, this example uses a
 * protocol, which is simple plain text. Valid strings in the protocol include:
 * 
 * Client --> Server ----------------- MOVE <r> <c> QUIT
 *
 * Server --> Client ----------------- WELCOME <playernum> VALID_MOVE
 * OPPONENT_MOVED <r> <c> MESSAGE <text>
 * 
 */
public class OthelloServer {
	private static int PORT = 8901;

	// Player is the input handler for each client
	private static Player p1, p2;

	// Game is the server copy of the game in progress. This is the official
	// game used
	// to check if a move is valid and ensure the correct player is moving.
	// (note: each client also maintains their own copy of the game for display
	// and local
	// checking of the rules).
	private static Othello game;

	/**
	 * Runs the application. Pairs up clients that connect.
	 */
	public static void main(String[] args) throws Exception {
		ServerSocket listener = new ServerSocket(PORT);
		System.out.println("Othello Server is Running");

		try {
			game = new Othello(8, 8);
			p1 = new Player(listener.accept(), 1);
			p2 = new Player(listener.accept(), 2);

			p1.start();
			p2.start();
		} finally {
			listener.close();
		}
	}

	/**
	 * The handler class for communicating with each client.
	 * 
	 * For communication with the client the player has a socket with its input
	 * and output streams. Since only text is being communicated we use a reader
	 * and a writer.
	 */
	private static class Player extends Thread {
		private int clientPlayerNum; // the player's number in the game
		private Socket socket;
		protected BufferedReader inputFromClient;
		protected PrintWriter outputToClient;

		/**
		 * Constructs a handler thread for a given socket and mark initializes
		 * the stream fields, displays the first two welcoming messages.
		 */
		public Player(Socket socket, int num) {
			this.socket = socket;
			this.clientPlayerNum = num;

			try {
				inputFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				outputToClient = new PrintWriter(socket.getOutputStream(), true);

				// Tell the client what player # they are.
				outputToClient.println("WELCOME " + clientPlayerNum);
				outputToClient.println("MESSAGE Waiting for opponent to connect");
			} catch (IOException e) {
				System.out.println("Player died: " + e);
			}
		}

		public void run() {
			try {
				// The thread is only started after everyone connects.
				outputToClient.println("MESSAGE All players connected");

				// Tell the first player that it is her turn.
				if (game.whoseTurn() == clientPlayerNum) {
					outputToClient.println("MESSAGE Your move");
				}

				// Repeatedly get commands from the client and process them.
				while (true) {
					String command = inputFromClient.readLine();
					System.out.println("[CLIENT " + clientPlayerNum + "]: " + command);

					if (command.startsWith("MOVE")) {
						Location loc = parseMoveCommand(command);

						// if correct player is trying to move, and move exists
						// and is valid
						if (game.whoseTurn() == clientPlayerNum && loc != null
								&& game.isValidMove(clientPlayerNum, loc.getRow(), loc.getCol())) {

							// tell client their move is valid
							outputToClient.println("VALID_MOVE");

							// make the move on the server game
							game.move(clientPlayerNum, loc.getRow(), loc.getCol());

							// tell the other client what their opponent did
							// so the game can update
							sendOpponantMessage("OPPONENT_MOVED " + loc.getRow() + " " + loc.getCol(), this);

							// TODO: send winner message?
						} else {
							// If invalid, let client know
							outputToClient.println("MESSAGE Invalid move!");
						}
					} else if (command.startsWith("QUIT")) {
						return;
					}
				}
			} catch (IOException e) {
				System.out.println("Player died: " + e);
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}

		public int getPlayerNum() {
			return this.clientPlayerNum;
		}
	}

	public static Location parseMoveCommand(String command) {
		String msg = command.substring(5).trim(); // command starts with "MOVE"

		// Now split the row and column into separate strings
		String[] coords = msg.split(" ");
		if (coords.length != 2)
			return null;
		try {
			int r = Integer.parseInt(coords[0]); // parse the row
			int c = Integer.parseInt(coords[1]); // parse the col
			return new Location(r, c); // return the location where they want to
										// move
		} catch (Exception e) {
			return null;
		}
	}

	/***
	 * There is definitely a more compact way to do this, but I wanted to make
	 * the code very easy to read.
	 * 
	 * @param message
	 *            the message to send to the opponent client
	 * @param player
	 *            the player who just moved
	 */
	public static void sendOpponantMessage(String message, Player player) {
		if (player.getPlayerNum() == 1) {
			p2.outputToClient.println(message);
		} else {
			p1.outputToClient.println(message);
		}
	}
}
