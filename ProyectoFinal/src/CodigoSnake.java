/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Cambar
 */
import java.awt.*;
import java.awt.event.*;
import static java.lang.String.format;
import java.util.*;
import java.util.List;
import javax.swing.*;
 
public class CodigoSnake extends JPanel implements Runnable {
   enum Dir {
      up(0, -1), right(1, 0), down(0, 1), left(-1, 0);
 
      Dir(int x, int y) {
         this.x = x; this.y = y;
      }
 
      final int x, y;
   }
    
 
   static final Random rand = new Random();
   static final int Pared = -1;
   static final int Pared2 = -1;
   static final int energia_maxima = 1500;
 
   volatile boolean gameOver = true;
 
   Thread gameThread;
   int puntos, puntuacion_maxima;
   int nFilas = 44;
   int nColumnas = 64;
   Dir dir;
   int energia;
   int modo = 2;
 
   int[][] Mundo;
   List<Point> snake, treats, frutaTrampa;
   Font smallFont;
 
   public CodigoSnake() {
      setPreferredSize(new Dimension(640, 440));
      setBackground(Color.WHITE);
      setFont(new Font("TimesNewRoman", Font.BOLD, 48));
      setFocusable(true);
 
      smallFont = getFont().deriveFont(Font.BOLD, 18);
      initGrid();
 
      addMouseListener(
         new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
               if (gameOver) {
                  startNewGame();
                  repaint();
               }
            }
         });
 
      addKeyListener(
         new KeyAdapter() {
 
            @Override
            public void keyPressed(KeyEvent e) {
 
               switch (e.getKeyCode()) {
 
                  case KeyEvent.VK_UP:
                     if (dir != Dir.down)
                        dir = Dir.up;
                     break;
 
                  case KeyEvent.VK_LEFT:
                     if (dir != Dir.right)
                        dir = Dir.left;
                     break;
 
                  case KeyEvent.VK_RIGHT:
                     if (dir != Dir.left)
                        dir = Dir.right;
                     break;
 
                  case KeyEvent.VK_DOWN:
                     if (dir != Dir.up)
                        dir = Dir.down;
                     break;
               }
               repaint();
            }
         });
   }
 
   void startNewGame() {
      gameOver = false;
 
      stop();
      initGrid();
      treats = new LinkedList<>();
      frutaTrampa = new LinkedList<>();
 
      dir = Dir.left;
      energia = energia_maxima;
 
      if (puntos > puntuacion_maxima)
         puntuacion_maxima = puntos;
      puntos = 0;
 
      snake = new ArrayList<>();
      for (int x = 0; x < 7; x++)
         snake.add(new Point(nColumnas / 2 + x, nFilas / 2));
 
      do
         agregarFruta();
      while(treats.isEmpty());
 
      (gameThread = new Thread(this)).start();
   }
 
   void stop() {
      if (gameThread != null) {
         Thread tmp = gameThread;
         gameThread = null;
         tmp.interrupt();
      }
   }
 
   void initGrid() {
      Mundo = new int[nFilas][nColumnas];
      for (int r = 0; r < nFilas; r++) {
         for (int c = 0; c < nColumnas; c++) {
            if (c == 0 || c == nColumnas - 1 || r == 0 || r == nFilas - 1)
               Mundo[r][c] = Pared;               
         }
      }
   }
 
   @Override
   public void run() {
       switch (modo){
           case 1:
 
      while (Thread.currentThread() == gameThread) {
 
         try {
            Thread.sleep(Math.max(75 - puntos, 25));
         } catch (InterruptedException e) {
            return;
         }
 
         if (energiaUsada() || golpeaPared() || golpePropio()) {
            gameOver();
         } else {
            if (comeFruta()) {
               puntos++;
               energia = energia_maxima;
               growSnake();
            } else if(comeFrutaTrampa()) {
                puntos = puntos/2;
                energia = energia_maxima;
                shrinkSnake();
            }
            moveSnake();
            agregarFruta();
         }
         repaint();
      }
      break;
           case 2:
               while (Thread.currentThread() == gameThread) {
 
         try {
            Thread.sleep(Math.max(75 - puntos, 25));
         } catch (InterruptedException e) {
            return;
         }
 
         if (energiaUsada() || toroidal() || golpePropio()) {
            gameOver();
         } else {
            if (comeFruta()) {
               puntos++;
               energia = energia_maxima;
               growSnake();
            }else if(comeFrutaTrampa()) {
                puntos = puntos/2;
                energia = energia_maxima;
                shrinkSnake();
            }
            moveSnake();
            agregarFruta();
         }
         repaint();
      }
      break;
   }
   }
 
   boolean energiaUsada() {
      energia -= 10;
      return energia <= 0;
   }
 
   boolean golpeaPared() {
      Point head = snake.get(0);
      int nextCol = head.x + dir.x;
      int nextRow = head.y + dir.y;
      return Mundo[nextRow][nextCol] == Pared;
   }
   
   boolean toroidal(){
       Point head = snake.get(0);
       int nextCol = head.x + dir.x;
       int nextRow = head.y + dir.y;
       if(nextCol == 0){
           head.x = 63;        
       } else if(nextCol == 63){
                   head.x = 0;
            }  
       if(nextRow == 0){
           head.y = 44;
       }else if(nextRow == 44){
           head.y = 0;
               }
       return false;
   }
   
 
   boolean golpePropio() {
      Point head = snake.get(0);
      int nextCol = head.x + dir.x;
      int nextRow = head.y + dir.y;
      for (Point p : snake)
         if (p.x == nextCol && p.y == nextRow)
            return true;
      return false;
   }
 
   boolean comeFruta() {
      Point head = snake.get(0);
      int nextCol = head.x + dir.x;
      int nextRow = head.y + dir.y;
      for (Point p : treats)
         if (p.x == nextCol && p.y == nextRow) {
            return treats.remove(p);
         }
      return false;
   }
   boolean comeFrutaTrampa() {
      Point head = snake.get(0);
      int nextCol = head.x + dir.x;
      int nextRow = head.y + dir.y;
      for (Point p : frutaTrampa)
         if (p.x == nextCol && p.y == nextRow) {
            return treats.remove(p);
         }
      return false;
   }
 
   void gameOver() {
      gameOver = true;
      stop();
   }
 
   void moveSnake() {
      for (int i = snake.size() - 1; i > 0; i--) {
         Point p1 = snake.get(i - 1);
         Point p2 = snake.get(i);
         p2.x = p1.x;
         p2.y = p1.y;
      }
      Point head = snake.get(0);
      head.x += dir.x;
      head.y += dir.y;
   }
 
   void growSnake() {
      Point tail = snake.get(snake.size() - 1);
      int x = tail.x + dir.x;
      int y = tail.y + dir.y;
      snake.add(new Point(x, y));
   }
   
   void shrinkSnake() {
      Point tail = snake.get(snake.size() - 1);
      int x = tail.x + dir.x;
      int y = tail.y + dir.y;
      snake.remove(5);
   }
 
   void agregarFruta() {
      if (treats.size() < 2) {
 
         if (rand.nextInt(1) == 0) { // 1 in 10
 
            if (rand.nextInt(4) != 0) {  // 3 in 4
               int x, y;
               while (true) {
 
                  x = rand.nextInt(nColumnas);
                  y = rand.nextInt(nFilas);
                  if (Mundo[y][x] != 0)
                     continue;
 
                  Point p = new Point(x, y);
                  if (snake.contains(p) || treats.contains(p))//{
                     continue;
                  
                  treats.add(p);
                  break;
               }
            } else if (treats.size() > 1)
               treats.remove(0);
         }
      }
   }
   void agregarFrutaTrampa() {
      if (frutaTrampa.size() < 1) {
 
         if (rand.nextInt(10) == 0) { // 1 in 10
 
            if (rand.nextInt(4) != 0) {  // 3 in 4
               int x, y;
               while (true) {
 
                  x = rand.nextInt(nColumnas);
                  y = rand.nextInt(nFilas);
                  if (Mundo[y][x] != 0)
                     continue;
 
                  Point p = new Point(x, y);
                  if (snake.contains(p) || frutaTrampa.contains(p))
                     continue;
                  
                  frutaTrampa.add(p);
                  break;
               }
            } else if (frutaTrampa.size() > 1)
               frutaTrampa.remove(0);
         }
      }
   }
 
   void drawGrid(Graphics2D g) {
      g.setColor(Color.black);
      for (int r = 0; r < nFilas; r++) {
         for (int c = 0; c < nColumnas; c++) {
            if (Mundo[r][c] == Pared)
               g.fillRect(c * 10, r * 10, 10, 10);
         }
      }
   }
 
   void drawSnake(Graphics2D g) {
      g.setColor(Color.BLACK);
      for (Point p : snake)
         g.fillRect(p.x * 10, p.y * 10, 10, 10);
 
      g.setColor(energia < 500 ? Color.red : Color.cyan);
      Point head = snake.get(0);
      g.fillRect(head.x * 10, head.y * 10, 10, 10);
   }
 
   void drawTreats(Graphics2D g) {
      g.setColor(Color.RED);
      for (Point p : treats)
         g.fillRect(p.x * 10, p.y * 10, 10, 10);
      g.setColor(Color.BLACK);
      for (Point p : frutaTrampa)
         g.fillRect(p.x * 10, p.y * 10, 10, 10);
   }
 
   /*void drawFrutaTrampa(Graphics2D g) {
      g.setColor(Color.BLACK);
      for (Point t : frutaTrampa)
         g.fillRect(t.x * 10, t.y * 10, 10, 10);
   }*/
   
   void drawStartScreen(Graphics2D g) {
      g.setColor(Color.black);
      g.setFont(getFont());
      g.drawString("SNAKE", 240, 190);
      g.setColor(Color.GREEN);
      g.setFont(smallFont);
      g.drawString("(Click Para Jugar)", 250, 220);
      g.setColor(Color.BLUE);
      g.setFont(smallFont);
      g.drawString("selecionar modo de juego", 210, 380);
      g.setColor(Color.GREEN);
      g.setFont(smallFont);
      g.drawString("clasico", 270, 400);
      //g.setColor(Color.Green);
      g.setFont(smallFont);
      g.drawString("Toroidal", 270, 420);
      
   }
 
   void drawScore(Graphics2D g) {
      int h = getHeight();
      g.setFont(smallFont);
      g.setColor(getForeground());
      String s = format("Maxima Puntuacion: %d    Puntos: %d", puntuacion_maxima, puntos);
      g.drawString(s, 30, h - 30);
      g.drawString(format("Energia: %d", energia), getWidth() - 150, h - 30);
   }
 
   @Override
   public void paintComponent(Graphics gg) {
      super.paintComponent(gg);
      Graphics2D g = (Graphics2D) gg;
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
             RenderingHints.VALUE_ANTIALIAS_ON);
 
      drawGrid(g);
 
      if (gameOver) {
         drawStartScreen(g);
      } else {
         drawSnake(g);
         drawTreats(g);
         //drawFrutaTrampa(g);
         drawScore(g);
      }
   }
   
   
 
   public static void main(String[] args) {
      SwingUtilities.invokeLater(
         () -> {
            JFrame mainFrame = new JFrame();
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setTitle("SNAKE");
            mainFrame.setResizable(true);
            mainFrame.add(new CodigoSnake(), BorderLayout.CENTER);
            mainFrame.pack();
            mainFrame.setLocationRelativeTo(null);
            mainFrame.setVisible(true);
         });
   }
}