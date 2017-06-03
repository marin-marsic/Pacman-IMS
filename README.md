# Pacman (Intelligent Multiagent Systems) #

University project.

FER, Zagreb

2017.

The main idea is to implement Pacman and ghosts as independent intelligent agents.   

### Pacman ###

He works alone. He can only see what is near him, radius of his view is three cells. He cannot see item blocked by the wall.   

In my implementation Pacman is using BFS algorithm to find the best move from what he is able to see. He has no memory of past states he visited. He runs away from the gosts unless he picked up "power up", in which case he tryes to eat them. Pacman is not trying to pick up "power up" if no ghost is chasing him. 


### Ghosts ###

They are trying to work together to catch mr. Pacman. 

TODO...
