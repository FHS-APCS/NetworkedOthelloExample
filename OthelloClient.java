import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JOptionPane;

import processing.core.*;

/**
 * A client for Othello, based on the TicTacToe server at
 * http://cs.lmu.edu/~ray/notes/javanetexamples/#tictactoe
 * 
 * The Othello game originally written by Jake Denfeld (2017 block 5 Intro Java)
 * based on my grid game framework.
 * 
 * Client uses a text-based protocol to send and recieve messages from the
 * server. These include:
 *
 * Client --> Server ----------------- MOVE <r> <c> QUIT
 *
 * Server --> Client ----------------- WELCOME <playernum> VALID_MOVE
 * OPPONENT_MOVED <r> <c> MESSAGE <text>
 *
 * The Othello client has an InputHandler which receives server messages and
 * does the appropriate action.
 */
public class OthelloClient extends PApplet {
	int c;
	Othello game;
	Display display;
	int green;
	InputHandler serverConnection;
	PrintWriter out;
	int playerNum;

	public void setup() {
		size(660, 630); // set the size of the screen.

		// Create a game object
		game = new Othello(8, 8);
		green = color(0, 105, 0);

		// Create the display
		// parameters: (10,10) is upper left of display
		// (300, 300) is the width and height
		display = new Display(this, 10, 60, 640, 550);

		display.setColor(0, green);
		display.setColor(2, 255);
		display.setColor(1, 0);

		// You can use images instead if you'd like.
		// d.setImage(1, "c:/data/ball.jpg");
		// d.setImage(2, "c:/data/cone.jpg");

		this.textSize(64);

		display.initializeWithGame(game);
		c = 0;

		// Create an input handler to send/receive messages from the server
		serverConnection = new InputHandler();

		// Get the output stream to the server so we can send messages from here
		out = serverConnection.getServerWriter();

		// Start the thread that listens for server messages and handles them
		serverConnection.start();
	}

	@Override
	public void draw() {
		background(0, 0, 0);
		fill(255);
		text("Player: " + playerNum, 10, 55);
		display.drawGrid(game.getGrid()); // display the game
	}

	public void mouseClicked() {
		if (playerNum < 1) {
			System.out.println("You haven't been assigned a player # by the server yet");
			return;
		}

		Location loc = display.gridLocationAt(mouseX, mouseY);
		if (game.isValidMove(playerNum, loc.getRow(), loc.getCol())) {
			// Send message to the server for the move you want
			out.println("MOVE " + loc.getRow() + " " + loc.getCol());

			// Make the move locally
			// Note: may be better to wait for confirmation from the server
			// before making move locally
			game.move(playerNum, loc.getRow(), loc.getCol());
		} else {
			System.out.println("Move not valid");
		}
	}

	public class InputHandler extends Thread {
		private int PORT = 8901;
		private Socket socket;
		private BufferedReader in;
		private PrintWriter out;

		public InputHandler() {
			String serverAddress = JOptionPane.showInputDialog("Type the ip of the server");

			try {
				// Setup networking
				socket = new Socket(serverAddress, PORT);
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void run() {
			String response;
			try {
				System.out.println("Waiting for welcome from server");
				response = in.readLine();
				if (response.startsWith("WELCOME")) {
					// Server sends message "WELCOME <playernum>"
					// Save the playernum the server assigned us
					playerNum = Integer.parseInt(response.substring(8).trim());
				}

				while (true) {
					response = in.readLine();
					System.out.println(playerNum + " - [SERVER]: " + response);
					if (response.startsWith("VALID_MOVE")) {

					} else if (response.startsWith("OPPONENT_MOVED")) {
						Location loc = parseMoveCommand(response);

						System.out.println("loc is: " + loc);

						if (loc != null && game.isValidMove(game.opponentTo(playerNum), loc.getRow(), loc.getCol())) {
							System.out.println("moving opponent");
							game.move(game.opponentTo(playerNum), loc.getRow(), loc.getCol());
						}
					} else if (response.startsWith("MESSAGE")) {
						System.out.println(response.substring(8));
					}
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		public PrintWriter getServerWriter() {
			return out;
		}
	}

	public static Location parseMoveCommand(String command) {
		String msg = command.substring(15).trim(); // command starts with
													// "OPPONENT_MOVED"
		String[] coords = msg.split(" ");
		if (coords.length != 2)
			return null;
		try {
			int r = Integer.parseInt(coords[0]);
			int c = Integer.parseInt(coords[1]);
			return new Location(r, c);
		} catch (Exception e) {
			return null;
		}
	}

	// main method to launch this Processing sketch from computer
	public static void main(String[] args) {
		PApplet.main(new String[] { "OthelloClient" });
	}
}