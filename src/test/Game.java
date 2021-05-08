package test;

import ui.ChessActionListener;
import ui.ChessWindow;
import ui.Highlight;

public class Game {

	public static void main(String[] args) {
		ChessWindow cw = new ChessWindow();
		cw.setPosition("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
		cw.printPosition();
		
		cw.addChessActionListener(new ChessActionListener() {
			
			@Override
			public void pickedPieceUp(char piece, String from) {
				System.out.println(piece + from);
				cw.addHighlight(new Highlight(from));
			}
			
			@Override
			public void movePieceAttempt(char piece, String from, String to) {
				System.out.println(piece + from + to);
				cw.clearHighlights();
			}

			@Override
			public void chessMoveAborted() {
				cw.clearHighlights();
			}
		});
	}
	
}
