import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class Dama extends JPanel {

    public static void main(String[] args) {
        JFrame window = new JFrame("Dama");
        Dama content = new Dama();
        window.setContentPane(content);
        window.pack();
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        window.setLocation( (screensize.width - window.getWidth())/2,
                (screensize.height - window.getHeight())/2 );
        window.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        window.setResizable(false);
        window.setVisible(true);
    }

    private JButton hraButton;
    private JButton vzdatButton;
    private JLabel zprava;

    public Dama() {

        setLayout(null);
        setPreferredSize( new Dimension(350,350) );
        setBackground(new Color(0,50,250));
        pole pole = new pole();
        add(pole);
        add(hraButton);
        add(vzdatButton);
        add(zprava);

        /* Set the position and size of each component by calling
         its setBounds() method. */

        pole.setBounds(20,20,164,164); // Note:  size MUST be 164-by-164 !
        hraButton.setBounds(210, 60, 120, 30);
        vzdatButton.setBounds(210, 120, 120, 30);
        zprava.setBounds(0, 200, 350, 30);

    }

    private static class DamaPohyb {
        int fromRow, fromCol;
        int toRow, toCol;
        DamaPohyb(int r1, int c1, int r2, int c2) {
            fromRow = r1;
            fromCol = c1;
            toRow = r2;
            toCol = c2;
        }
        boolean isJump() {
            return (fromRow - toRow == 2 || fromRow - toRow == -2);
        }
    }

    private class pole extends JPanel implements ActionListener, MouseListener {

        CheckersData board;
        boolean gameInProgress;
        int currentPlayer;
        int selectedRow, selectedCol;
        DamaPohyb[] legalMoves;

        pole() {
            setBackground(Color.BLACK);
            addMouseListener(this);
            vzdatButton = new JButton("Vzdát hru");
            vzdatButton.addActionListener(this);
            hraButton = new JButton("Nová hra");
            hraButton.addActionListener(this);
            zprava = new JLabel("",JLabel.CENTER);
            zprava.setFont(new  Font("Serif", Font.BOLD, 14));
            zprava.setForeground(Color.green);
            board = new CheckersData();
            doNewGame();
        }

        public void actionPerformed(ActionEvent evt) {
            Object src = evt.getSource();
            if (src == hraButton)
                doNewGame();
            else if (src == vzdatButton)
                doResign();
        }

        void doNewGame() {
            if (gameInProgress == true) {
                zprava.setText("Dokonci prvne hru!");
                return;
            }
            board.setUpGame();
            currentPlayer = CheckersData.RED;
            legalMoves = board.getLegalMoves(CheckersData.RED);
            selectedRow = -1;
            zprava.setText("Cerveny na tahu");
            gameInProgress = true;
            hraButton.setEnabled(false);
            vzdatButton.setEnabled(true);
            repaint();
        }

        void doResign() {
            if (gameInProgress == false) {
                zprava.setText("Hra nezacala");
                return;
            }
            if (currentPlayer == CheckersData.RED)
                gameOver("Cerveny vzdava hru. Cerny vyhral.");
            else
                gameOver("Cerny vzdava hru.  Cerveny vyhral.");
        }

        void gameOver(String str) {
            zprava.setText(str);
            hraButton.setEnabled(true);
            vzdatButton.setEnabled(false);
            gameInProgress = false;
        }

        void doClickSquare(int row, int col) {

            for (int i = 0; i < legalMoves.length; i++)
                if (legalMoves[i].fromRow == row && legalMoves[i].fromCol == col) {
                    selectedRow = row;
                    selectedCol = col;
                    if (currentPlayer == CheckersData.RED)
                        zprava.setText("Cerveny na tahu");
                    else
                        zprava.setText("Cerny na tahu.");
                    repaint();
                    return;
                }

            if (selectedRow < 0) {
                zprava.setText("Pro pohyb kliknete na figurku.");
                return;
            }

            for (int i = 0; i < legalMoves.length; i++)
                if (legalMoves[i].fromRow == selectedRow && legalMoves[i].fromCol == selectedCol
                        && legalMoves[i].toRow == row && legalMoves[i].toCol == col) {
                    doMakeMove(legalMoves[i]);
                    return;
                }

            zprava.setText("Klikněte na policko, na které se chcete přesunout.");

        }

        void doMakeMove(DamaPohyb move) {

            board.makeMove(move);

            if (move.isJump()) {
                legalMoves = board.getLegalJumpsFrom(currentPlayer,move.toRow,move.toCol);
                if (legalMoves != null) {
                    if (currentPlayer == CheckersData.RED)
                        zprava.setText("Cerveny musis skocit s figurkou na policko");
                    else
                        zprava.setText("Cerny musis skocit s figurkou na policko");
                    selectedRow = move.toRow;
                    selectedCol = move.toCol;
                    repaint();
                    return;
                }
            }

            if (currentPlayer == CheckersData.RED) {
                currentPlayer = CheckersData.BLACK;
                legalMoves = board.getLegalMoves(currentPlayer);
                if (legalMoves == null)
                    gameOver("Cernemu došel tah.  Cerveny vyhrava.");
                else if (legalMoves[0].isJump())
                    zprava.setText("Cerny musis skocit s figurkou na policko");
                else
                    zprava.setText("Cerny zacina tah");
            }
            else {
                currentPlayer = CheckersData.RED;
                legalMoves = board.getLegalMoves(currentPlayer);
                if (legalMoves == null)
                    gameOver("Cervenemu došel tah.  Cerveny vyhrava.");
                else if (legalMoves[0].isJump())
                    zprava.setText("Cerveny musis skocit s figurkou na policko");
                else
                    zprava.setText("Cerveny zacina tah");
            }

            selectedRow = -1;

            if (legalMoves != null) {
                boolean sameStartSquare = true;
                for (int i = 1; i < legalMoves.length; i++)
                    if (legalMoves[i].fromRow != legalMoves[0].fromRow
                            || legalMoves[i].fromCol != legalMoves[0].fromCol) {
                        sameStartSquare = false;
                        break;
                    }
                if (sameStartSquare) {
                    selectedRow = legalMoves[0].fromRow;
                    selectedCol = legalMoves[0].fromCol;
                }
            }

            repaint();

        }

        public void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setColor(Color.black);
            g.drawRect(0,0,getSize().width-1,getSize().height-1);
            g.drawRect(1,1,getSize().width-3,getSize().height-3);

            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    if ( row % 2 == col % 2 )
                        g.setColor(Color.LIGHT_GRAY);
                    else
                        g.setColor(Color.GRAY);
                    g.fillRect(2 + col*20, 2 + row*20, 20, 20);
                    switch (board.pieceAt(row,col)) {
                        case CheckersData.RED:
                            g.setColor(Color.RED);
                            g.fillOval(4 + col*20, 4 + row*20, 15, 15);
                            break;
                        case CheckersData.BLACK:
                            g.setColor(Color.BLACK);
                            g.fillOval(4 + col*20, 4 + row*20, 15, 15);
                            break;
                        case CheckersData.RED_KING:
                            g.setColor(Color.RED);
                            g.fillOval(4 + col*20, 4 + row*20, 15, 15);
                            g.setColor(Color.WHITE);
                            g.drawString("K", 7 + col*20, 16 + row*20);
                            break;
                        case CheckersData.BLACK_KING:
                            g.setColor(Color.BLACK);
                            g.fillOval(4 + col*20, 4 + row*20, 15, 15);
                            g.setColor(Color.WHITE);
                            g.drawString("K", 7 + col*20, 16 + row*20);
                            break;
                    }
                }
            }

            if (gameInProgress) {
                g.setColor(Color.cyan);
                for (int i = 0; i < legalMoves.length; i++) {
                    g.drawRect(2 + legalMoves[i].fromCol*20, 2 + legalMoves[i].fromRow*20, 19, 19);
                    g.drawRect(3 + legalMoves[i].fromCol*20, 3 + legalMoves[i].fromRow*20, 17, 17);
                }
                if (selectedRow >= 0) {
                    g.setColor(Color.white);
                    g.drawRect(2 + selectedCol*20, 2 + selectedRow*20, 19, 19);
                    g.drawRect(3 + selectedCol*20, 3 + selectedRow*20, 17, 17);
                    g.setColor(Color.green);
                    for (int i = 0; i < legalMoves.length; i++) {
                        if (legalMoves[i].fromCol == selectedCol && legalMoves[i].fromRow == selectedRow) {
                            g.drawRect(2 + legalMoves[i].toCol*20, 2 + legalMoves[i].toRow*20, 19, 19);
                            g.drawRect(3 + legalMoves[i].toCol*20, 3 + legalMoves[i].toRow*20, 17, 17);
                        }
                    }
                }
            }

        }

        public void mousePressed(MouseEvent evt) {
            if (gameInProgress == false)
                zprava.setText("Kliknout \"Nova hra\" zacit novou hru.");
            else {
                int col = (evt.getX() - 2) / 20;
                int row = (evt.getY() - 2) / 20;
                if (col >= 0 && col < 8 && row >= 0 && row < 8)
                    doClickSquare(row,col);
            }
        }

        public void mouseReleased(MouseEvent evt) { }
        public void mouseClicked(MouseEvent evt) { }
        public void mouseEntered(MouseEvent evt) { }
        public void mouseExited(MouseEvent evt) { }


    }

    private static class CheckersData {
        static final int
                EMPTY = 0,
                RED = 1,
                RED_KING = 2,
                BLACK = 3,
                BLACK_KING = 4;

        int[][] board;

        CheckersData() {
            board = new int[8][8];
            setUpGame();
        }

        void setUpGame() {
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    if ( row % 2 == col % 2 ) {
                        if (row < 3)
                            board[row][col] = BLACK;
                        else if (row > 4)
                            board[row][col] = RED;
                        else
                            board[row][col] = EMPTY;
                    }
                    else {
                        board[row][col] = EMPTY;
                    }
                }
            }
        }

        int pieceAt(int row, int col) {
            return board[row][col];
        }

        void makeMove(DamaPohyb move) {
            makeMove(move.fromRow, move.fromCol, move.toRow, move.toCol);
        }

        void makeMove(int fromRow, int fromCol, int toRow, int toCol) {
            board[toRow][toCol] = board[fromRow][fromCol];
            board[fromRow][fromCol] = EMPTY;
            if (fromRow - toRow == 2 || fromRow - toRow == -2) {
                int jumpRow = (fromRow + toRow) / 2;
                int jumpCol = (fromCol + toCol) / 2;
                board[jumpRow][jumpCol] = EMPTY;
            }
            if (toRow == 0 && board[toRow][toCol] == RED)
                board[toRow][toCol] = RED_KING;
            if (toRow == 7 && board[toRow][toCol] == BLACK)
                board[toRow][toCol] = BLACK_KING;
        }

        DamaPohyb[] getLegalMoves(int player) {

            if (player != RED && player != BLACK)
                return null;

            int playerKing;
            if (player == RED)
                playerKing = RED_KING;
            else
                playerKing = BLACK_KING;

            ArrayList<DamaPohyb> moves = new ArrayList<DamaPohyb>();
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    if (board[row][col] == player || board[row][col] == playerKing) {
                        if (canJump(player, row, col, row+1, col+1, row+2, col+2))
                            moves.add(new DamaPohyb(row, col, row+2, col+2));
                        if (canJump(player, row, col, row-1, col+1, row-2, col+2))
                            moves.add(new DamaPohyb(row, col, row-2, col+2));
                        if (canJump(player, row, col, row+1, col-1, row+2, col-2))
                            moves.add(new DamaPohyb(row, col, row+2, col-2));
                        if (canJump(player, row, col, row-1, col-1, row-2, col-2))
                            moves.add(new DamaPohyb(row, col, row-2, col-2));
                    }
                }
            }

            if (moves.size() == 0) {
                for (int row = 0; row < 8; row++) {
                    for (int col = 0; col < 8; col++) {
                        if (board[row][col] == player || board[row][col] == playerKing) {
                            if (canMove(player,row,col,row+1,col+1))
                                moves.add(new DamaPohyb(row,col,row+1,col+1));
                            if (canMove(player,row,col,row-1,col+1))
                                moves.add(new DamaPohyb(row,col,row-1,col+1));
                            if (canMove(player,row,col,row+1,col-1))
                                moves.add(new DamaPohyb(row,col,row+1,col-1));
                            if (canMove(player,row,col,row-1,col-1))
                                moves.add(new DamaPohyb(row,col,row-1,col-1));
                        }
                    }
                }
            }

            if (moves.size() == 0)
                return null;
            else {
                DamaPohyb[] moveArray = new DamaPohyb[moves.size()];
                for (int i = 0; i < moves.size(); i++)
                    moveArray[i] = moves.get(i);
                return moveArray;
            }

        }

        DamaPohyb[] getLegalJumpsFrom(int player, int row, int col) {
            if (player != RED && player != BLACK)
                return null;
            int playerKing;
            if (player == RED)
                playerKing = RED_KING;
            else
                playerKing = BLACK_KING;
            ArrayList<DamaPohyb> moves = new ArrayList<DamaPohyb>();
            if (board[row][col] == player || board[row][col] == playerKing) {
                if (canJump(player, row, col, row+1, col+1, row+2, col+2))
                    moves.add(new DamaPohyb(row, col, row+2, col+2));
                if (canJump(player, row, col, row-1, col+1, row-2, col+2))
                    moves.add(new DamaPohyb(row, col, row-2, col+2));
                if (canJump(player, row, col, row+1, col-1, row+2, col-2))
                    moves.add(new DamaPohyb(row, col, row+2, col-2));
                if (canJump(player, row, col, row-1, col-1, row-2, col-2))
                    moves.add(new DamaPohyb(row, col, row-2, col-2));
            }
            if (moves.size() == 0)
                return null;
            else {
                DamaPohyb[] moveArray = new DamaPohyb[moves.size()];
                for (int i = 0; i < moves.size(); i++)
                    moveArray[i] = moves.get(i);
                return moveArray;
            }
        }

        private boolean canJump(int player, int r1, int c1, int r2, int c2, int r3, int c3) {
            if (r3 < 0 || r3 >= 8 || c3 < 0 || c3 >= 8)
                return false;
            if (board[r3][c3] != EMPTY)
                return false;
            if (player == RED) {
                if (board[r1][c1] == RED && r3 > r1)
                    return false;
                if (board[r2][c2] != BLACK && board[r2][c2] != BLACK_KING)
                    return false;
                return true;
            }
            else {
                if (board[r1][c1] == BLACK && r3 < r1)
                    return false;
                if (board[r2][c2] != RED && board[r2][c2] != RED_KING)
                    return false;
                return true;
            }
        }

        private boolean canMove(int player, int r1, int c1, int r2, int c2) {

            if (r2 < 0 || r2 >= 8 || c2 < 0 || c2 >= 8)
                return false;

            if (board[r2][c2] != EMPTY)
                return false;

            if (player == RED) {
                if (board[r1][c1] == RED && r2 > r1)
                    return false;
                return true;
            }
            else {
                if (board[r1][c1] == BLACK && r2 < r1)
                    return false;
                return true;
            }

        }
    }
}

