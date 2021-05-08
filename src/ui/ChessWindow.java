package ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class ChessWindow {

	static BufferedImage[] textures;

	private static int margin = 50;

	private JFrame frame;
	private DrawLabel dl;
	private MouseHandler mouse;
	private KeyboardHandler kb;
	private String fen;
	private char[][] position;
	private boolean showFlipped;
	private int lightColor;
	private int darkColor;

	private List<ChessActionListener> listeners;
	private List<Highlight> highlightedSquares;

	static {
		try {
			textures = new BufferedImage[128];
			BufferedImage spritesheet = ImageIO.read(ChessWindow.class.getResource("/res/pieces.png"));

			int pieceWidth = spritesheet.getWidth() / 6;
			int pieceHeight = spritesheet.getHeight() / 2;

			for (char piece : "prnbqkPRNBQK".toCharArray()) {
				int xOffset = -1;

				char lowerCase = (piece + "").toLowerCase().charAt(0);

				switch (lowerCase) {
				case 'p' -> xOffset = 5;
				case 'r' -> xOffset = 4;
				case 'n' -> xOffset = 3;
				case 'b' -> xOffset = 2;
				case 'q' -> xOffset = 1;
				case 'k' -> xOffset = 0;
				}

				if (xOffset == -1) // piece does not exist
					continue;

				int yOffset = (Character.isLowerCase(piece)) ? 1 : 0;

				textures[piece] = spritesheet.getSubimage(pieceWidth * xOffset, pieceHeight * yOffset, pieceWidth,
						pieceHeight);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ChessWindow() {
		listeners = new ArrayList<>();
		highlightedSquares = new ArrayList<>();
		showFlipped = false;
		fen = "8/8/8/8/8/8/8/8"; // empty board
		position = new char[8][8];
		lightColor = 14277576;
		darkColor = 8686171;

		frame = new JFrame("Chess");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1000, 1000);
		frame.setLocationRelativeTo(null);

		dl = new DrawLabel();
		frame.add(dl);

		mouse = new MouseHandler();
		dl.addMouseListener(mouse);
		dl.addMouseMotionListener(mouse);

		kb = new KeyboardHandler();
		frame.addKeyListener(kb);

		frame.setVisible(true);
	}

	public void addChessActionListener(ChessActionListener listener) {
		listeners.add(listener);
	}

	public void removeChessActionListener(ChessActionListener listener) {
		listeners.remove(listener);
	}

	public void addHighlight(Highlight h) {
		highlightedSquares.add(h);
	}

	public void removeHighlight(Highlight h) {
		highlightedSquares.remove(h);
	}

	public void clearHighlights() {
		highlightedSquares.clear();
	}

	public void flipBoard() {
		showFlipped = !showFlipped;
		frame.repaint();

		System.out.println("flip");
	}

	public void setPosition(String fen) {
		char[][] board = new char[8][8];

		fen = fen.split(" ")[0];
		if (fen.split("/").length != 8) return; // incorrect amount of ranks specified

		for (int rank = 7; rank >= 0; rank--) {
			String rankS = fen.split("/")[7 - rank];

			int file = 0;
			for (char c : rankS.toCharArray()) {
				if ("12345678".contains("" + c)) {
					// empty space
					file += Integer.parseInt("" + c);
				} else if ("prnbqkPRNBQK".contains("" + c)) {
					// piece
					board[file][rank] = c;
					file++;
				} else {
					return; // illegal char
				}
				if (file > 7) break;
			}
		}

		this.fen = fen;
		this.position = board;
		frame.repaint();
	}

	public void printPosition() {
		System.out.println("fen: " + fen);
		for (int rank = 7; rank >= 0; rank--) {
			System.out.println("+---+---+---+---+---+---+---+---+");
			for (int file = 0; file < 8; file++) {
				System.out.print("|");
				if (showFlipped)
					System.out.print(" " + position[7 - file][7 - rank] + " ");
				else
					System.out.print(" " + position[file][rank] + " ");
			}
			if (showFlipped)
				System.out.print(7 - rank + 1);
			else
				System.out.print(rank + 1);
			System.out.println();
		}
		if (showFlipped)
			System.out.println("+-H-+-G-+-F-+-E-+-D-+-C-+-B-+-A-+");
		else
			System.out.println("+-A-+-B-+-C-+-D-+-E-+-F-+-G-+-H-+");
	}

	private class DrawLabel extends JLabel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void paint(Graphics g2) {
			super.paint(g2);
			Graphics2D g = (Graphics2D) g2;

			g.setColor(Color.WHITE);
			g.fillRect(0, 0, this.getWidth(), this.getHeight());

			// which ever is smaller
			int boardWidth = (this.getWidth() - margin) < (this.getHeight() - margin) ? (this.getWidth() - margin)
					: (this.getHeight() - margin);
			boardWidth -= boardWidth % 8; // make boardWidth divisible by 8
			int tileWidth = boardWidth / 8;

			g.translate(this.getWidth() / 2 - boardWidth / 2, this.getHeight() / 2 - boardWidth / 2);

			int pickedUpX = -1;
			int pickedUpY = -1;
			if (mouse.isMovingPiece()) {
				pickedUpX = mouse.from.charAt(0) - 'A';
				pickedUpY = 7 - (mouse.from.charAt(1) - '1');

				if (showFlipped) {
					pickedUpX = 7 - pickedUpX;
					pickedUpY = 7 - pickedUpY;
				}
			}

			// draw chessboard
			Color dark = new Color(darkColor);
			Color light = new Color(lightColor);
			for (int x = 0; x < 8; x++) {
				for (int y = 0; y < 8; y++) {
					Color tileColor = null;
					if ((x + y) % 2 == 0)
						tileColor = light;
					else
						tileColor = dark;

					// draw bg
					g.setColor(tileColor);
					g.fillRect(x * tileWidth, y * tileWidth, tileWidth, tileWidth);
				}
			}

			// draw highlights
			for (Highlight h : highlightedSquares) {
				int highlightX = -1;
				int highlightY = -1;
				highlightX = h.getTile().charAt(0) - 'A';
				highlightY = 7 - (h.getTile().charAt(1) - '1');

				if (showFlipped) {
					highlightX = 7 - highlightX;
					highlightY = 7 - highlightY;
				}

				g.setColor(new Color(189, 238, 255, 100));
				System.out.println("highlighting");
				g.fillRect(highlightX * tileWidth, highlightY * tileWidth, tileWidth, tileWidth);
			}

			for (int x = 0; x < 8; x++) {
				for (int y = 0; y < 8; y++) {
					// draw pieces
					if (!(pickedUpX == x && pickedUpY == y)) { // if this piece isn't held by the mouse
						char piece = position[x][7 - y];
						if (showFlipped) piece = position[7 - x][y];
						g.drawImage(textures[piece], x * tileWidth, y * tileWidth, tileWidth, tileWidth, null);
					}
				}
			}

			// draw held piece
			if (mouse.isMovingPiece()) {
				int mouseXOnBoard = mouse.mouseX - (dl.getWidth() / 2 - boardWidth / 2);
				int mouseYOnBoard = mouse.mouseY - (dl.getHeight() / 2 - boardWidth / 2);

				g.drawImage(textures[mouse.piece], mouseXOnBoard - tileWidth / 2, mouseYOnBoard - tileWidth / 2,
						tileWidth, tileWidth, null);
			}
		}
	}

	private class KeyboardHandler implements KeyListener {

		boolean flipHandled = false;

		@Override
		public void keyTyped(KeyEvent e) {

		}

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_F && !mouse.isMovingPiece() && !flipHandled) {
				flipBoard();
				flipHandled = true;
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_F) flipHandled = false;
		}

	}

	private class MouseHandler implements MouseListener, MouseMotionListener {

		char piece = 0;
		String from = null;
		int mouseX = 0;
		int mouseY = 0;

		public boolean isMovingPiece() {
			if (from == null) return false;
			if (from.isEmpty()) return false;
			return true;
		}

		@Override
		public void mouseClicked(MouseEvent e) {

		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getButton() != 1) return;

			int boardWidth = (dl.getWidth() - margin) < (dl.getHeight() - margin) ? (dl.getWidth() - margin)
					: (dl.getHeight() - margin);
			boardWidth -= boardWidth % 8; // make boardWidth divisible by 8
			int tileWidth = boardWidth / 8;

			int xOnBoard = e.getX() - (dl.getWidth() / 2 - boardWidth / 2);
			int yOnBoard = e.getY() - (dl.getHeight() / 2 - boardWidth / 2);

			if (xOnBoard > boardWidth) return;
			if (yOnBoard > boardWidth) return;

			piece = position[xOnBoard / tileWidth][7 - yOnBoard / tileWidth];
			if (showFlipped) piece = position[7 - xOnBoard / tileWidth][yOnBoard / tileWidth];

			if (!"prnbqkPRNBQK".contains("" + piece)) return;

			if (showFlipped)
				from = "" + (char) ('H' - xOnBoard / tileWidth) + (1 + yOnBoard / tileWidth);
			else
				from = "" + (char) ('A' + xOnBoard / tileWidth) + (8 - yOnBoard / tileWidth);

			// trigger chess action listeners
			for (ChessActionListener listener : listeners)
				listener.pickedPieceUp(piece, from);

			frame.repaint();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.getButton() != 1) return;

			if (!isMovingPiece()) return;

			String move = from;
			from = null;

			frame.repaint();

			int boardWidth = (dl.getWidth() - margin) < (dl.getHeight() - margin) ? (dl.getWidth() - margin)
					: (dl.getHeight() - margin);
			boardWidth -= boardWidth % 8; // make boardWidth divisible by 8
			int tileWidth = boardWidth / 8;

			int xOnBoard = e.getX() - (dl.getWidth() / 2 - boardWidth / 2);
			int yOnBoard = e.getY() - (dl.getHeight() / 2 - boardWidth / 2);

			if (xOnBoard > boardWidth) return;
			if (yOnBoard > boardWidth) return;

			if (showFlipped)
				move += "" + (char) ('H' - xOnBoard / tileWidth) + (1 + yOnBoard / tileWidth);
			else
				move += "" + (char) ('A' + xOnBoard / tileWidth) + (8 - yOnBoard / tileWidth);

			if (move.substring(2).equals(move.substring(0, 2))) {
				for (ChessActionListener listener : listeners)
					listener.chessMoveAborted();
				
				return;
			}

			for (ChessActionListener listener : listeners)
				listener.movePieceAttempt(piece, move.substring(0, 2), move.substring(2, 4));
		}

		@Override
		public void mouseEntered(MouseEvent e) {

		}

		@Override
		public void mouseExited(MouseEvent e) {
			from = null;
			
			for (ChessActionListener listener : listeners)
				listener.chessMoveAborted();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			mouseX = e.getX();
			mouseY = e.getY();

			frame.repaint();

			if (e.getButton() != 1) return;

		}

		@Override
		public void mouseMoved(MouseEvent e) {
			mouseX = e.getX();
			mouseY = e.getY();
		}
	}
}
