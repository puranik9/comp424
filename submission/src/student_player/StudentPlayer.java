package student_player;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import boardgame.Move;
import coordinates.Coord;
import coordinates.Coordinates;
import tablut.TablutBoardState;
import tablut.TablutMove;
import tablut.TablutPlayer;

/** A player file submitted by a student. */
public class StudentPlayer extends TablutPlayer {
	
	//Constants
	private final int WEIGHTING_NUM_PCS = 2;
	private final int WEIGHTING_KING_DIST = 10;
	private final int WEIGHTING_PCS_TO_KING = 1;
	private final int MONTE_CARLO_END_TIME = 1000;
	
	//Player variables
	private int studentPlayer;
	private int opponentPlayer;	

    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer() {
        super("2605654143");
    }

    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
    public Move chooseMove(TablutBoardState boardState) {
       
    		// set the studentPlayer and opponentPlayer IDs
    		if(player_id == TablutBoardState.SWEDE) {
    			studentPlayer = TablutBoardState.SWEDE;
    			opponentPlayer = TablutBoardState.MUSCOVITE;
    		}
    		else {
    			studentPlayer = TablutBoardState.MUSCOVITE;
    			opponentPlayer = TablutBoardState.SWEDE;
    		}
    		
    		// add root node and get all children nodes
    		TreeNode rootNode = new TreeNode(null, boardState, null);
    		rootNode.addChildren();
    		List<TreeNode> children = rootNode.getChildren();
    		
    		// make win move if there is one
    		Move winMove = findWinMove(children);
    		if(winMove != null) {
    			return winMove;
    		}
    		
    		// if greedy move exists for king to get to corner
    		if(studentPlayer == TablutBoardState.SWEDE) {
    			Move greedyMove = findGreedyMove(rootNode);
    			if(greedyMove != null) {
    				return greedyMove;
    			}
    		}
    		
    		// run monte carlo sim if no greedy move exists
    		long startTimeStamp = System.currentTimeMillis();
    		while((System.currentTimeMillis() - startTimeStamp) < MONTE_CARLO_END_TIME) {
    			for(TreeNode child: children) {
    				TreeNode bestNode = chooseBestNode(child);
    				TablutBoardState playResult = simRandomPlay(bestNode.getState());
    				if(playResult.getWinner() == studentPlayer) {
    					child.setWinningPoints((child.getWinningPoints() + 1));
    				}
    			}
    		}
    		
    		// high score from monte carlo sim is returned
    		return findHighScore(rootNode.getChildren()).recentMove;
    		
    }
    /**
     * Find move to win if it exists
     * 
     * @param children
     * @return Move winMove (null if there is no such move)
     */
    private Move findWinMove(List<TreeNode> children) {
    		for(TreeNode child : children) {
    			TablutBoardState boardState = child.getState();
    			if(boardState.getWinner() == studentPlayer) {
    				return child.getRecentMove();    			
			}
    		}
    		return null;
    }
    
    /**
     * Find high score node amongst children in random play
     * 
     * @param children
     * @return TreeNode highScore node
     */
    private TreeNode findHighScore(List<TreeNode> children) {
    		TreeNode highScore = children.get(0);
    		for(TreeNode child: children) {
    			if(child.getWinningPoints() > highScore.getWinningPoints()) {
    				highScore = child;
    			}
    		}
    		return highScore;
    }
    
    /**
     * Simulate a random play for Monte Carlo
     * 
     * @param boardState
     * @return TablutBoardState final state of game
     */
    private TablutBoardState simRandomPlay(TablutBoardState boardState) {
    		TablutBoardState tmpState = (TablutBoardState) boardState.clone();
    		if(tmpState.gameOver()) {
    			return boardState;
    		}
    		while(!tmpState.gameOver()) {
    			Random random = new Random();
    			List<TablutMove> legalMoves = tmpState.getAllLegalMoves();
    			tmpState.processMove(legalMoves.get(random.nextInt(legalMoves.size())));
    		}
    		return tmpState;
    }
    
    /**
     * Chooses best node based on determined heuristic
     * 
     * @param root
     * @return TreeNode bestNode
     */
    private TreeNode chooseBestNode(TreeNode root) {
    		TreeNode node = root;
		node.addChildren();
		int bestHeuristic = -1;
		TreeNode bestNode = null;
		for(TreeNode child: root.getChildren()) {
			int heuristic = determineHeuristic(child);
			if(heuristic > bestHeuristic) {
				bestNode = child;
			}	
		}
		return bestNode;
    }
    
    /**
     * Determine heuristic found on state of board
     * 
     * @param childNode
     * @return int total points heuristic
     */
    private int determineHeuristic(TreeNode childNode) {
    		TablutBoardState boardState = childNode.getState();
    		// basic heuristic
    		if(boardState.gameOver()) {
    			if(boardState.getWinner() == studentPlayer) {
    				return 10000;
    			}
    			if(boardState.getWinner() == opponentPlayer) {
    				return 0;
    			}
    		}
    		
    		int points = 1000;
    		Coord kingPiece = boardState.getKingPosition();
    		
    		// get points for pieces remaining, lose points for opponents pieces remaining
    		HashSet<Coord> humanPlayerPieces = boardState.getPlayerPieceCoordinates();
    		HashSet<Coord> opponentPlayerPieces = boardState.getOpponentPieceCoordinates();
    		points += WEIGHTING_NUM_PCS * humanPlayerPieces.size();
    		points -= WEIGHTING_NUM_PCS * opponentPlayerPieces.size();
    		
    		// get points for being close to king
    		for(Coord coord: boardState.getPlayerPieceCoordinates()) {
    			points -= WEIGHTING_PCS_TO_KING * coord.distance(kingPiece);
    		}
    		
    		// lose points for opponent being close to king
    		for(Coord coord: boardState.getPlayerPieceCoordinates()) {
    			points -= WEIGHTING_PCS_TO_KING * coord.distance(kingPiece);
    		}
    		
    		// get/lose points for king being close to corner 
    		if(studentPlayer == TablutBoardState.SWEDE) {
    			points -= WEIGHTING_KING_DIST * Coordinates.distanceToClosestCorner(kingPiece);
    		}
    		else {
    			points += WEIGHTING_KING_DIST * Coordinates.distanceToClosestCorner(kingPiece);
    		}
    		return points;
    }
    
    /**
     * Finds greedy way to move king closer to corner, 
     * or moving closer to king to protect it
     * 
     * @param parent
     * @return Move bestMove (null if there is no such move)
     */
    private Move findGreedyMove(TreeNode parent) {
    		Move bestMove = null;
    		Coord kingPiece = parent.getState().getKingPosition();
    		int minDist = Coordinates.distanceToClosestCorner(kingPiece);
    		for(TablutMove move: parent.getState().getLegalMovesForPosition(kingPiece)) {
    			int moveDist = Coordinates.distanceToClosestCorner(move.getEndPosition());
    			if(moveDist < minDist) {
    				TablutBoardState childState = (TablutBoardState) parent.getState().clone();
    				childState.processMove(move);
    				if(safeMove(childState)) {
    					minDist = moveDist;
    					bestMove = move;
    				}
    			}
    		}
    		return bestMove;
    }
    
    /**
     * Returns if state is safe after greedy move is made
     * 
     * @param boardState
     * @return boolean
     */
    private boolean safeMove(TablutBoardState boardState) {
    		int originalTotalPcs = boardState.getNumberPlayerPieces(studentPlayer);
    		for(TablutMove move: boardState.getAllLegalMoves()) {
    				TablutBoardState childState = (TablutBoardState) boardState.clone();
    				childState.processMove(move);
    				int newTotalPcs = childState.getNumberPlayerPieces(studentPlayer);
    				if(originalTotalPcs - newTotalPcs != 0) {
    					return false;
    				}
    		}
    		return true;
    }
   
}