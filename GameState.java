package org.example;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class GameState {
    private final Cell[] cells;
    private final Player winner;
    private final int turn;
    private int godCount;

    private GameState(Cell[] cells, Player winner, int turn, int godCount) {
        this.cells = cells;
        this.turn = turn;
        this.winner = winner;
        this.godCount = godCount;
    }

    /**
     * Get current game state.
     * @param gameController the game.
     */
    public static GameState getState(GameController gameController) {
        Cell[] cells = getCells(gameController);
        Player winner = getWinner(gameController);
        int turn = getTurn(gameController);
        int godCount = getGodCount(gameController.getGameBoard());
        return new GameState(cells, winner, turn, godCount);
    }

    @Override
    public String toString() {
        if (this.winner == null) {
            return "{ \"cells\": " + Arrays.toString(this.cells) + "," +
                    "\"turn\": " + String.valueOf(this.turn) + "," +
                    "\"godCount\": " + String.valueOf(godCount) + "}";
        }
        return "{ \"cells\": " + Arrays.toString(this.cells) + "," +
                "\"turn\": " + String.valueOf(this.turn) + "," +
                "\"winner\": " + String.valueOf(this.winner.value) + "," +
                "\"godCount\": " + String.valueOf(godCount) + "}";
    }

    /**
     * Get information in every cell.
     * @param gameController the game.
     */
    private static Cell @NotNull [] getCells(GameController gameController) {
        Cell[] cells = new Cell[25];
        GameBoard gameBoard = gameController.getGameBoard();
        for (int x = 0; x <= 4; x++) {
            for (int y = 0; y <= 4; y++) {
                String text = "";
                String link = "";
                String clazz = "";
                Player player = gameBoard.getCellPlayer(x, y);
                Tower tower = gameBoard.getCellTower(x, y);
                if (tower == null) {
                    if (player == Player.PLAYER0) text = "X";
                    else if (player == Player.PLAYER1) text = "O";
                    else if (player == null) {
                        clazz = "playable";
                    }
                } else if (tower.getBlockHeight() == 1) {
                    if (player == Player.PLAYER0) text = "[  X  ]";
                    else if (player == Player.PLAYER1) text = "[  O  ]";
                    else if (player == null) {
                        text = "[     ]";
                    }
                } else if (tower.getBlockHeight() == 2) {
                    if (player == Player.PLAYER0) text = "[[ X ]]";
                    else if (player == Player.PLAYER1) text = "[[ O ]]";
                    else if (player == null) {
                        text = "[[   ]]";
                    }
                } else if (tower.getBlockHeight() == 3) {
                    if (player == Player.PLAYER0) text = "[[[X]]]";
                    else if (player == Player.PLAYER1) text = "[[[O]]]";
                    else if (player == null) {
                        text = "[[[ ]]]";
                    }
                } else if (tower.getBlockHeight() == 4) {
                    text = "[[[●]]]";
                }
                if (gameBoard.getOptional()[5 * x + y] == 1) {
                    clazz = "move";
                }
                if (gameBoard.getOptional()[5 * x + y] == 2) {
                    clazz = "build";
                }
                if (gameBoard.getOptional()[5 * x + y] == 3) {
                    clazz = "secondBuild";
                }
                if (gameBoard.getWorkersInRound()[5 * x + y] == 4) {
                    clazz = "workersInRound";
                }
                link = "/play?x=" + x + "&y=" + y;
                cells[5 * y + x] = new Cell(text, clazz, link);
            }
        }
        return cells;
    }

    /**
     * Get winner of game.
     * @param gameController the game.
     */
    private static Player getWinner(GameController gameController) {
        return gameController.getWinner();
    }

    /**
     * Get turn of game.
     * @param gameController the game.
     */
    private static int getTurn(GameController gameController) {
        return gameController.getPlayer().value;
    }

    /**
     * Get current number of god cards.
     * @param gameBoard the game board.
     */
    private static int getGodCount(GameBoard gameBoard) {
        return gameBoard.getGodCount();
    }
}

class Cell {
    private final String text;
    private final String clazz;
    private final String link;

    Cell(String text, String clazz, String link) {
        this.text = text;
        this.clazz = clazz;
        this.link = link;
    }

    public String getText() {
        return this.text;
    }

    public String getClazz() {
        return this.clazz;
    }

    public String getLink() {
        return this.link;
    }

    @Override
    public String toString() {
        return "{ \"text\": \"" + this.text + "\"," +
                " \"clazz\": \"" + this.clazz + "\"," +
                " \"link\": \"" + this.link + "\"}";
    }
}
