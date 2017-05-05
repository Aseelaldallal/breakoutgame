import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.math.*;
import java.util.*;
import java.util.Timer;
import java.awt.geom.*;


/*********************************************/
/****************** BREAKOUT *****************/
/*********************************************/

public class Breakout extends JPanel {
	
	/***************** Constants *****************/
	
	// VIEW CONSTANTS
	public static final int INIT_VIEW_WIDTH = 800; // Width of View when game first starts
	public static final int INIT_VIEW_HEIGHT = 800; // Height of View when game first starts
	public static final int MIN_WINDOW_WIDTH = 350;
	public static final int MIN_WINDOW_HEIGHT = 400;
	// PADDLE CONSTANTS
	public static final double PADDLE_H_FACTOR = 0.02; // Size of Paddle relative to View
	public static final double PADDLE_W_FACTOR = 0.16;
	public static final double PADDLE_DFB = 0.07; // Distance from bottom of view to paddle, in percent
	public static final Color PADDLE_COLOR = Color.CYAN;
	public static final int PADDLE_MV_AMT =11; // Amount paddle moves with each key stroke
	// BOARD CONSTANTS
	public static final Color BOARD_COLOR = Color.BLACK;
	// BALL CONSTANTS
	public static final double BALL_RADIUS_FACTOR = 0.02; // Size of Ball relative to View
	public static final int BALL_DF_SPEED = 400; // Ball Speed 
	public static final int INIT_ANGLE = 90;
	public static final Color BALL_COLOR = Color.GREEN;
	// BRICK CONSTANTS
	public static final double BRICK_DIS_FROM_SIDE_MARGIN_FACTOR = 0.1; // Margin between view and leftmost/rightmost brick
    public static final double BRICK_DIS_FROM_TOP_MARGIN_FACTOR = 0.1; // Margin between view top and uppermost bricks
    public static final double BRICK_WIDTH_FACTOR = 0.1;
    public static final double BRICK_HEIGHT_FACTOR = 0.05;
    public static final int NUM_BRICKS_COL = 8;
    public static final int NUM_BRICKS_ROW = 5;
    public static final Color BRICK_L1_COLOR = Color.YELLOW;
    public static final Color BRICK_L2_COLOR = Color.GREEN;
    public static final Color BRICK_L3_COLOR = Color.RED;
    public static final int NUM_BRICK_LEVELS = 3;
    // SPLASH SCREEN CONSTANTS
    public static final Color SPLASH_BACKGROUND_C = Color.BLACK;
    public static final Color SPLASH_NAME_C = Color.YELLOW;
    public static final Color SPLASH_INST_C = Color.PINK;
    public static final String FONT_NAME = "Comic Sans MS";
    public static final double FONT_NM_SZ_FACTOR = 0.1; // Size of Name font relative to View Width
    public static final double FONT_ID_SZ_FACTOR = 0.08; // Size of student ID relative to View Width
    public static final double FONT_INST_SZ_FACTOR = 0.03; // Size of instructions font relative to View Width

	/************* Instance Variables ************/

	
	private JFrame frame;
	private Model myModel;
	private View myView;
	private boolean gameStarted = false; 
	private boolean gameOver = false;
	private boolean playerWon = false;
	private boolean playerStarted = false; // Used to see if the player started playing, and so move the ball
	private double speed;
	private double updateRate;
	private double fps; 
	
	/**************** Constructor ****************/

	public Breakout(String speed, String fps) {
	
		this.fps = Double.parseDouble(fps);
		this.updateRate = 1000/this.fps;
		this.speed = Double.parseDouble(speed)/this.fps;
		System.out.println("Speed: " + this.speed);
	
		// Create Window for Game
		this.frame = new JFrame("Breakout");
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.frame.setVisible(true); 
		this.frame.setContentPane(this); 
		frame.setMinimumSize(new Dimension(MIN_WINDOW_WIDTH, MIN_WINDOW_HEIGHT));
		frame.setMaximumSize(new Dimension(1000,1000));
		
        // Initialize View
		this.myView = new View(INIT_VIEW_WIDTH, INIT_VIEW_HEIGHT);
		this.setLayout(new BorderLayout());
	    this.add(myView, BorderLayout.CENTER);
	    this.frame.pack(); 

	    // Initialize the Model
	    this.myModel = new Model();  
	    myModel.runGame();

	}
	
	
	
	/******************* Main ********************/
	
	/* Pre:  args is not null and contains two integers. The first integer is the ball speed. The
	 *       second is the frame rate.
	 * Post: Starts the breakout game (NEED TO ADD PARAM ROLES)
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
	         public void run() {
		     		Breakout game = new Breakout(args[0], args[1]);
	         }
	      });
	}
	
	/*********************************************/
	/******************** VIEW *******************/
	/*********************************************/
	
	// game window
	// draws everything based on the game state
	// receives notification from the model when something changes, and 
	// draws components based on the model.
	class View extends JPanel {
		
		/************* Instance Variables ************/
		
		private int vWidth;
		private int vHeight;
		private JButton startGameButton;
		
		/**************** Constructor ****************/
		
		/* Pre:  None.
		 * Post: Creates a view with width w and height h. If the game hasn't started yet, draws the splash screen.
		 * 		 If the game hasn't started, draws a button that allows the user to start the game. 
		 */
		public View(int w, int h) {
			this.vWidth = w;
			this.vHeight = h;
			this.setLayout(new BorderLayout());
			
			if (!gameStarted) {	
				startGameButton = new JButton("Start Playing");
				startGameButton.setFont(new Font(FONT_NAME, Font.BOLD, 20));
				this.add(startGameButton, BorderLayout.SOUTH);
				startGameButton.addActionListener( e-> gameStarted = true );
				
			}
			
		}
		
		/****************** Accessor *****************/
		
		public int getWidth()  		 { return this.vWidth;   }
		public int getHeight() 		 { return this.vHeight;  }
		public void setWidth(int w)  { this.vWidth = w; 	 }
		public void setHeight(int h) { this.vHeight = h; 	 }
		
		/*************** Public Methods **************/
		
		/* Pre:  g is not null
		 * Post: If the game hasn't started, draw the splash screen. If the game started, draw the game screen.
		 *       If game over, draw game over screen.
		 */
		public void paintComponent(Graphics g) {
			setDoubleBuffered(true);
			if(!gameStarted) {
				this.drawSplashScreen(g);
			} else
			if(gameStarted) {
				this.remove(startGameButton);
				if(gameStarted & !gameOver) {
					this.remove(startGameButton);
					if (!playerStarted) {
						this.askPressKey(g);
					}
					myModel.draw(g);
					if (!playerStarted) {
						this.askPressKey(g);
					}
				} else
				if(playerWon && gameOver) {
					this.drawWinScreen(g);
				} else
				if(!playerWon && gameOver) {
					this.drawGameOverScreen(g);
				}
			}
		}
		

		/* Pre:  None
		 * Post: Gets the preferred size of this view
		 */
		public Dimension getPreferredSize() {
	         return (new Dimension(vWidth, vHeight));
	    }
		
		/* Pre:  None
		 * Post: Returns a string representation of this
		 */
		public String toString() {
			String a = "View Height: " + this.vHeight;
			String b = "View Width: " + this.vWidth;
			return a + "\n" + b;
		}
		

		/************** Private Methods **************/
		
		/* Pre:  g is not null
		 * Post: Prints "Press any Key to Get Started" at the top of the screen
		 */
		private void askPressKey(Graphics g) {
			Graphics2D myG = (Graphics2D)g;
			myG.setColor(Color.WHITE);
			Font myFont = new Font(FONT_NAME, Font.BOLD, (int)(Math.min(this.vWidth * 0.04, this.vHeight * 0.04)));
			myG.setFont(myFont);
			FontMetrics met = myG.getFontMetrics();
			String inst = "Press any key to get started";
			myG.drawString(inst, (int)(this.vWidth/2 - met.stringWidth(inst)/2), (int)(0.06 * this.vHeight));
		}

		/* Pre:  g is not null
		 * Post: Draws the splash screen
		 */
		private void drawSplashScreen(Graphics g) {
			
			// Draw Background
			Graphics2D myG = (Graphics2D)g;
			myG.setColor(SPLASH_BACKGROUND_C);
			myG.fillRect(0, 0, this.vWidth, this.vHeight);
			
			// Write My Name
			myG.setColor(SPLASH_NAME_C);
			Font myFont = new Font(FONT_NAME, Font.BOLD, (int)(Math.min(this.vWidth * FONT_NM_SZ_FACTOR, this.vHeight * FONT_NM_SZ_FACTOR)));
			myG.setFont(myFont);
			String toWrite = "Aseel Al Dallal";
			FontMetrics nmMetrics = myG.getFontMetrics();
		    int x = (this.vWidth/2) - (nmMetrics.stringWidth(toWrite)/2) ;
		    int y = (int)( (0.1*this.vHeight) + nmMetrics.getHeight());
			myG.drawString(toWrite, x,y);
			
			// Write My Student ID
			toWrite= "20166957";
			myFont = new Font(FONT_NAME, Font.BOLD, (int)(Math.min(this.vWidth * FONT_ID_SZ_FACTOR, this.vHeight * FONT_ID_SZ_FACTOR)));
			myG.setFont(myFont);
			FontMetrics IDMetrics = myG.getFontMetrics();
			x = (this.vWidth/2) - (IDMetrics.stringWidth(toWrite)/2) ;
		    y = y + nmMetrics.getHeight();
			myG.drawString(toWrite, x,y);
			
			// Write Instructions
			toWrite= "Use the keyboard or mouse to move the paddle.";
			myFont = new Font(FONT_NAME, Font.BOLD, (int)(this.vWidth * FONT_INST_SZ_FACTOR));
			myG.setFont(myFont);
			FontMetrics InstMetrics = myG.getFontMetrics();
			x = (this.vWidth/2) - (InstMetrics.stringWidth(toWrite)/2) ;
		    y = y + IDMetrics.getHeight();
			myG.drawString(toWrite, x,y);
			toWrite = "Try to break the bricks. Once you break all bricks, you win!";
			x = (this.vWidth/2) - (InstMetrics.stringWidth(toWrite)/2) ;
		    y = y + InstMetrics.getHeight();
			myG.drawString(toWrite, x,y);
			toWrite = "Hint: Some bricks are harder to break than others!";
			x = (this.vWidth/2) - (InstMetrics.stringWidth(toWrite)/2) ;
		    y = y + InstMetrics.getHeight();
			myG.drawString(toWrite, x,y);
			toWrite= "If you drop the ball, your life will never be the same.";
			x = (this.vWidth/2) - (InstMetrics.stringWidth(toWrite)/2) ;
		    y = y + InstMetrics.getHeight();
			myG.drawString(toWrite, x,y);

		}
		
		/* Pre:  Pre: None. 
		 * Post: Draws a you won screen with your score
		 */
		private void drawWinScreen(Graphics g) {
			
			Graphics2D myG = (Graphics2D)g;
			g.setColor(SPLASH_BACKGROUND_C);
			myG.fillRect(0, 0, this.vWidth, this.vHeight);
			
			myG.setColor(SPLASH_NAME_C);
			Font myFont = new Font(FONT_NAME, Font.BOLD, (int)(Math.min(this.vWidth * FONT_NM_SZ_FACTOR, this.vHeight * FONT_NM_SZ_FACTOR)));
			myG.setFont(myFont);
			String toWrite = "You Won!";
			FontMetrics nmMetrics = myG.getFontMetrics();
		    int x = (this.vWidth/2) - (nmMetrics.stringWidth(toWrite)/2) ;
		    int y = (int)( this.vHeight/2 - 2*nmMetrics.getHeight());
			myG.drawString(toWrite, x,y);
			
			toWrite = "Score: " + myModel.score;
		    x = (this.vWidth/2) - (nmMetrics.stringWidth(toWrite)/2) ;
		    y = (int)( this.vHeight/2 - nmMetrics.getHeight());
			myG.drawString(toWrite, x,y);
			
			myFont = new Font(FONT_NAME, Font.BOLD, (int)(this.vWidth * FONT_INST_SZ_FACTOR));
			myG.setFont(myFont);
			nmMetrics = myG.getFontMetrics();
			toWrite = "You deserve a McFlurry. Go buy one.";
		    x = (this.vWidth/2) - (nmMetrics.stringWidth(toWrite)/2) ;
		    y = (int)( this.vHeight/2 + nmMetrics.getHeight());
			myG.drawString(toWrite, x,y);
		}
		

		private void drawGameOverScreen(Graphics g) {
			
			Graphics2D myG = (Graphics2D)g;
			g.setColor(SPLASH_BACKGROUND_C);
			myG.fillRect(0, 0, this.vWidth, this.vHeight);
			
			myG.setColor(SPLASH_NAME_C);
			Font myFont = new Font(FONT_NAME, Font.BOLD, (int)(Math.min(this.vWidth * FONT_NM_SZ_FACTOR, this.vHeight * FONT_NM_SZ_FACTOR)));
			myG.setFont(myFont);
			String toWrite = "Game Over!";
			FontMetrics nmMetrics = myG.getFontMetrics();
		    int x = (this.vWidth/2) - (nmMetrics.stringWidth(toWrite)/2) ;
		    int y = (int)( this.vHeight/2 - nmMetrics.getHeight()/2);
			myG.drawString(toWrite, x,y);
			
			toWrite = "Score: " + myModel.score;
		    x = (this.vWidth/2) - (nmMetrics.stringWidth(toWrite)/2) ;
		    y = (int)( this.vHeight/2 + nmMetrics.getHeight()/2);
			myG.drawString(toWrite, x,y);
			
		}
		
	}
	
	

	/*********************************************/
	/******************* MODEL *******************/
	/*********************************************/
	
	// model keeps track of game state (objects in the game)
	// contains a Timer that ticks periodically to advance the game
	// AND calls an update() method in the View to tell it to redraw
	class Model implements KeyListener, MouseMotionListener, ComponentListener, MouseListener {

		/************* Instance Variable *************/
		
		private Board myBoard;
		private Paddle myPaddle;
		private Ball myBall;
		private ArrayList<Brick> myBricks;
		private int score; 
		
		/**************** Constructor ****************/
		
		/* Pre:  myView has been initialized
		 * Post: Initializes the board, ball, paddle and bricks.
		 */
		public Model() {
			score = 0;
			myBoard = new Board(0,0,myView.getWidth(),myView.getHeight());
			myPaddle = this.createPaddle();
			myBall = this.createBall();
			this.createBricks();
			frame.setFocusable(true);
			frame.addKeyListener(this);
			frame.addMouseListener(this);
			addComponentListener(this);
		}
		
		/*************** Public Methods **************/
		
		/* Pre:  None
		 * Post: Creates a new thread to run the game
		 */
		public void runGame() {
		      Thread gameThread = new Thread() {    	
		    	  
		         public void run() {
		        	   while (!gameOver) {	
		        		   myModel.moveBall();
		        		   repaint();
			               try {
				              Thread.sleep(20);
				           } catch (InterruptedException ex) {}
		               }
		        	   repaint(); // paints game over or you won screen
		         }
		     };
		     gameThread.start();       
		}
		
		
		/* Pre:  Board, Paddle, Ball, and Brick arraylist are not null. g is not null.
		 * Post: Draws all view elements, and the current score
		 */
		public void draw(Graphics g) {
			myBoard.draw(g);
			myPaddle.draw(g); 
			myBall.draw(g); 
			for( Brick aBrick: myBricks) {
				aBrick.draw(g);
			}  
			Graphics2D myG = (Graphics2D)g; // this is bad coding style
			Font myFont = new Font(FONT_NAME, Font.BOLD, (int)(Math.min(myView.getWidth() * FONT_INST_SZ_FACTOR, myView.getHeight() * FONT_INST_SZ_FACTOR)));
			myG.setFont(myFont);
			String toWrite = "Score: " + this.score;
			FontMetrics nmMetrics = myG.getFontMetrics();
		    int x = (int)(0.03 * myView.getWidth()) ;
		    int y = (int)(myView.getHeight() - nmMetrics.getHeight() );
			myG.drawString(toWrite, x,y);
		}
		
		/* Pre:  Board, Paddle, Ball, and Brick arraylist are not null.
		 * Post: Moves the ball across the screen and checks for collisions
		 */
		public void moveBall() {
			if (playerStarted) {
				myBall.setXCoord((int) (myBall.getX() + myBall.getdX()));
				myBall.setYCoord((int) (myBall.getY() + myBall.getdY()));
				
				this.detectBoundaryCollisions();
				this.checkPaddleCollisions();
				this.checkBrickCollisions(); 
				
				if (this.allBricksDestroyed()) {
					gameOver = true;
					playerWon = true;
				} else
				if( myBall.intersects(myBoard.getMinX(), myBoard.getMaxY(), myBoard.getWidth(), 1) ) { // ball falls
					gameOver = true;
				}

			}
		}
		
		
		
		/* Pre:  e is not null. The game has been initialized
		 * Post: Moves the paddle left and right along with the mouse. Ensures paddle does does cross view boundary. 
		 *       If the player has not started playing, move the ball.
		 */
		@Override
		public void mouseMoved(MouseEvent e) {	
			double newX = e.getX();			
			if(newX - myPaddle.getWidth()/2 < myBoard.getMinX()) {
				newX = myBoard.getMinX() + myPaddle.getWidth()/2;	
			} else 
			if (newX + myPaddle.getWidth()/2 > myBoard.getMaxX()) {
				newX = myBoard.getMaxX() - myPaddle.getWidth()/2;
			}
			myPaddle.setRect(newX-myPaddle.getWidth()/2, myPaddle.getY(), myPaddle.getWidth(), myPaddle.getHeight());	
		}

		/* Pre:  e is not null. The Game has been initialized
		 * Post: Moves the paddle left and right according to keystrokes. Ensures paddle does does cross view boundary.
		 *       If this is the players first move, move the ball
		 */
		@Override
		public void keyPressed(KeyEvent e) {
			if(playerStarted) {
				double newX = -1;
				if(e.getKeyCode() == KeyEvent.VK_LEFT) { // If player moves left
					newX = myPaddle.getX() - PADDLE_MV_AMT;
					if(newX < myBoard.getMinX()) {
						newX = myBoard.getMinX();
					}
				} else
				if(e.getKeyCode() == KeyEvent.VK_RIGHT) { // If player moves right
					newX = myPaddle.getX() + PADDLE_MV_AMT;
					if (newX + myPaddle.getWidth() > myBoard.getMaxX()) {
						newX = myBoard.getMaxX() - myPaddle.getWidth();
					}
				}
				assert(newX != -1);
				myPaddle.setRect(newX, myPaddle.getY(), myPaddle.getWidth(), myPaddle.getHeight());
			} else
			{
				playerStarted = true;
				frame.addMouseMotionListener(this);
			}
			
		}

		
		
		/* Pre:  The view is initialized, and all displayables in it are initialized and not null.
		 * Post: Resizes all displayables, and positions them on the screen accordingly
		 * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
		 */
		@Override
		public void componentResized(ComponentEvent e) {
			
			Component myWindow = e.getComponent();
			Dimension myDim = myWindow.getSize();
			
			myView.setHeight((int)myDim.getHeight());
			myView.setWidth((int)myDim.getWidth());
			
			myBoard.updateSize(1, 1); // passing 1,1 because we want the board to fill view
			this.updateBricks(); // Update size of each brick is my Bricks arraylist, and reposition
			myBall.updateSize(BALL_RADIUS_FACTOR);
			myPaddle.updateSize(PADDLE_W_FACTOR, PADDLE_H_FACTOR); 
			
			if( myPaddle.getMaxX() > myBoard.getMaxX()) {
				double newX = myBoard.getMaxX() - myPaddle.getWidth();
				myPaddle.setRect(newX, myPaddle.getY(), myPaddle.getWidth(), myPaddle.getHeight());
			}
			
			if (!playerStarted) {
				// Make sure paddle is in screen center
				double paddleXC = myView.getWidth()/2 - myPaddle.getWidth()/2;
				myPaddle.setRect(paddleXC, myPaddle.getY(), myPaddle.getWidth(), myPaddle.getHeight());
				// Make sure ball is in center, on top of paddle
				double ballXC = myView.getWidth()/2 - myBall.getRadius();	
				double ballYC = myPaddle.getY() - 2*myBall.getRadius();
				myBall.setXCoord(ballXC);
				myBall.setYCoord(ballYC);
			} 
			
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			if (!playerStarted) {
				playerStarted = true;
				frame.addMouseMotionListener(this);
			}
		}

		/************** Private Methods **************/
		
		/* Pre:  The view has been initialized and is not null
		 * Post: Returns a paddle in the center of the screen
		 */
		private Paddle createPaddle() {
			double paddleWidth = myView.getWidth() * PADDLE_W_FACTOR;
			double paddleHeight = myView.getHeight() * PADDLE_H_FACTOR;
			double paddleXC = myView.getWidth()/2 - paddleWidth/2;
			double paddleYC = myView.getHeight() - (PADDLE_DFB*myView.getHeight());
			return new Paddle(paddleXC, paddleYC, paddleWidth, paddleHeight);
		}
		
		/* Pre:  The paddle and view have been initialized. The paddle and view are not null
		 * Post: Return a ball at the center of the screen, right on top of the paddle
		 */
		private Ball createBall() {
			double ballRadius = Math.max(myView.getWidth(), myView.getHeight()) * BALL_RADIUS_FACTOR;
			double ballXC = myView.getWidth()/2 - ballRadius;		
			double ballYC = myPaddle.getY() - 2*ballRadius;
			return new Ball(ballXC, ballYC, ballRadius);
		}
		
		
		/* Pre:  The view has been initialized and is not null
		 * Post: Places bricks in the view
		 */
		private void createBricks() {
		
			myBricks = new ArrayList<Brick>();
			
			// Calculate Brick Container Margins
			double sideMargin = myView.getWidth() * BRICK_DIS_FROM_SIDE_MARGIN_FACTOR;
			double topMargin = myView.getHeight() * BRICK_DIS_FROM_TOP_MARGIN_FACTOR;
			double leftMargin = sideMargin; // Left Margin
			double rightMargin = myView.getWidth() - sideMargin;			
			// Calculate Brick Width
			int brickWidth = (int) (BRICK_WIDTH_FACTOR * myView.getWidth());
			int brickHeight = (int) (BRICK_HEIGHT_FACTOR * myView.getHeight());			
			// counters
			int xStart = (int) leftMargin;
			int yStart = (int) topMargin;	
			for(int j=0; j<NUM_BRICKS_ROW; j++) {				
				for (int i=0; i<NUM_BRICKS_COL; i++) {
					Random rand = new Random();
					int level = rand.nextInt(NUM_BRICK_LEVELS);
					if (level == 0) {
						Brick aBrick = new Brick(xStart, yStart, BRICK_L1_COLOR, brickWidth, brickHeight, 1 );
						myBricks.add(aBrick);
					}
					if (level ==1) {
						Brick aBrick = new Brick(xStart, yStart, BRICK_L2_COLOR, brickWidth, brickHeight, 2 );
						myBricks.add(aBrick);
					}
					if (level ==2) {
						Brick aBrick = new Brick(xStart, yStart, BRICK_L3_COLOR, brickWidth, brickHeight, 3 );
						myBricks.add(aBrick);
					}
					xStart += brickWidth;
				}
				xStart = (int) leftMargin;
				yStart += brickHeight;
			}
		}
		
		/* Pre:  Arraylist myBricks is not null. The view has been initialized and is not null. 
		 * Post: Updates the size of each brick in myBricks, taking into account the Views size
		 */
		private void updateBricks() {
			for (Brick aBrick : myBricks) {
				aBrick.updateSize(BRICK_WIDTH_FACTOR, BRICK_HEIGHT_FACTOR);
			} 
			this.updateBrickLocations();
		}
		
		
		/* Pre:  The view is not null. The arraylist myBricks is not null
		 * Post: Ensures that bricks are not stacked on top of one another on the view, and are
		 *       adjacent. This method is meant to be called when the bricks width/height have 
		 *       been changed, but their x and y coordinates remain the same (hence are stacked
		 *       on top of on one another). 
		 */
		private void updateBrickLocations() {
			
			// Calculate Brick Container Margins
			double sideMargin = myView.getWidth() * BRICK_DIS_FROM_SIDE_MARGIN_FACTOR;
			double topMargin = myView.getHeight() * BRICK_DIS_FROM_TOP_MARGIN_FACTOR;
			double leftMargin = sideMargin; // Left Margin
			// Calculate Brick Width
			int brickWidth = (int) (BRICK_WIDTH_FACTOR * myView.getWidth());
			int brickHeight = (int) (BRICK_HEIGHT_FACTOR * myView.getHeight());
			// counters
			int xStart = (int) leftMargin;
			int yStart = (int) topMargin;	
			for(int j=0; j<NUM_BRICKS_ROW; j++) {			
				for (int i=0; i<NUM_BRICKS_COL; i++) {			
					Brick myB = myBricks.get((j*NUM_BRICKS_COL) + i);
					myB.setRect(xStart, yStart, brickWidth, brickHeight);
					xStart += brickWidth;
				}
				xStart = (int) leftMargin;
				yStart += brickHeight;
			}
		}
		

		/* Pre:   myBall and myPaddle are not null
		 * Post:  Checks if the ball intersects with the paddle, if so, bounces back up. If ball hits
		 *  	  edge of paddle
		 */
		private void checkPaddleCollisions() {
			
			double edgeSize = 0.1*myPaddle.getWidth();
			double centerSize = 0.8*myPaddle.getWidth();
			
			Rectangle2D leftR = new Rectangle2D.Double(myPaddle.getX(), myPaddle.getY(), edgeSize, myPaddle.getHeight());
			Rectangle2D rightR = new Rectangle2D.Double(myPaddle.getMaxX()-edgeSize, myPaddle.getY(), edgeSize, myPaddle.getHeight());
			Rectangle2D centerR = new Rectangle2D.Double(leftR.getMaxX(),myPaddle.getY(),centerSize, myPaddle.getHeight());
			
			if ( myBall.intersects(rightR) ) {
				myBall.setYCoord(myPaddle.getY()-(2*myBall.getRadius())-1);
				myBall.setdY(-myBall.getdY());
				myBall.setdX(Math.abs(myBall.getdX()));

			}  else
			if ( myBall.intersects(leftR) ) {
				myBall.setYCoord(myPaddle.getY()-(2*myBall.getRadius())-1);
				myBall.setdY(-myBall.getdY());
				myBall.setdX(-Math.abs(myBall.getdX()));
			}  else
			if( myBall.intersects(centerR)) {
				myBall.setYCoord(myPaddle.getY()-(2*myBall.getRadius())-1);
				myBall.setdY(-myBall.getdY());
			} 
		}
		
		
		/* Pre:  myBall is not null
		 * Post: Checks whether the ball collided with the window boundary. If so, changes ball direction
		 */
		private void detectBoundaryCollisions() {
			
			if (myBall.getX() < myBoard.getMinX()) {  // Left Bound Collision
				myBall.setdX(-myBall.getdX()); 
				myBall.setXCoord(myBoard.getMinX());
			} 
			if (myBall.getMaxX() > myBoard.getMaxX()) { // Right Bound Collision
				myBall.setdX(-myBall.getdX());
				myBall.setXCoord(myBoard.getMaxX() - myBall.getRadius()*2);
			}
			if (myBall.getY() < myBoard.getMinY()) { // Top Bound Collision
				myBall.setdY(-myBall.getdY());
				myBall.setYCoord(myBoard.getMinY());
			}

		}
		
		/* Pre:  myBall is not null. myBricks arraylist is not null
		 * Post: Checks if myBall collided with any of the bricks. If so, increments the score, changes the brick color
		 * 	 	 or destroys it accordingly. Then changes direction of the ball.
		 */
		private void checkBrickCollisions() {
			
			boolean ydirectionChanged = false;
			boolean xdirectionChanged = false;
			
			for (Brick b : myBricks) {
				if( !b.isDestroyed()) {
					if(myBall.intersects(b)) {
							if (myBall.getMinY() < b.getMinY() ) { // Ball collided from top
								myBall.setYCoord(b.getMinY() - myBall.getHeight() - 1);
								/*try { 
									Thread.sleep(5000);
								} catch (Exception e) {}*/
							} else
							if (myBall.getMaxY() > b.getMaxY() ) { //Ball hit from bottom
								myBall.setYCoord(b.getMaxY() + 1);
							} else
							if (myBall.getMinX() < b.getMinX() ) { // Ball hit from left side
								myBall.setXCoord(b.getMinX() - myBall.getWidth()-1);
								if (!xdirectionChanged) {
									myBall.setdX(-Math.abs(myBall.getdX()));
									xdirectionChanged = true;
								}
							} else
							if (myBall.getMaxX() > b.getMaxX()) { // Ball hit from right side
								myBall.setXCoord(b.getMaxX() + 1);
								if (!xdirectionChanged) {
									myBall.setdX(Math.abs(myBall.getdX()));
									xdirectionChanged = true;
								}
							}
							if(!ydirectionChanged) { // dont change direction twice
								myBall.setdY(-myBall.getdY());
								ydirectionChanged = true;
							}
						b.hitBrick();
						this.score += 10;
					}
				}
			}
		}
		
		
		/* Pre:  myBricks is not null
		 * Post: Returns true if all bricks are destroyed
		 */
		private boolean allBricksDestroyed() {
			
			for(Brick b : myBricks) {
				if(!b.isDestroyed()) {
					return false;
				}
			}
			return true;
		}
		/******************* Bloat *******************/
		
		// Ignore
		public void mouseDragged(MouseEvent arg0) {}
		public void keyReleased(KeyEvent arg0) {}
		public void componentHidden(ComponentEvent arg0) {}
		public void componentMoved(ComponentEvent arg0) {}
		public void componentShown(ComponentEvent arg0) {}
		public void keyTyped(KeyEvent arg0) {}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
		
	}
	
	
	/*********************************************/
	/**************** DISPLAYABLE ****************/
	/*********************************************/
	
	class Rectangle extends Rectangle2D.Double {
		
		/************* Instance Variable *************/
		
		protected Color myColor; // Displayable Element Color
		
		/**************** Constructor ****************/
		
		/* Pre:  x is the x coordinate of the top left corner of displayable element. 
		 * 		 y is the y coordinate of the top left corner of displayable element.
		 * 		 c is the color of the displayable element.
		 * Post: Creates a displayable element at position x,y on the view
		 * 		 with color c. Stores the size of the element relative to the view.
		 */
		public Rectangle(double x, double y, double w, double h) {
			super(x,y,w,h);
			myColor = Color.GRAY; // Default Color for Displayable
		}
				
		/*************** Public Methods **************/
		
		/* Pre:  None
		 * Post: Returns a string representation of this displayable
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			String a = "X Coor: " + this.x;
			String b = "Y Coor: " + this.y;
			String c = "Width: " + this.width;
			String d = "Height: " + this.height;
			return a + "\n" + b+ "\n" + c+ "\n" + d;
		}
		
		/* Pre:  None.
		 * Post: Updates the size of the RectDisplayable to a proportion of the view,
		 *       as identified by widthFactor and heightFactor
		 */
		public void updateSize(double widthFactor, double heightFactor) {
			this.width = widthFactor * myView.getWidth();
			this.height = heightFactor * myView.getHeight();
		}
		
		public void draw(Graphics g) {
			Graphics2D myG = (Graphics2D)g;
			myG.setColor(this.myColor);
			myG.fill(this);
		}
		
	}
	

	/*********************************************/
	/******************* BOARD *******************/
	/*********************************************/
	
	class Board extends Rectangle {
		
		public Board(double x, double y, double w, double h) {
			super(x,y,w,h);
			this.myColor = BOARD_COLOR;
		}
	}
	
	/*********************************************/
	/******************* PADDLE ******************/
	/*********************************************/
	
	class Paddle extends Rectangle  {
		
		public Paddle(double x, double y, double w, double h) {
			super(x,y,w,h);
			this.myColor = PADDLE_COLOR;
		}
			
		/* Pre:  The view has been initialized.
		 * Post: Updates the size of the paddle to a proportion of the screen based on wF and hF. 
		 *       Repositions the paddle at the bottom of the screen, as specified by PADDLE_DFB
		 * @see Breakout.RectDisplayable#updateSize(double, double)
		 */
		public void updateSize(double wF, double hF) {		
			super.updateSize(wF, hF);	
			double y =  myView.getHeight() - (PADDLE_DFB*myView.getHeight());
			this.setRect(this.x, y, this.width, this.height);
		}
		
		
	}
	
	/*********************************************/
	/******************** BRICK ******************/
	/*********************************************/

	class Brick extends Rectangle  {

		/************* Instance Variable *************/
		protected boolean destroyed; // tracks whether the brick is destroyed or not
		protected int numHitsLeft; // Number of collisions with ball
		
		//protected int maxHits; // Number of collisions needed for brick to be destroyed. Default value is 1.
		
		/**************** Constructor ****************/
		
		/* Pre:  See Super
		 * Post: See Super
		 */
		public Brick(double x, double y, Color c, double w, double h, int numHits) {
			super(x,y,w,h);
			this.destroyed = false;
			this.numHitsLeft = numHits;
			this.myColor = c;
		}
		
		/****************** Accessor *****************/
		
		public boolean isDestroyed() 	{ return destroyed;	 	 }
		public int getNumHitsLeft()  	{ return numHitsLeft; 	 }
		public void destroy() 			{ this.destroyed = true; }
		
		/*************** Public Methods **************/
		
		/* Pre:  None.
		 * Post: Records that the brick got hit. If numHits is equal to maxHits, the brick
		 * 		 is destroyed
		 */
		public void hitBrick() {
			this.numHitsLeft--;
			if (this.numHitsLeft == 0) {
				this.destroyed = true;
			} else if( this.numHitsLeft == 1) {
				this.myColor = BRICK_L1_COLOR;
			} else if( this.numHitsLeft == 2) {
				this.myColor = BRICK_L2_COLOR;
			} 
		}
		
		public void draw(Graphics g) {
			Graphics2D myG = (Graphics2D)g;
			if(destroyed == false) {
				myG.setColor(myColor);
				myG.fill(this);
				myG.setColor(myColor.WHITE);
				myG.draw(this);
				/*
				myG.setColor(Color.WHITE);
				myG.setStroke(new BasicStroke(1));
				myG.drawLine(this.x, this.y, this.x+this.width, this.y);
				myG.drawLine(this.x, this.y+this.height, this.x + this.width, this.y+this.height);
				myG.drawLine(this.x, this.y, this.x, this.y+this.height);
				myG.drawLine(this.x + this.width, this.y, this.x+this.width, this.y+this.height); */
			}
		}
		
		public String toString() {
			return super.toString() + "\n NumHitsLeft: " + numHitsLeft;
		}
		
	}
	
	/*********************************************/
	/******************** BALL *******************/
	/*********************************************/
	
	class Ball extends Ellipse2D.Double {
		
		/************* Instance Variable *************/
		
		private Color color = BALL_COLOR;
		private double dX = speed* Math.cos(INIT_ANGLE);
		private double dY  = -speed * Math.sin(INIT_ANGLE);
		private double radius;
		
		/**************** Constructor ****************/
		
		/* Pre:  c is not null. x and y are within the view
		 * Post: creates a ball whose upper left corner is at x,y. Sets the speed of
		 * 		 of the ball to dX and dY.
		 */
		public Ball(double x, double y, double radius) {
			super(x,y, radius*2, radius*2);
			this.radius = radius;
		}
		
		/****************** Accessor *****************/
		
		public double getRadius() 	 { return this.radius;	}
		public double getdX()		 { return this.dX;	 	}
		public double getdY()		 { return this.dY;		}
		
		public void setXCoord(double x) { this.x = x;		}
		public void setYCoord(double y) { this.y = y;		}
		public void setdX(double dx)    { this.dX = dx; 	}
		public void setdY(double dy)    { this.dY = dy; 	}
		public void setRadius(int r)    { this.radius = r;	}

		
		
		
		/*************** Public Methods **************/
		
		/* Pre:  None
		 * Post: sets the size of the of the ball to a proportion of the view 
		 * 		 based on the value of radiusFactor. This is essentially the same
		 * 		 as setRadius.
		 */
		public void updateSize(double ballRadiusFactor)  {
			this.radius = Math.max(myView.getWidth(), myView.getHeight())*BALL_RADIUS_FACTOR;
			this.width = radius*2;
			this.height = radius*2;
		}
		
		
		
		/* Pre: g is not null
		 * Post:  Draws the ball on the screen, using its x and y as center
		 */
		public void draw(Graphics g) {
			Graphics2D myG = (Graphics2D)g;
			myG.setColor(BALL_COLOR);
			myG.fill(this);
			
		}
		
		/* Pre: None.
		 * Post: @see Breakout.Displayable#toString()
		 */
		public String toString() {
			String a = "Coordinates: (" + x +", " + y +")";
			String b = "Radius: " + this.radius;
			String c = "dX: " + this.dX;
			String d = "dY: " + this.dY;
			return a + "\n" + b + "\n" + c + "\n" + d;
		}
	
	}
}
