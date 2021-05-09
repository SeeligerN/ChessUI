package test;

import ui.ChessActionListener;
import ui.ChessWindow;
import ui.Highlight;
import ui.MoveHighlight;

public class Game {

	public static void main(String[] args) {
		ChessWindow cw = new ChessWindow();
		cw.setPosition("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
		cw.printPosition();
		
		cw.addChessActionListener(new ChessActionListener() {
			
			@Override
			public void pickedPieceUp(char piece, String from) {
				System.out.println(piece + from);
				cw.addHighlight(new MoveHighlight(from));
			}
			
			@Override
			public void movePieceAttempt(char piece, String from, String to) {
				System.out.println(piece + from + to);
				cw.clearHighlights();
				
				cw.setPieceAt(to, piece);
				cw.clearPieceAt(from);
			}

			@Override
			public void chessMoveAborted() {
				cw.clearHighlights();
			}
		});
	}
	
}
