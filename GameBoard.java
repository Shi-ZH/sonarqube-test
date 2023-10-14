package org.example;

import java.util.HashMap;
import java.util.Map;

/**
 * Game board of the game.It is made up of 5×5 grid.
 * @author Zihong Shi(zihongs@andrew.cmu.edu)
 */
public class GameBoard {
    /** use two HashMap:grids to store worker and tower in a position.
     * The key is coordinate of position range from 11-15, 21-25, 31-35, 41-45, 51-55.
     * 11 represents (row1,col1)grid on the game board. 55 represents (row5,col5)grid on the game board.
     * The value is a worker object or tower object. */
    private Map<Integer, Worker> workerGrids;
    /** store the information of towers on board. */
    private Map<Integer, Tower> towerGrids;
    /** store the optional cells around a worker. */
    private int[] optional = new int[25];
    /** store workers in round. */
    private int[] workersInRound = new int[25];
    /** the step of the game. */
    private String step = "setWorker";
    /** count the number of workers. */
    private int workerCount = 0;
    /** count the number of god cards. */
    private int godCount = 0;
    /** indicate if Demeter is select. */
    private boolean addDemeter = false;
    /** indicate if Minotaur is select. */
    private boolean addMinotaur = false;
    /** indicate if Pan is select. */
    private boolean addPan = false;

    /**
     * A constructor to instantiate a game board.
     */
    public GameBoard() {
        workerGrids = new HashMap<Integer, Worker>();
        towerGrids = new HashMap<Integer, Tower>();
        initializeGrids();
    }

    /**
     * Initialize game board.
     */
    public void initializeGrids() {
        int key = 0;
        for (int i = 1; i <= 5; i++) {
            for (int j = 1; j <= 5; j++) {
                key = i * 10 + j;
                workerGrids.put(key, null);
                towerGrids.put(key, null);
            }
        }
    }

    /**
     * Get worker's player in a specific cell.
     * @return the player.
     */
    public Player getCellPlayer(int x, int y) {
        int coordinate = (x + 1) * 10 + (y + 1);

        if (workerGrids.get(coordinate) == null) {
            return null;
        }
        return workerGrids.get(coordinate).getPlayer();
    }

    /**
     * Get tower in a specific cell.
     * @return the tower object.
     */
    public Tower getCellTower(int x, int y) {
        int coordinate = (x + 1) * 10 + (y + 1);
        return towerGrids.get(coordinate);
    }

    public Map<Integer, Worker> getWorkerGrids() {
        return workerGrids;
    }

    public Map<Integer, Tower> getTowerGrids() {
        return towerGrids;
    }

    public String getStep() {
        return this.step;
    }

    public int getWorkerCount() {
        return workerCount;
    }

    public int[] getOptional() {
        return optional;
    }

    public int[] getWorkersInRound() {
        return workersInRound;
    }

    public int getGodCount() {
        return godCount;
    }

    public void addGodCount() {
        godCount += 1;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public void setWorkerCount() {
        workerCount += 1;
    }

    public void setAddDemeter(boolean addDemeter) {
        this.addDemeter = addDemeter;
    }

    public void setAddMinotaur(boolean addMinotaur) {
        this.addMinotaur = addMinotaur;
    }

    public void setAddPan(boolean addPan) {
        this.addPan = addPan;
    }

    public boolean isAddDemeter() {
        return addDemeter;
    }

    public boolean isAddMinotaur() {
        return addMinotaur;
    }

    public boolean isAddPan() {
        return addPan;
    }
}
