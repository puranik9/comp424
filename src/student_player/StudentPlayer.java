package student_player;

import java.util.List;
import java.util.Random;

import boardgame.Move;
import tablut.TablutBoardState;
import tablut.TablutMove;
import tablut.TablutPlayer;

/** A player file submitted by a student. */
public class StudentPlayer extends TablutPlayer {
	
	private int humanPlayer;
	private int opponentPlayer;
	private int level;
	

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
    			humanPlayer = TablutBoardState.SWEDE;
    			opponentPlayer = TablutBoardState.MUSCOVITE;
    		}
    		else {
    			humanPlayer = TablutBoardState.MUSCOVITE;
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
    		
    		int endTime = 1500;
    		long startTimeStamp = System.currentTimeMillis();
    		while((System.currentTimeMillis() - startTimeStamp) < endTime) {
    			for(TreeNode child: rootNode.getChildren()) {
    				TreeNode bestNode = chooseBestNode(child);
    				TablutBoardState playResult = simRandomPlay(bestNode.getState());
    				if(playResult.getWinner() == humanPlayer) {
    					child.setWinningPoints((child.getWinningPoints() + 1));
    				}
    			}
    		}
    		
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
    			if(boardState.getWinner() == humanPlayer) {
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
    		return node.getChildren().get(0);
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
    
    
}