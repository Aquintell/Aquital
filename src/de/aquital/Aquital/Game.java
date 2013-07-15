package de.aquital.Aquital;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import javax.swing.JFrame;

import de.aquital.Aquital.graphics.Screen;
import de.aquital.Aquital.input.Keyboard;

public class Game extends Canvas implements Runnable{
	private static final long serialVersionUID = 1L;
	
	public static int width = 300;
	public static int height = width / 16 * 9;
	public static int scale = 3;
	public static String title = "Aquital";
	
	private Thread thread; // A Process within a Process - A Subprocess
						   // 1 Thread is the Game we want another
	
	private JFrame frame; 	// Our Window
	private Keyboard key;
	
	private boolean running = false; //Gameloop activisor
	
	
	private Screen screen;
	
	private BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB); // new Image Object which has Acces to the Buffer
	private int[] pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData(); // hoolds every pixel
	
	public Game() {
		Dimension size = new Dimension(width * scale, height * scale); //Dimension holds Size
		setPreferredSize(size); //method in canvas which sets the Size
		
		screen = new Screen(width, height);
		frame = new JFrame();
		key= new Keyboard();
		
		addKeyListener(key);
		}
	
	
	public synchronized void start(){ 			  //synchronized = pretends errors, no overlaps
		running = true;
		thread = new Thread(this, "Display");	  //thread is attached to the Gameobeject by this
		thread.start();
	}
	
	public synchronized void stop(){
		running = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
	}
	
	public void run(){ // overrides runnable
		long lastTime = System.nanoTime();
		long timer = System.currentTimeMillis();
		final double ns = 1000000000.0 / 60;
		double delta = 0;
		int frames = 0;
		int updates = 0;
		
		while(running){
			long  now = System.nanoTime();
			delta += (now- lastTime) / ns;
			lastTime = now;
			
			while(delta >= 1){
				update();
				
				updates++;
				delta--;
			}
			
			render();
			
			
			frames++;
			
			if(System.currentTimeMillis() - timer > 1000){
				timer += 1000;
				frame.setTitle("Title" + "  |  "  + updates + " ups, " + frames + "fps");
				updates = 0;
				frames =  0;
			}
			
		}
		
		stop();
		
	}

	int x = 0, y = 0;
	
	
	private void update() {		//logic stuff
		
		key.update();
		if(key.up) y--;
		if(key.down) y++;
		if(key.left) x--;
		if(key.right) x++;
		
	
	}
	
	private void render() {		//fancy stuff
		BufferStrategy bs = getBufferStrategy();	//A Buffer is a temporary storage place to hold data before it
		if(bs == null){								//gets displayed or whatever. Due to this we don't need to render
			createBufferStrategy(3);				//while running.
			return;									//(3) means Tripplebuffering 
		}
		screen.clear();
		screen.render(x,y );
		
		for(int i =0; i < pixels.length; i++){
			pixels[i] = screen.pixels[i];
		}
		
		Graphics g = bs.getDrawGraphics();			//creates Link between Graphics and Bufferstrategy
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getWidth(),getHeight());
		g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
			
		
		g.dispose();						//Graphics need to be removed after every Frame otherwise
											//they'd stack and the Game would crash
		bs.show();									
		}


	


	public static void main(String[] args) { //first method that will be executed
		Game game = new Game();				 // can't access frame through this method
		game.frame.isFocused();
		game.frame.setResizable(false);		 // but we can through an Game obeject
		game.frame.setTitle(Game.title);
		game.frame.add(game); 				 // frame gets add to Game
		game.frame.pack();					 // sets size of our component
		game.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		game.frame.setLocationRelativeTo(null);		// centers the frame into the middle
		game.frame.setVisible(true);				// lets the frame be shown
		
		game.start();
		
		
		

	}
	
	

}
