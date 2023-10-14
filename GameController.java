package org.example;

import java.util.ArrayList;
import java.util.List;

/**
 * A game controller to control the flow of the game.
 * controller includes every step of game,such as set workers,
 * move, build, and determine if a player wins the game.
 * @author Zihong Shi(zihongs@andrew.cmu.edu)
 */
public class GameController {
    /** A game board. */
    private GameBoard gameBoard;
    /** player in round. */
    private Player player;
    /** The worker to be moved. */
    private Worker moveWorker;
    /** A list to store picked two god cards. */
    private List<GodCards> godCardsList;

    /** A controller constructor with no args. */
    public GameController() {
        this(new GameBoard(), Player.PLAYER0, null, new ArrayList<>());
    }

    /** A method to set worker's start location.
     * @param gameBoard a game board.
     * @param nextPlayer player in next round.
     * @param moveWorker worker to be moved.
     * @param godCardsList List to store picked two god cards.
     */
    public GameController(GameBoard gameBoard, Player nextPlayer, Worker moveWorker, List<GodCards> godCardsList) {
        this.gameBoard = gameBoard;
        this.player = nextPlayer;
        this.moveWorker = moveWorker;
        this.godCardsList = godCardsList;
    }

    /** Initialize Demeter god. */
    public GameController selectDemeter() {
        GodCards godCard = new Demeter(player);

        if (!gameBoard.isAddDemeter()) {
            godCardsList.add(godCard);
            gameBoard.addGodCount();
            gameBoard.setAddDemeter(true);
        }
        Player nextPlayer = player == Player.PLAYER0 ? Player.PLAYER1 : Player.PLAYER0;
        return new GameController(gameBoard, nextPlayer, null, godCardsList);
    }

    /** Initialize Minotaur god. */
    public GameController selectMinotaur() {
        GodCards godCard = new Minotaur(player);

        if (!gameBoard.isAddMinotaur()) {
            godCardsList.add(godCard);
            gameBoard.addGodCount();
            gameBoard.setAddMinotaur(true);
        }
        Player nextPlayer = player == Player.PLAYER0 ? Player.PLAYER1 : Player.PLAYER0;
        return new GameController(gameBoard, nextPlayer, null, godCardsList);
    }

    /** Initialize Pan god. */
    public GameController selectPan() {
        GodCards godCard = new Pan(player);

        if (!gameBoard.isAddPan()) {
            godCardsList.add(godCard);
            gameBoard.addGodCount();
            gameBoard.setAddPan(true);
        }
        Player nextPlayer = player == Player.PLAYER0 ? Player.PLAYER1 : Player.PLAYER0;
        return new GameController(gameBoard, nextPlayer, null, godCardsList);
    }

    /** Check which player the card belongs to. */
    public GodCards checkCard(Player player) {
        for (GodCards card : godCardsList) {
            if (card.getPlayer() == player) {
                return card;
            }
        }
        return null;
    }

    /** Set workers' initial position.
     * @param coordinate The selected coordinate.
     */
    public GameController setWorker(int coordinate) {
        Worker worker = new Worker();
        if (!worker.setLocation(coordinate, gameBoard)) {
            //if setting worker fails.
            return this;
        }
        worker.setPlayer(player);
        gameBoard.getWorkerGrids().put(coordinate, worker);
        gameBoard.setWorkerCount();
        if (gameBoard.getWorkerCount() == 4) {
            gameBoard.setStep("selectWorker");
            Player nextPlayer = player == Player.PLAYER0 ? Player.PLAYER1 : Player.PLAYER0;
            workersInRound(nextPlayer);
            return new GameController(gameBoard, nextPlayer, null, godCardsList);
        }
        //change player.
        Player nextPlayer = player == Player.PLAYER0 ? Player.PLAYER1 : Player.PLAYER0;
        return new GameController(gameBoard, nextPlayer, null, godCardsList);
    }

    /** Select a worker to be moved.
     * @param coordinate The selected coordinate.
     */
    public GameController selectWorker(int coordinate) {
        //if selects rival's worker.
        if (gameBoard.getWorkerGrids().get(coordinate) != null) {
            if (gameBoard.getWorkerGrids().get(coordinate).getPlayer() != player) {
                return this;
            }
        }
        //if selects an empty cell.
        if (gameBoard.getWorkerGrids().get(coordinate) == null) {
            return this;
        }
        //select a worker.
        moveWorker = gameBoard.getWorkerGrids().get(coordinate);
        gameBoard.setStep("move");
        clearWorkersInRound();
        optionalCell(coordinate);
        return new GameController(gameBoard, player, moveWorker, godCardsList);
    }

    /** Move a worker.
     * @param coordinate The selected coordinate.
     */
    public GameController moveWorker(int coordinate) {
        //if the player selects the other worker.
        if (gameBoard.getWorkerGrids().get(coordinate) != null &&
                moveWorker.getLocation() != coordinate &&
                    gameBoard.getWorkerGrids().get(coordinate).getPlayer() == player) {
            clearOptionalCell();

            moveWorker = gameBoard.getWorkerGrids().get(coordinate);
            optionalCell(coordinate);
            return new GameController(gameBoard, player, moveWorker, godCardsList);
        }
        //if the player selects an empty cell that is not around the worker.
        if (gameBoard.getWorkerGrids().get(coordinate) == null &&
                gameBoard.getOptional()[(coordinate / 10 - 1) * 5 + (coordinate % 10 - 1)] == 0) {
            return this;
        }

        if (!moveWorker.move(coordinate, gameBoard)) {
            //if move fails.
            return this;
        }
        //if a player wins the game.
        if (getWinner() != null) {
            gameBoard.setStep("over");
            clearOptionalCell();
            return this;
        }
        gameBoard.setStep("build");
        clearOptionalCell();
        optionalCell(coordinate);
        return new GameController(gameBoard, player, moveWorker, godCardsList);
    }

    /** Build a tower.
     * @param coordinate The selected coordinate.
     */
    public GameController buildTower(int coordinate) {
        if (!moveWorker.build(coordinate, gameBoard)) {
            //if build fails.
            return this;
        }
        gameBoard.setStep("selectWorker");
        clearOptionalCell();
        Player nextPlayer = player == Player.PLAYER0 ? Player.PLAYER1 : Player.PLAYER0;
        workersInRound(nextPlayer);
        return new GameController(gameBoard, nextPlayer, null, godCardsList);
    }

    /** End the game. */
    public GameController gameOver() {
        return new GameController(gameBoard, player, null, godCardsList);
    }

    /** Find optional cells around the worker .
     * @param coordinate The selected coordinate.
     */
    public void optionalCell(int coordinate) {
        int cell = 0;

        if (gameBoard.getStep().equals("move")) {
            for (int x = 1; x <= 5; x++) {
                for (int y = 1; y <= 5; y++) {
                    cell = x * 10 + y;
                    int cx = coordinate / 10;
                    int cy = coordinate % 10;
                    if (Math.abs(x - cx) <= 1 && Math.abs(y - cy) <= 1) {
                        if (cell == coordinate || gameBoard.getWorkerGrids().get(cell) != null) {
                            continue;
                        }
                        if (gameBoard.getTowerGrids().get(cell) != null) {
                            if (gameBoard.getTowerGrids().get(cell).getBlockHeight() == 4) {
                                continue;
                            }
                            if (gameBoard.getTowerGrids().get(cell).getBlockHeight() -
                                    gameBoard.getWorkerGrids().get(coordinate).getLevel() > 1) {
                                continue;
                            }
                        }
                        gameBoard.getOptional()[5 * (x - 1) + (y - 1)] = 1;
                    }
                }
            }
        }
        if (gameBoard.getStep().equals("build")) {
            for (int x = 1; x <= 5; x++) {
                for (int y = 1; y <= 5; y++) {
                    cell = x * 10 + y;
                    int cx = coordinate / 10;
                    int cy = coordinate % 10;
                    if (Math.abs(x - cx) <= 1 && Math.abs(y - cy) <= 1) {
                        if (cell == coordinate || gameBoard.getWorkerGrids().get(cell) != null) {
                            continue;
                        }
                        if (gameBoard.getTowerGrids().get(cell) != null) {
                            if (gameBoard.getTowerGrids().get(cell).getBlockHeight() == 4) {
                                continue;
                            }
                        }
                        gameBoard.getOptional()[5 * (x - 1) + (y - 1)] = 2;
                    }
                }
            }
        }
    }

    /** clear optional cells around the worker. */
    public void clearOptionalCell() {
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                gameBoard.getOptional()[5 * x + y] = 0;
            }
        }
    }

    /** Find workers in round.
     * @param nextPlayer Player in next round.
     */
    public void workersInRound(Player nextPlayer) {
        int cell = 0;

        if (gameBoard.getStep().equals("selectWorker")) {
            for (int x = 1; x <= 5; x++) {
                for (int y = 1; y <= 5; y++) {
                    cell = x * 10 + y;
                    if (gameBoard.getWorkerGrids().get(cell) != null &&
                            gameBoard.getWorkerGrids().get(cell).getPlayer() == nextPlayer) {
                        gameBoard.getWorkersInRound()[5 * (x - 1) + (y - 1)] = 4;
                    }
                }
            }
        }
    }

    public void clearWorkersInRound() {
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                gameBoard.getWorkersInRound()[5 * x + y] = 0;
            }
        }
    }

    public Worker getMoveWorker() {
        return this.moveWorker;
    }

    public Player getWinner() {
        if (player.getWinTag()) {
            return player;
        }
        return null;
    }

    public Player getPlayer() {
        return player;
    }

    public GameBoard getGameBoard() {
        return gameBoard;
    }

    public List<GodCards> getGodCardsList() {
        return godCardsList;
    }
}
