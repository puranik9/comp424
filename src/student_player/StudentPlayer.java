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
	
	private int aiPlayer;
	private int opponentPlayer;
	private int level;
	private int WEIGHTING_NUM_PCS = 2;
	private int WEIGHTING_KING_DIST = 10;
	private int WEIGHTING_PCS_TO_KING = 1;

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
       
    		// Set humanPlayer and opponentPlayer IDs
    		if(player_id == TablutBoardState.SWEDE) {
    			aiPlayer = TablutBoardState.SWEDE;
    			opponentPlayer = TablutBoardState.MUSCOVITE;
    		}
    		else {
    			aiPlayer = TablutBoardState.MUSCOVITE;
    			opponentPlayer = TablutBoardState.SWEDE;
    		}
    		
    		// add root node and get all children nodes
    		TreeNode rootNode = new TreeNode(null, boardState, null);
    		rootNode.addChildren();
    		List<TreeNode> children = rootNode.getChildren();
    		
    		// make win move if there is one
    		Move winMove = getWinMove(children);
    		if(winMove != null) {
    			return winMove;
    		}
    		
    		// if greedy move exists for king to get to corner
    		if(aiPlayer == TablutBoardState.SWEDE) {
    			Move greedyMove = findGreedyMove(rootNode);
    			if(greedyMove != null) {
    				return greedyMove;
    			}
    		}
    		
    		//if any obvious move exists for king to get to corner
    		Move obvMove = findObviousMove(children, rootNode);
    		if(obvMove != null) {
    			return obvMove;
    		}
    		
    		//run monte carlo sim if no obv or greedy move exists
    		int endTime = 1500;
    		long startTimeStamp = System.currentTimeMillis();
    		while((System.currentTimeMillis() - startTimeStamp) < endTime) {
    			for(TreeNode child: children) {
    				TreeNode bestNode = chooseBestNode(child);
    				TablutBoardState playResult = simRandomPlay(bestNode.getState());
    				if(playResult.getWinner() == aiPlayer) {
    					child.setWinningPoints((child.getWinningPoints() + 1));
    				}
    			}
    		}
    		
    		//high score from monte carlo sim is returned
    		return getHighScore(rootNode.getChildren()).recentMove;
    		
    }
    /**
     * 
     * @param children
     * @return
     */
    private Move getWinMove(List<TreeNode> children) {
    		for(TreeNode child : children) {
    			TablutBoardState boardState = child.getState();
    			if(boardState.getWinner() == aiPlayer) {
    				return child.getRecentMove();    			
			}
    		}
    		return null;
    }
    
    /**
     * 
     * @param children
     * @return
     */
    private TreeNode getHighScore(List<TreeNode> children) {
    		TreeNode highScore = children.get(0);
    		for(TreeNode child: children) {
    			if(child.getWinningPoints() > highScore.getWinningPoints()) {
    				highScore = child;
    			}
    		}
    		return highScore;
    }
    
    /**
     * 
     * @param root
     * @return
     */
    private TreeNode chooseBestNode(TreeNode root) {
    		TreeNode node = root;
		node.addChildren();
		int bestHeuristic = -1;
		TreeNode bestNode = null;
		for(TreeNode child: root.getChildren()) {
			int heuristic = determineHeuristic(child.getState());
			if(heuristic > bestHeuristic) {
				bestNode = child;
			}	
		}
		return bestNode;
    }
    
    /**
     * 
     * @param boardState
     * @return
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
    
    private int determineHeuristic(TablutBoardState boardState) {
    		int points = 1000;
    		if(boardState.gameOver()) {
    			if(boardState.getWinner() == aiPlayer) {
    				return 10000;
    			}
    			if(boardState.getWinner() == opponentPlayer) {
    				return 0;
    			}
    		}
    		
    		Coord kingPiece = boardState.getKingPosition();
    		HashSet<Coord> humanPlayerPieces = boardState.getPlayerPieceCoordinates();
    		HashSet<Coord> opponentPlayerPieces = boardState.getOpponentPieceCoordinates();
    		points += humanPlayerPieces.size();
    		points -= opponentPlayerPieces.size();
    		
    		if(aiPlayer == TablutBoardState.SWEDE) {
    			points -= WEIGHTING_KING_DIST * Coordinates.distanceToClosestCorner(kingPiece);
    		}
    		else {
    			points += WEIGHTING_KING_DIST * Coordinates.distanceToClosestCorner(kingPiece);
    		}
    		return points;
    }
    
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
    
    private boolean safeMove(TablutBoardState boardState) {
    		int originalTotalPcs = boardState.getNumberPlayerPieces(aiPlayer);
    		for(TablutMove move: boardState.getAllLegalMoves()) {
    				TablutBoardState childState = (TablutBoardState) boardState.clone();
    				childState.processMove(move);
    				int newTotalPcs = childState.getNumberPlayerPieces(aiPlayer);
    				if(originalTotalPcs - newTotalPcs != 0) {
    					return false;
    				}
    		}
    		return true;
    }
    
    private Move findObviousMove(List<TreeNode> children, TreeNode parent) {
    		int prevOpponentPcs = parent.getState().getNumberPlayerPieces(opponentPlayer);
    		for(TreeNode child: children) {
    			int newOpponentPcs = child.getState().getNumberPlayerPieces(opponentPlayer);
    			if(prevOpponentPcs - newOpponentPcs != 0) {
    				return child.getRecentMove();
    			}
    		}
    		return null;
    }
}