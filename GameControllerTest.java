package org.example;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GameControllerTest {

    @Before
    public void setUp() {
    }

    @Test
    public void testSetWorker() {
        GameController gameController = new GameController();
        var success = gameController.setWorker(12);
        assertNotNull(success.getGameBoard().getWorkerGrids().get(12));
    }

    @Test
    public void testMoveWorker() {
        GameController gameController = new GameController();
        gameController = gameController.setWorker(12);
        gameController = gameController.setWorker(13);
        gameController = gameController.setWorker(22);
        gameController = gameController.setWorker(23);
        gameController = gameController.selectWorker(12);
        var success = gameController.moveWorker(22);
        assertNotNull(success.getGameBoard().getWorkerGrids().get(22));
    }

    // test move to rival worker's position using Minotaur card.
    @Test
    public void testMoveWorker2() {
        GameController gameController = new GameController();
        GodCards godCard = new Minotaur(Player.PLAYER0);
        gameController = gameController.setWorker(12);
        gameController = gameController.setWorker(13);
        gameController = gameController.setWorker(54);
        gameController = gameController.setWorker(55);
        gameController = gameController.selectWorker(12);
        var success = godCard.godCardFunction(gameController, 55);
        assertEquals(success.getGameBoard().getWorkerGrids().get(55).getPlayer(), Player.PLAYER0);
    }

    // Simulate a game process to test Demeter card function.
    @Test
    public void testMoveWorker3() {
        GameController gameController = new GameController();

        GodCards godCard0 = new Pan(Player.PLAYER0);
        GodCards godCard1 = new Demeter(Player.PLAYER1);
        gameController = gameController.setWorker(12);
        gameController = gameController.setWorker(54);
        gameController = gameController.setWorker(13);
        gameController = gameController.setWorker(55);

        gameController = gameController.selectWorker(13);
        gameController = gameController.moveWorker(23);
        gameController = gameController.buildTower(33);

        gameController = gameController.selectWorker(54);
        gameController = gameController.moveWorker(44);
        gameController = godCard1.godCardFunction(gameController, 34);
        gameController = godCard1.godCardFunction(gameController, 43);


        //test Demeter's first build and second build.
        assertNotNull(gameController.getGameBoard().getTowerGrids().get(34));
        assertNotNull(gameController.getGameBoard().getTowerGrids().get(43));
    }

    // Simulate a game process to test Pan card function.
    @Test
    public void testMoveWorker4() {
        GameController gameController = new GameController();

        GodCards godCard0 = new Pan(Player.PLAYER0);
        GodCards godCard1 = new Demeter(Player.PLAYER1);
        gameController = gameController.setWorker(12);
        gameController = gameController.setWorker(54);
        gameController = gameController.setWorker(13);
        gameController = gameController.setWorker(55);

        gameController = gameController.selectWorker(13);
        gameController = gameController.moveWorker(23);
        gameController = gameController.buildTower(33);

        gameController = gameController.selectWorker(54);
        gameController = gameController.moveWorker(44);
        gameController = godCard1.godCardFunction(gameController, 34);
        gameController = godCard1.godCardFunction(gameController, 43);

        gameController = gameController.selectWorker(23);
        gameController = gameController.moveWorker(33);
        gameController = gameController.buildTower(34);

        gameController = gameController.selectWorker(55);
        gameController = gameController.moveWorker(45);
        gameController = godCard1.godCardFunction(gameController, 35);
        gameController = godCard1.godCardFunction(gameController, 55);

        gameController = gameController.selectWorker(33);
        gameController = gameController.moveWorker(34);
        gameController = gameController.buildTower(43);

        gameController = gameController.selectWorker(44);
        gameController = gameController.moveWorker(54);
        gameController = godCard1.godCardFunction(gameController, 44);
        gameController = godCard1.godCardFunction(gameController, 53);

        gameController = gameController.selectWorker(34);
        gameController = godCard0.godCardFunction(gameController, 24);

        //test jump down two levels using Pan card.
        assertEquals(gameController.getWinner(), Player.PLAYER0);
    }
}
