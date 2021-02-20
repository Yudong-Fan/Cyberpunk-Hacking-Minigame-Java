package game;

import entity.*;
import graphics.*;
import graphics.MenuBar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.List;
import java.util.Stack;


//Game UI.
// DO NOT modify this file!

class Game extends JPanel {

    private final StatusHandler statusHandler;
    private static final int TIMER_PERIOD = 1000;
    private int timeFlag = 0;
    private final JPanel backgroundPanel;
    private final ActionListener exitGame;
    private final Stack<Status> statuses = new Stack<>();
    private Status currentStatus;

    //init GamePanel
    public Game(Status firstStatus,ActionListener exitGame ) {

        this.statusHandler = new StatusHandler(firstStatus);
        this.statuses.push(firstStatus);
        currentStatus = firstStatus;
        this.exitGame = exitGame;

        this.setBackground(Color.BLACK);

        Image image = new ImageIcon("src/main/java/image/gamePanel2.jpg").getImage();
        backgroundPanel = new Background(image);

        add(backgroundPanel, new FlowLayout(FlowLayout.CENTER, 0, 0));

        drawGamingPanel();
    }

    //refresh the Panel
    private void updatePanel() {
        backgroundPanel.removeAll();
        backgroundPanel.repaint();

        if (statusHandler.isGameOver())
            drawGameOverPanel();
        else
            drawGamingPanel();

        backgroundPanel.revalidate();
    }

    private void drawGamingPanel() {
        backgroundPanel.add(drawTimerPanel());
        backgroundPanel.add(drawBuffer());
        backgroundPanel.add(drawCodeMatrix());
        backgroundPanel.add(drawDaemons());
        backgroundPanel.add(drawScorePanel());
        backgroundPanel.add(drawMenuBar());
    }

    private void drawGameOverPanel() {
        backgroundPanel.add(drawTimerPanel());
        backgroundPanel.add(drawBuffer());
        backgroundPanel.add(drawDaemons());
        backgroundPanel.add(drawScorePanel());
        backgroundPanel.add(drawMenuBar());
        backgroundPanel.add(drawTimeOutPanel());
    }

    private JPanel drawCodeMatrix() {
        CodeMatrix codeSource = currentStatus.getCodeMatrix();
        int matrixSpan = codeSource.getMatrixSpan();

        CodeMatrixPanel panel = new CodeMatrixPanel(matrixSpan);

        for (int row = 0; row < matrixSpan; row++) {
            for (int col = 0; col < matrixSpan; col++) {
                JButton matrixCell = drawMatrixCell(codeSource.getMatrixCell(row, col));
                panel.add(matrixCell);
            }
        }
        return panel;
    }

    private JButton drawMatrixCell(MatrixCell tile) {
        JButton matrixCell = new MatrixCellButton(tile.getCode());
        if (tile.isSelected())
            matrixCell.setForeground(new Color(70, 44, 84));

        if (tile.isAvailable()) {
            matrixCell.setBackground(new Color(41, 44, 57));

            if (!tile.isSelected())
                addClickEvent(matrixCell,tile.getCoordinate());
        }
        return matrixCell;
    }

    private JPanel drawBuffer() {
        JPanel panel = new BufferPanel();
        for (int i = 0; i < currentStatus.getBuffer().getBufferSize(); i++) {
            JLabel label = new BufferCell(currentStatus.getBuffer().getBufferCode(i));
            panel.add(label);
        }
        return panel;
    }

    private JPanel drawDaemons() {
        JPanel panel = new DaemonsPanel();
        List<Daemon> daemons = currentStatus.getDaemons();

        for (Daemon daemon : daemons) {
            JPanel daemonPanel = new DaemonLabel();
            panel.add(daemonPanel);

            if (daemon.isSucceeded())
                daemonPanel.add(new SucceededLabel());
            if (daemon.isFailed())
                daemonPanel.add(new FailedLabel());

            if (!daemon.isFailed() && !daemon.isSucceeded()) {
                for (int j = 0; j < daemon.getDaemonCells().size(); j++) {
                    JLabel label = drawDaemonCell(daemon.getDaemonCells().get(j));
                    daemonPanel.add(label);
                }
            }
        }

        return panel;
    }

    private JLabel drawDaemonCell(DaemonCell seqCode) {
        JLabel label = new DaemonCellLabel(seqCode.getCode());
        if (!seqCode.isAdded())
            label.setForeground(Color.WHITE);

        if (seqCode.isSelected())
            label.setBorder(BorderFactory.createLineBorder(new Color(250, 247, 10)));

        return label;
    }

    private JPanel drawScorePanel() {
        return new JPanel(); //TODO
    }

    private JPanel drawTimeOutPanel() {
        return new GameOver();
    }

    private JPanel drawTimerPanel() {
        JPanel panel = new TimeLimit();
        JLabel countDownLabel = new CountDownLabel(currentStatus.getTimeLimit() + "");
        panel.add(countDownLabel);

        return panel;
    }

    private JPanel drawMenuBar() {
        JPanel menuBar = new MenuBar();
        JButton exitButton = new ExitButton();
        exitButton.addActionListener(exitGame);
        menuBar.add(exitButton);

        return menuBar;
    }

    private void addClickEvent(JButton matrixCell, Coordinate coordinate) {

        matrixCell.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (timeFlag == 0) {
                    timeFlag = 1;
                    startTime();
                }

                currentStatus.getCodeMatrix().setCellPicked(coordinate);
                statuses.push(statusHandler.updateStatus());
                currentStatus = statuses.peek();
                updatePanel();
            }

            @Override
            public void mousePressed(MouseEvent e) {//no such request
            }

            @Override
            public void mouseReleased(MouseEvent e) {//no such request
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                matrixCell.setForeground(Color.CYAN);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                matrixCell.setForeground(new Color(222, 255, 85));
            }
        });
    }

    public void startTime() {
        new Timer(TIMER_PERIOD, e -> {
            if (currentStatus.getTimeLimit() > 0) {
                currentStatus.addTimeLimit(-1);
                updatePanel();

            } else {
                ((Timer) e.getSource()).stop();
                statusHandler.setGameOver();
                statusHandler.markUnrewardedDaemonsFailed();
                updatePanel();
            }
        }).start();
    }
}
