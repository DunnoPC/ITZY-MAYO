package com.syu.itzy_mayo;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.FrameLayout;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import java.util.Random;
import android.view.View;

public class Game2048Activity extends AppCompatActivity {

    private GridLayout gridLayout;
    private TextView gameOverText;
    private final int GRID_SIZE = 4;
    private final TextView[][] cells = new TextView[GRID_SIZE][GRID_SIZE];
    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_2048);

        gameOverText = findViewById(R.id.game_over_text);
        FrameLayout innerContainer = findViewById(R.id.game_inner_container);

        innerContainer.post(() -> {
            int size = Math.min(innerContainer.getWidth(), innerContainer.getHeight());
            int marginPerSide = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
            int totalMargin = marginPerSide * 2 * GRID_SIZE;
            int cellSize = (size - totalMargin) / GRID_SIZE;

            gridLayout = new GridLayout(this);
            gridLayout.setColumnCount(GRID_SIZE);
            gridLayout.setRowCount(GRID_SIZE);
            gridLayout.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT));

            for (int y = 0; y < GRID_SIZE; y++) {
                for (int x = 0; x < GRID_SIZE; x++) {
                    TextView cell = createCell(cellSize);
                    gridLayout.addView(cell);
                    cells[x][y] = cell;
                }
            }

            innerContainer.addView(gridLayout);
            addRandomTile();
            addRandomTile();
        });
    }

    private void showGameOver() {
        gameOverText.setVisibility(View.VISIBLE);
        gameOverText.bringToFront();
        // Force redraw
        gameOverText.invalidate();
    }

    private TextView createCell(int size) {
        TextView cell = new TextView(this);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = size;
        params.height = size;
        params.setMargins(4, 4, 4, 4);
        cell.setLayoutParams(params);
        cell.setGravity(Gravity.CENTER);
        cell.setTypeface(Typeface.DEFAULT_BOLD);
        cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        cell.setBackgroundColor(Color.LTGRAY);
        return cell;
    }

    private boolean canMove() {
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                String current = cells[x][y].getText().toString();
                if (current.isEmpty()) return true;
                int value = Integer.parseInt(current);
                if (x + 1 < GRID_SIZE && value == getValueAt(x + 1, y)) return true;
                if (x - 1 >= 0 && value == getValueAt(x - 1, y)) return true;
                if (y + 1 < GRID_SIZE && value == getValueAt(x, y + 1)) return true;
                if (y - 1 >= 0 && value == getValueAt(x, y - 1)) return true;
            }
        }
        return false;
    }

    private int getValueAt(int x, int y) {
        String text = cells[x][y].getText().toString();
        return text.isEmpty() ? 0 : Integer.parseInt(text);
    }
    private boolean isBoardFull() {
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                if (cells[x][y].getText().toString().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void addRandomTile() {
        boolean isFull = true;
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                if (cells[x][y].getText().toString().isEmpty()) {
                    isFull = false;
                }
            }
        }

        if (isFull) {
            if (!canMove()) {
                showGameOver();
                return;
            }
            return;
        }

        int x, y;
        do {
            x = random.nextInt(GRID_SIZE);
            y = random.nextInt(GRID_SIZE);
        } while (!cells[x][y].getText().toString().isEmpty());

        cells[x][y].setText(random.nextInt(10) < 9 ? "2" : "4");
        updateCellColor(cells[x][y]);
    }

    private void updateCellColor(TextView cell) {
        switch (cell.getText().toString()) {
            case "2": cell.setBackgroundColor(Color.parseColor("#EEE4DA")); break;
            case "4": cell.setBackgroundColor(Color.parseColor("#EDE0C8")); break;
            case "8": cell.setBackgroundColor(Color.parseColor("#F2B179")); break;
            case "16": cell.setBackgroundColor(Color.parseColor("#F59563")); break;
            case "32": cell.setBackgroundColor(Color.parseColor("#F67C5F")); break;
            case "64": cell.setBackgroundColor(Color.parseColor("#F65E3B")); break;
            case "128": cell.setBackgroundColor(Color.parseColor("#EDCF72")); break;
            case "256": cell.setBackgroundColor(Color.parseColor("#EDCC61")); break;
            case "512": cell.setBackgroundColor(Color.parseColor("#EDC850")); break;
            case "1024": cell.setBackgroundColor(Color.parseColor("#EDC53F")); break;
            case "2048": cell.setBackgroundColor(Color.parseColor("#EDC22E")); break;
            default: cell.setBackgroundColor(Color.LTGRAY); break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean moved;

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                moved = swipeLeft();
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                moved = swipeRight();
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                moved = swipeUp();
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                moved = swipeDown();
                break;
            default:
                return super.onKeyDown(keyCode, event);
        }

        if (moved) {
            addRandomTile();
        }

        // Always check game over regardless of move
        if (isBoardFull() && !canMove()) {
            new android.os.Handler().postDelayed(this::showGameOver, 100);
        }

        return true;
    }

    private boolean swipeLeft() {
        boolean moved = false;
        for (int y = 0; y < GRID_SIZE; y++) {
            int[] newRow = new int[GRID_SIZE];
            boolean[] merged = new boolean[GRID_SIZE];
            int index = 0;

            for (int x = 0; x < GRID_SIZE; x++) {
                String cellText = cells[x][y].getText().toString();
                if (!cellText.isEmpty()) {
                    int value = Integer.parseInt(cellText);
                    if (index > 0 && newRow[index - 1] == value && !merged[index - 1]) {
                        newRow[index - 1] *= 2;
                        merged[index - 1] = true;
                        moved = true;
                    } else {
                        if (newRow[index] != 0) moved = true;
                        newRow[index++] = value;
                    }
                }
            }

            for (int x = 0; x < GRID_SIZE; x++) {
                String newValue = newRow[x] == 0 ? "" : String.valueOf(newRow[x]);
                if (!cells[x][y].getText().toString().equals(newValue)) moved = true;
                cells[x][y].setText(newValue);
                updateCellColor(cells[x][y]);
            }
        }
        return moved;
    }

    private boolean swipeRight() {
        boolean moved = false;
        for (int y = 0; y < GRID_SIZE; y++) {
            int[] newRow = new int[GRID_SIZE];
            boolean[] merged = new boolean[GRID_SIZE];
            int index = GRID_SIZE - 1;

            for (int x = GRID_SIZE - 1; x >= 0; x--) {
                String cellText = cells[x][y].getText().toString();
                if (!cellText.isEmpty()) {
                    int value = Integer.parseInt(cellText);
                    if (index < GRID_SIZE - 1 && newRow[index + 1] == value && !merged[index + 1]) {
                        newRow[index + 1] *= 2;
                        merged[index + 1] = true;
                        moved = true;
                    } else {
                        if (newRow[index] != 0) moved = true;
                        newRow[index--] = value;
                    }
                }
            }

            for (int x = 0; x < GRID_SIZE; x++) {
                String newValue = newRow[x] == 0 ? "" : String.valueOf(newRow[x]);
                if (!cells[x][y].getText().toString().equals(newValue)) moved = true;
                cells[x][y].setText(newValue);
                updateCellColor(cells[x][y]);
            }
        }
        return moved;
    }

    private boolean swipeUp() {
        boolean moved = false;
        for (int x = 0; x < GRID_SIZE; x++) {
            int[] newCol = new int[GRID_SIZE];
            boolean[] merged = new boolean[GRID_SIZE];
            int index = 0;

            for (int y = 0; y < GRID_SIZE; y++) {
                String cellText = cells[x][y].getText().toString();
                if (!cellText.isEmpty()) {
                    int value = Integer.parseInt(cellText);
                    if (index > 0 && newCol[index - 1] == value && !merged[index - 1]) {
                        newCol[index - 1] *= 2;
                        merged[index - 1] = true;
                        moved = true;
                    } else {
                        if (newCol[index] != 0) moved = true;
                        newCol[index++] = value;
                    }
                }
            }

            for (int y = 0; y < GRID_SIZE; y++) {
                String newValue = newCol[y] == 0 ? "" : String.valueOf(newCol[y]);
                if (!cells[x][y].getText().toString().equals(newValue)) moved = true;
                cells[x][y].setText(newValue);
                updateCellColor(cells[x][y]);
            }
        }
        return moved;
    }

    private boolean swipeDown() {
        boolean moved = false;
        for (int x = 0; x < GRID_SIZE; x++) {
            int[] newCol = new int[GRID_SIZE];
            boolean[] merged = new boolean[GRID_SIZE];
            int index = GRID_SIZE - 1;

            for (int y = GRID_SIZE - 1; y >= 0; y--) {
                String cellText = cells[x][y].getText().toString();
                if (!cellText.isEmpty()) {
                    int value = Integer.parseInt(cellText);
                    if (index < GRID_SIZE - 1 && newCol[index + 1] == value && !merged[index + 1]) {
                        newCol[index + 1] *= 2;
                        merged[index + 1] = true;
                        moved = true;
                    } else {
                        if (newCol[index] != 0) moved = true;
                        newCol[index--] = value;
                    }
                }
            }

            for (int y = 0; y < GRID_SIZE; y++) {
                String newValue = newCol[y] == 0 ? "" : String.valueOf(newCol[y]);
                if (!cells[x][y].getText().toString().equals(newValue)) moved = true;
                cells[x][y].setText(newValue);
                updateCellColor(cells[x][y]);
            }
        }
        return moved;
    }
}
