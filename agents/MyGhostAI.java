package agents;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import mmaracic.gameaiframework.AgentAI;
import mmaracic.gameaiframework.PacmanVisibleWorld;
import mmaracic.gameaiframework.WorldEntity;

/**
 * @author marin
 *
 */
public class MyGhostAI extends AgentAI {

	private Date now = new Date();
	private Random r = new Random(now.getTime());

	int[] lastMove = new int[] { 0, 0 };
	int pacmanVisibleTime = 0;

	private int findClosest(ArrayList<int[]> moves, int[] pos) {
		int index = 0;
		int[] move = moves.get(0);
		float dist = Math.abs(pos[0] - move[0]) + Math.abs(pos[1] - move[1]);
		for (int i = 1; i < moves.size(); i++) {
			move = moves.get(i);
			float currDist = Math.abs(pos[0] - move[0]) + Math.abs(pos[1] - move[1]);
			if (currDist < dist) {
				dist = currDist;
				index = i;
			}
		}
		return index;
	}

	@Override
	public int decideMove(ArrayList<int[]> moves, PacmanVisibleWorld mySurroundings,
			WorldEntity.WorldEntityInfo myInfo) {

		int[] move = null;

		int TABLE_SIZE = mySurroundings.getDimensionX();
		int TREE_DEPTH = TABLE_SIZE / 2;

		double[][] visited = new double[TABLE_SIZE][TABLE_SIZE];

		for (int i = -TREE_DEPTH; i <= TREE_DEPTH; i++) {
			for (int j = -TREE_DEPTH; j <= TREE_DEPTH; j++) {
				if (i == 0 && j == 0)
					continue;

				ArrayList<WorldEntity.WorldEntityInfo> neighPosInfos = mySurroundings.getWorldInfoAt(i, j);
				HashMap<Integer, Object> metaHash = mySurroundings.getWorldMetadataAt(i, j);

				// look around for info
				if (neighPosInfos != null) {
					for (WorldEntity.WorldEntityInfo info : neighPosInfos) {
						if (info.getIdentifier().compareToIgnoreCase("Pacman") == 0) {
							int index = findClosest(moves, new int[] { i, j });
							metaHash.clear();
							metaHash.put(myInfo.getID(), moves.get(index));
							return index;
						}
						else if (info.getIdentifier().compareToIgnoreCase("Wall") == 0) {
							visited[j + TREE_DEPTH][i + TREE_DEPTH] += 100;
						} else if (info.getIdentifier().compareToIgnoreCase("Point") == 0) {
							visited[j + TREE_DEPTH][i + TREE_DEPTH] += 100000;
						} else if (info.getIdentifier().compareToIgnoreCase("Powerup") == 0) {
							visited[j + TREE_DEPTH][i + TREE_DEPTH] += -1000;
						} else if (info.getIdentifier().compareToIgnoreCase("Ghost") == 0) {
							visited[j + TREE_DEPTH][i + TREE_DEPTH] -= 100;
						} else {
							printStatus("I dont know what " + info.getIdentifier() + " is!");
						}
					}
				}

				if (metaHash != null) {
					for (Integer id : metaHash.keySet()) {
						if (id != myInfo.getID()) {
							move = (int[]) metaHash.remove(id);
							// printStatus(myInfo.getID()+": Found pacman trail
							// left by ghost: "+id+"!");
						}
					}
				}

			}
		}

		// Go where metadata pointed
		if (move != null) {
			for (int i = 0; i < moves.size(); i++) {
				int[] m = moves.get(i);
				if (m[0] == move[0] && m[1] == move[1]) {
					return i;
				}
			}
		}

		double d = Math.random();
		if (d < 0.4) {
			int choice = r.nextInt(moves.size());
			return choice;
		}

		double best = -1000000;
		int bestID = 0;

		// find best move
		for (int i = moves.size() - 1; i >= 0; i--) {
			int[] m = moves.get(i);
			double moveValue = evaluate(TREE_DEPTH + m[1], TREE_DEPTH + m[0], TREE_DEPTH - 1, visited);
			if (moveValue != 0) {
			}
			if (moveValue > best) {
				best = moveValue;
				bestID = i;
			} else if (moveValue == best) {
				d = Math.random();
				if (d > 0.5) {
					best = moveValue;
					bestID = i;
				}
			}
		}

		return bestID;

	}

	private double evaluate(int posX, int posY, int depth, double[][] visited) {

		if (depth == 0) {
			return visited[posX][posY];
		}

		double value = visited[posX][posY];
		double temp = visited[posX][posY];
		visited[posX][posY] = 0;
		value += (0.1 * evaluate(posX + 1, posY + 0, depth - 1, visited));
		value += (0.1 * evaluate(posX - 1, posY + 0, depth - 1, visited));
		value += (0.1 * evaluate(posX + 0, posY + 1, depth - 1, visited));
		value += (0.1 * evaluate(posX + 0, posY + -1, depth - 1, visited));
		visited[posX][posY] = temp;

		return value;
	}
}
