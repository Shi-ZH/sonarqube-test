package org.example;

import java.io.IOException;
import java.util.Map;
import fi.iki.elonen.NanoHTTPD;

public class App extends NanoHTTPD {

    public static void main(String[] args) {
        try {
            new App();
        } catch (IOException ioe) {
            System.err.println("Couldn't start server:\n" + ioe);
        }
    }

    private GameController gameController;

    public App() throws IOException {
        super(8080);

        this.gameController = new GameController();

        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("\nRunning!\n");
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Map<String, String> params = session.getParms();

        GameBoard gameBoard = gameController.getGameBoard();
        GodCards godCard;

        if (uri.equals("/newgame")) {
            gameController = new GameController();
        } else if (uri.equals("/play")) {
            int coordinate = (Integer.parseInt(params.get("x")) + 1) * 10 + (Integer.parseInt(params.get("y")) + 1);

            if (gameBoard.getStep().equals("setWorker")) {
                gameController = gameController.setWorker(coordinate);
            } else if (gameBoard.getStep().equals("selectWorker")) {
                gameController = gameController.selectWorker(coordinate);
            } else if (gameBoard.getStep().equals("move")) {
                godCard = gameController.checkCard(gameController.getPlayer());
                // if no god cards.
                if (godCard == null) {
                    gameController = gameController.moveWorker(coordinate);
                }
                // if game has god cards.
                if (godCard != null) {
                    // if god card can be used.
                    if (godCard.getState().equals("move")) {
                        gameController = godCard.godCardFunction(gameController, coordinate);
                    } else {
                        gameController = gameController.moveWorker(coordinate);
                    }
                }
            } else if (gameBoard.getStep().equals("build") || gameBoard.getStep().equals("secondBuild")) {
                godCard = gameController.checkCard(gameController.getPlayer());
                // if no god cards.
                if (godCard == null) {
                    gameController = gameController.buildTower(coordinate);
                }
                // if game has god cards.
                if (godCard != null) {
                    // if god card can be used.
                    if (godCard.getState().equals("build")) {
                        gameController = godCard.godCardFunction(gameController, coordinate);
                    } else {
                        gameController = gameController.buildTower(coordinate);
                    }
                }
            } else if (gameBoard.getStep().equals("over")) {
                gameController = gameController.gameOver();
            }
        } else if (uri.equals("/Demeter")) {
            gameController = gameController.selectDemeter();
        } else if (uri.equals("/Minotaur")) {
            gameController = gameController.selectMinotaur();
        } else if (uri.equals("/Pan")) {
            gameController = gameController.selectPan();
        }
        /*else if (uri.equals("/undo")) {
            this.game = this.game.undo();
        }*/

        // Extract the view-specific data from the game and apply it to the template.
        GameState gameState = GameState.getState(this.gameController);
        return newFixedLengthResponse(gameState.toString());
    }
}