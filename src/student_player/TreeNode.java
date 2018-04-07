package student_player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import boardgame.Board;
import boardgame.Move;
import tablut.TablutBoard;
import tablut.TablutBoardState;
import tablut.TablutMove;

/**
 *
 * @author puranik9
 *
 * This class is used to represent a Tree Node
 */

public class TreeNode {
	TablutBoardState boardState;
    List<TreeNode> children;
    Move recentMove;
    int winPoints = 0;

    /**
     * 
     * @param parent
     * @param boardState
     * @param move
     */
    public TreeNode(TreeNode parent, TablutBoardState boardState, Move move){
        this.boardState = boardState;
        this.recentMove = move;
    }

    /**
     * 
     */
    public void addChildren() {
        children = new ArrayList<TreeNode>();
        List<TablutMove> choices = boardState.getAllLegalMoves();
        for(TablutMove move : choices) {
            TablutBoardState childState = (TablutBoardState) boardState.clone();
            childState.processMove(move);
            TreeNode child = new TreeNode(this, childState, move);
            children.add(child);
        }
    }
    
    /**
     * 
     * @return
     */
    public List<TreeNode> getChildren() {
    		return children;
    }
    
    /**
     * 
     * @return
     */
	public TablutBoardState getState() {
		return boardState;
	}
	
	/**
	 * 
	 * @return
	 */
	public TreeNode getRandomChild() {
		Random random = new Random();
		int rand = random.nextInt(children.size());
		return children.get(rand);
	}
	
	/**
	 * 
	 * @return
	 */
	public int getWinningPoints() {
		return winPoints;
	}
	
	/**
	 * 
	 * @param points
	 */
	public void setWinningPoints(int points) {
		this.winPoints = points;
	}
	
	/**
	 * 
	 * @return
	 */
	public Move getRecentMove() {
		return recentMove;
	}
	
}
