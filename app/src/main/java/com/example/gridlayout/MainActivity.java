package com.example.gridlayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;
public class MainActivity extends AppCompatActivity {

    private static final int COLUMN_COUNT = 10;
    private static boolean isFlagging = false;
    private static int flagLeft = 4;
    private final int rows = 12; // Number of rows
    private final int cols = 10; // Number of columns
    private int[][] cells; // Array to store index of grid representing cells
    private boolean[][] isMine; // Array to track mine locations
    private boolean[][] isRevealed; // Array to track revealed cells
    private boolean[][] isFlagged; // Array to track flagged cells
    private boolean gameOver = false;
    private int clock = 0;
    private int secondsElapsed = 0;
    private boolean running = false;
    private boolean gameWon = false;



    // save the TextViews of all cells in an array, so later on,
    // when a TextView is clicked, we know which cell it is
    private ArrayList<TextView> cell_tvs;

    private int dpToPixel(int dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    // Following function generate the 12x10 grid layout for the game
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cell_tvs = new ArrayList<>();

        GridLayout grid = (GridLayout) findViewById(R.id.gridLayout01);
        for (int i = 0; i<=11; i++) {
            for (int j = 0; j <= 9; j++) {
                TextView tv = new TextView(this);
                tv.setHeight(dpToPixel(32));
                tv.setWidth(dpToPixel(32));
                tv.setTextSize(16);//dpToPixel(32) );
                tv.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                tv.setTextColor(Color.GRAY);
                tv.setBackgroundColor(Color.GRAY);
                tv.setOnClickListener(this::onClickTV);

                GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
                lp.setMargins(dpToPixel(2), dpToPixel(2), dpToPixel(2), dpToPixel(2));
                lp.rowSpec = GridLayout.spec(i);
                lp.columnSpec = GridLayout.spec(j);

                grid.addView(tv, lp);

                cell_tvs.add(tv);
            }
        }
        initializeGame();
    }

    private int findIndexOfCellTextView(TextView tv) {
        for (int n=0; n<cell_tvs.size(); n++) {
            if (cell_tvs.get(n) == tv)
                return n;
        }
        return -1;
    }

    public void onClickTV(View view){
        TextView tv = (TextView) view;
        int n = findIndexOfCellTextView(tv);
        int i = n/COLUMN_COUNT;
        int j = n%COLUMN_COUNT;
        tv.setTextColor(Color.GRAY);
        tv.setBackgroundColor(Color.GREEN);
        onCellClick(i,j,tv);
    }

    private void initializeGame() {
        cells = new int[rows][cols];
        isMine = new boolean[rows][cols];
        isRevealed = new boolean[rows][cols];
        isFlagged = new boolean[rows][cols];
        gameOver = false;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                cells[row][col] = row*10+col; // finding the index of the grid
                isMine[row][col] = false;
                isRevealed[row][col] = false;
                isFlagged[row][col] = false;
                runTimer();
            }
        }
        // Generate mine locations
        generateMines();
    }

    private void generateMines() {
        Random random = new Random();
        int minesPlaced = 0;

        // Number of mines
        int numMines = 4;
        while (minesPlaced < numMines) {
            int row = random.nextInt(rows);
            int col = random.nextInt(cols);

            if (!isMine[row][col]) {
                isMine[row][col] = true;
                minesPlaced++;
            }
        }
    }
    private void onCellClick(int row, int col,TextView tv) {
        if(gameOver){
            resultPage();
        }else {
            if (!isFlagging) {
                if (!isRevealed[row][col]) {
                    isRevealed[row][col] = true;

                    if (isMine[row][col]) {
                        // Game over, the clicked cell is a mine
                        gameOver = true;
                        running = false;
                        gameWon = false;
                        revealMines();
                    } else {
                        // Calculate the number of adjacent mines
                        int adjacentMines = countAdjacentMines(row, col);

                        // Update the cell's appearance and text
                        tv.setText(String.valueOf(adjacentMines));

                        if (adjacentMines == 0) {
                            // If the cell has no adjacent mines, reveal adjacent cells recursively
                            revealAdjacentCells(row, col);
                        }
                    }
                }
            } else {
                placeFlag(row, col, tv);
            }
            // Check for win condition
            if (checkWinCondition()) {
                gameOver = true;
            }
        }
    }

    private int countAdjacentMines(int row, int col) {
        int count = 0;
        int[][] directions = {
                {-1, -1}, {-1, 0}, {-1, 1},
                {0, -1}, /*(row, col)*/ {0, 1},
                {1, -1}, {1, 0}, {1, 1}
        };

        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];

            if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols && isMine[newRow][newCol]) {
                count++;
            }
        }

        return count;
    }

    private void revealAdjacentCells(int row, int col) {
        int[][] directions = {
                {-1, -1}, {-1, 0}, {-1, 1},
                {0, -1}, /*(row, col)*/ {0, 1},
                {1, -1}, {1, 0}, {1, 1}
        };

        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];

            if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols && !isRevealed[newRow][newCol]) {
               onClickTV(cell_tvs.get(cells[newRow][newCol]));
            }
        }
    }
    private void revealMines() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (isMine[row][col]) {
                    cell_tvs.get(cells[row][col]).setText(R.string.mine); // Display mines
                    gameOver = true;
                }
            }
        }
    }

    private boolean checkWinCondition() {
        // Check if all non-mine cells are revealed
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (!isMine[row][col] && !isRevealed[row][col]) {
                    return false;
                }
            }
        }
        running = false;
        gameWon = true;
        return true;
    }

    private void placeFlag(int row, int col, TextView tv){
        if (flagLeft > 0) {
            if (isFlagged[row][col]) {
                tv.setText("");
                flagLeft++;
                isFlagged[row][col] = false;
            } else if(!isFlagged[row][col]&&!isRevealed[row][col]) {
                String flag = getString(R.string.flag);
                tv.setText(flag);
                flagLeft--;
                isFlagged[row][col] = true;
                isRevealed[row][col] = true;
            }
        }
        TextView tv1 = (TextView) findViewById(R.id.flagLeft);
        tv1.setText(String.valueOf(flagLeft));
    }

    public void onButtonClick(View view) {
        TextView tv = (TextView) view;
        if (!isFlagging) {
            String flag = getString(R.string.flag);
            tv.setText(flag);
            isFlagging = true;
        }else{
            String pick = getString(R.string.pick);
            tv.setText(pick);
            isFlagging = false;
        }
    }

    private void runTimer() {
        running = true;
        final TextView timeView = (TextView) findViewById(R.id.clockTime);
        final Handler handler = new Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {
                secondsElapsed = clock/60;
                String time = String.valueOf(secondsElapsed);
                timeView.setText(time);

                if (running) {
                    clock++;
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    private void resultPage(){
        String message;
        Intent intent = new Intent(this, Result.class);
        if(gameWon){
            message = "Used " + secondsElapsed + " seconds. You won. Good job!";
        }else{
            message = "Used " + secondsElapsed + " seconds. You lost. Let's try again!";
        }
        intent.putExtra("com.example.gridlayout.MESSAGE", message);
        startActivity(intent);
    }
}