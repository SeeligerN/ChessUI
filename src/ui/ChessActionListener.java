package ui;

public interface ChessActionListener {

	/**
	 * This method is here to make it possible to mark possible move squares
	 */
	public void pickedPieceUp(char piece, String from);
	
	public void movePieceAttempt(char piece, String from, String to);
	
	public void chessMoveAborted();
}
