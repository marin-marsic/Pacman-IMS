package agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import com.jme3.math.Vector3f;

import mmaracic.gameaiframework.AgentAI;
import mmaracic.gameaiframework.PacmanAgent;
import mmaracic.gameaiframework.PacmanVisibleWorld;
import mmaracic.gameaiframework.WorldEntity;
import mmaracic.gameaiframework.WorldEntity.WorldEntityInfo;

/**
 * @author marin
 *
 */
public class MyPacmanAI extends AgentAI {

	int ghostVisibleTime = 0;
	ArrayList<int[]> nearGhosts = new ArrayList<>();
	
	int hungerTime = 0;

	@Override
	public int decideMove(ArrayList<int[]> moves, PacmanVisibleWorld mySurroundings, WorldEntityInfo myInfo) {

		int TABLE_SIZE = mySurroundings.getDimensionX();
		int TREE_DEPTH = TABLE_SIZE / 2;
		int TOO_HUNGRY_TIME = 5;
		int GHOST_DANGER_PERIOD = 5;

		if (TREE_DEPTH > 3) {
			TREE_DEPTH = 3;
			TABLE_SIZE = 2 * TREE_DEPTH + 1;
		}

		double[][] visited = new double[TABLE_SIZE][TABLE_SIZE];
		boolean ghostVisible = false;

		// if (nearGhosts.size() > 0) {
		// for (int i = nearGhosts.size() - 1; i >= 0; i--) {
		// int[] nearGhost = nearGhosts.get(i);
		// visited[nearGhost[0]][nearGhost[1]] = (-40000 / (11 -
		// ghostVisibleTime));
		// }
		// }

		boolean powerUP = myInfo.hasProperty(PacmanAgent.powerupPropertyName);
		Vector3f pos = myInfo.getPosition();

		for (int i = -TREE_DEPTH; i <= TREE_DEPTH; i++) {
			for (int j = -TREE_DEPTH; j <= TREE_DEPTH; j++) {
				if (i == 0 && j == 0)
					continue;

				ArrayList<WorldEntity.WorldEntityInfo> neighPosInfos = mySurroundings.getWorldInfoAt(i, j);
				HashMap<Integer,Object> metaHash = mySurroundings.getWorldMetadataAt(i, j);

				// look around for info
				if (neighPosInfos != null) {
					for (WorldEntity.WorldEntityInfo info : neighPosInfos) {
						if (info.getIdentifier().compareToIgnoreCase("Pacman") == 0) {
							visited[j + TREE_DEPTH][i + TREE_DEPTH] += 0;
						} else if (info.getIdentifier().compareToIgnoreCase("Wall") == 0) {
							visited[j + TREE_DEPTH][i + TREE_DEPTH] += 0;
							if (ghostVisibleTime > GHOST_DANGER_PERIOD-1) {
								visited[j + TREE_DEPTH][i + TREE_DEPTH] -= 2;
							}
						} else if (info.getIdentifier().compareToIgnoreCase("Point") == 0) {
							visited[j + TREE_DEPTH][i + TREE_DEPTH] += 0.0;
							if (ghostVisibleTime <= 0) {
								visited[j + TREE_DEPTH][i + TREE_DEPTH] += 1;
							}
						} else if (info.getIdentifier().compareToIgnoreCase("Powerup") == 0) {
							visited[j + TREE_DEPTH][i + TREE_DEPTH] += 0.5;
							if (ghostVisibleTime > 0) {
								visited[j + TREE_DEPTH][i + TREE_DEPTH] += 300;
							}
						} else if (info.getIdentifier().compareToIgnoreCase("Ghost") == 0) {
//							printStatus("DANGER " + (j + TREE_DEPTH) + " " + (i + TREE_DEPTH));
							ghostVisible = true;
							ghostVisibleTime = GHOST_DANGER_PERIOD;
							int[] ghostLocation = new int[2];
							ghostLocation[0] = j + TREE_DEPTH;
							ghostLocation[1] = i + TREE_DEPTH;
							nearGhosts.add(ghostLocation);
							visited[j + TREE_DEPTH][i + TREE_DEPTH] -= 40000;
							
							if (powerUP) {
								visited[j + TREE_DEPTH][i + TREE_DEPTH] += 50000;
							}
						} else {
							printStatus("I dont know what " + info.getIdentifier() + " is!");
						}
					}
				}
				
				// check metadata if hungry
				if (metaHash != null ) {
					
					if (hungerTime >= TOO_HUNGRY_TIME) {
						for (Integer id : metaHash.keySet()) {
	                        if (id == myInfo.getID() && (metaHash.get(id)) != null) {
//	                            printStatus("VISITED " + i + " " + j);
	                            visited[j + TREE_DEPTH][i + TREE_DEPTH] += (double) metaHash.get(id);
	                        }
	                    }
					}
					
//					if (ghostVisibleTime >= 2) {
//						for (Integer id : metaHash.keySet()) {
//	                        if (id == myInfo.getID() && (metaHash.get(-1)) != null) {
//	                            printStatus("VISITED " + i + " " + j + " " + (double) metaHash.get(-1));
//	                            visited[j + TREE_DEPTH][i + TREE_DEPTH] += (double) metaHash.get(-1);
//	                        }
//	                    }
//					}					
					
				}

			}
		}

		ghostVisibleTime--;
		double best = -1000000;
		int bestID = 0;
		int secondBestId = 0;
		int numOfMoves = 0;

		// find best move
		for (int i = moves.size() - 1; i >= 0; i--) {
			int[] move = moves.get(i);
			double moveValue = evaluate(TREE_DEPTH + move[1], TREE_DEPTH + move[0], TREE_DEPTH - 1, visited);
			numOfMoves++;
			if (moveValue != 0) {
			}
			if (moveValue > best) {
				best = moveValue;
				secondBestId = bestID;
				bestID = i;
			} else if (moveValue == best) {
				double d = Math.random();
				if (d > 0.5) {
					best = moveValue;
					secondBestId = bestID;
					bestID = i;
				}
			}
//			printStatus("" + bestID + " " + move[0] + " " + move[1] + " ->   " + moveValue);
		}

//		printStatus("bestid: " + bestID + " " + best);
		
		if (best <= 0.000001 && ghostVisibleTime <= 0) {
			hungerTime++;
		}
		else {
			hungerTime = 0;
		}
		
		// if hungry for too long, leave metadata
		if (hungerTime >= TOO_HUNGRY_TIME) {
			hungerTime = TOO_HUNGRY_TIME;
			
//			printStatus("HUNGRY");
			HashMap<Integer,Object> metaHash = mySurroundings.getWorldMetadataAt(0,0);
			double punishment = 0;
			if (metaHash.get(myInfo.getID()) != null) {
				punishment = (double) metaHash.get(myInfo.getID());
			}
			metaHash.put(myInfo.getID(), punishment-10);
			return bestID;
		}

		// if ghost is near, play carefully
//		printStatus("ghost: " + ghostVisibleTime);
		if (ghostVisibleTime > 0) {
//			if (numOfMoves > 2) {
//				double d = Math.random();
//				if (d < 0.3) {
//					return secondBestId;
//				}
//			}
//			if (ghostVisibleTime >= 2) {
//				HashMap<Integer,Object> metaHash = mySurroundings.getWorldMetadataAt(0,0);
//				double punishment = 0;
//				if (metaHash.get(-1) != null) {
//					punishment = (double) metaHash.get(-1);
//				}
//				metaHash.put(-1, punishment-0.1);
//				return bestID;
//			}
			return bestID;
		}

		// explore if no ghosts are near
		else {
			ghostVisibleTime = 0;
			nearGhosts = new ArrayList<>();
			double d = Math.random();
//			if (d < 0.2) {
//				Random rnd = new Random();
//				int n = rnd.nextInt(moves.size());
//				return n;
//			}
			return bestID;
		}

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
