import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.Random;

class Timer{
	long timerLength;
	long startTime = 0;
	public Timer(long timerLength) {
		this.timerLength = timerLength;
		reset();
	}

	public boolean finished() {
		long currentTime = System.currentTimeMillis();
		return (currentTime-startTime>timerLength);
	}

	public void reset() {
		long currentTime = System.currentTimeMillis();
		startTime = currentTime;
	}

	public long timeDif(){
//		return (currentTime-startTime);
		return 42;
	}
}

abstract class entity{
	int x;
	int y;
	boolean removeThisObject = false;
	boolean checkCollision = false;
	int points = 0;
	String entityName = "";
	public BufferedImage image;
	public entity(int x,int y) {
		this.x = x;
		this.y = y;
	}

	public entity(int x,int y,String entityName) {
		this.x = x;
		this.y = y;
		this.entityName = entityName;
	}
	
	public entity(int x,int y,String entityName,String imageName) {
		this.x = x;
		this.y = y;
		this.entityName = entityName;
		try {
		image = ImageIO.read(new File(imageName));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	abstract void update();

	abstract void draw(Graphics g);
	public boolean hasCollided(int x,int y){
		return false;
	}
	public void collided(entity otherEntity){

	}
	public void removeThisObject(){
		removeThisObject = true;
	}
	public int getPoints() {
		return points;
	}
}

class laserBeam extends entity {
	public laserBeam(int x,int y){
		super(x,y,"laserBeam");
		checkCollision = true;
	}
	public void update() {
		y += 11;
		if (y > 800) {
			removeThisObject();
		}
	}

	public void draw(Graphics g) {
		g.setColor(Color.magenta);
		g.drawLine(x, y, x, y+30);
		g.drawLine(x+1, y, x+1, y+30);
		g.drawLine(x+2, y, x+2, y+30);
	}
	public void collided(entity otherEntity){
		otherEntity.removeThisObject();
		removeThisObject();
		myPanel.myPlayer.points += otherEntity.getPoints();
		if (myPanel.myPlayer.unlockedReflectEnemyLaser && otherEntity.entityName.equals("alienLaserBeam")) {
			myPanel.entities.add(new laserBeam(x+20, 50));
			myPanel.entities.add(new laserBeam(x-20, 50));
		}
	}
}

class alienLaserBeam extends entity {
	myPanel MyPanel;
	public alienLaserBeam(int x, int y,myPanel MyPanel) {
		super(x, y,"alienLaserBeam");
		this.MyPanel = MyPanel;
		checkCollision = true;
		points = 500;
	}

	public void update() {
		y -= 7;
		if (y < 0) {
			removeThisObject();
		}
	}

	public void draw(Graphics g) {
		g.setColor(Color.red);
		Graphics2D g2 = (Graphics2D) g;
		g2.setStroke(new BasicStroke(5));
		g2.drawLine(x, y, x, y + 30);
	}

	public void collided(entity otherEntity) {
		if (otherEntity.entityName.equals("player")){
			MyPanel.myPlayer.looseHealth();
			removeThisObject();
		}
	}

	public boolean hasCollided(int otherX,int otherY){
		if (MyPanel.myPlayer.unlockedHitEnemyLaser) {
			return (otherX < x+20 && otherX > x-20 && otherY < y+20 && otherY > y-20);
		}
		return false;
	}
}

class alien extends entity{
	int width = 40;
	int height = 40;
	int speed = 1; // in pixels
	String imageName;
	myPanel MyPanel;


	public alien(int x,int y,myPanel MyPanel,int speed, String imageName){
		super(x,y,"bloon",imageName);

		this.MyPanel = MyPanel;
		this.speed = speed;
	}
	public void update(){
		double dSpeed = speed;
		y-= dSpeed / 2.0;
		if (y < 0) {
			MyPanel.myPlayer.looseHealth();
			removeThisObject();
		}
		if (Math.random() < 0.005){
			MyPanel.entities.add(new alienLaserBeam(x,y-50,MyPanel));
		}
	}

	public void draw(Graphics g){
		g.drawImage(image,x,y,255/5,330/5,null);
	}

	public boolean hasCollided(int otherX,int otherY){
		return (otherX < x+width && otherX > x-width && otherY < y+height && otherY > y-height);
	}

	public int getPoints() {
		return 100+50*speed;
	}
}

class alienFactory{
	myPanel MyPanel;
	public alienFactory(myPanel MyPanel){
		this.MyPanel = MyPanel;
	}

	public alien newAlien(int x, int y){
		
		return new alien(x,y,MyPanel,1,"Images/Bloon_BLUE.png");
	}

	public alien newAlien(int x, int y,int speed){

		if(speed > 3){
			return new alien(x,y,MyPanel,speed, "Images/Bloon_GREY.png");
		}

		switch (speed) {
			case 2:
			return new alien(x,y,MyPanel,speed, "Images/Bloon_GREEN.png");
		
			case 3:
			return new alien(x,y,MyPanel,speed, "Images/Bloon_RED.png");

			default:
			return new alien(x,y,MyPanel,speed, "Images/Bloon_BLUE.png");
		}

	}

	public alien newAlien(int x, int y,int speed, boolean vis){
		return new alien(x,y,MyPanel,speed, "");
	}
}

class player extends entity {
	public boolean gameOver;
	int health = 6;
	int playerDirection;
	public boolean shoot;
	private ArrayList<entity> entities;
	public boolean unlockedTwoLaser = true;
	public boolean unlockedTrident = false;
	public boolean unlockedHitEnemyLaser = true;
	public boolean unlockedReflectEnemyLaser = false;
	public boolean unlockedTeleportToCenter = false;
	public boolean unlockedChargeLaser = false;
	Timer shootTimer = new Timer(250);
	Timer chargeTimerStart = new Timer(250);
	Timer chargeTimerFinished = new Timer(1000);
	public player(int x, int y,ArrayList<entity> entities) {
		super(x, y,"player","Images/player.png");
		this.entities = entities;
		checkCollision = true;
		// TODO Auto-generated constructor stub
	}

	public void looseHealth(){
		health -= 1;
		if (health == 0){
			gameOver = true;
		}
	}

	@Override
	void update() {
		if (chargeTimerStart.finished()){
			if (unlockedTeleportToCenter){
				x = 500;
			}
			chargeTimerStart.reset();
		}
		if (!shoot) {
			chargeTimerStart.reset();
		}
		// TODO Auto-generated method stub
		if (x < 1000 && playerDirection == 1) {
			x += playerDirection*5;
		}
		if (x > 0 && playerDirection == -1) {
			x += playerDirection*5;
		}

		if (shoot && shootTimer.finished() && !chargeTimerStart.finished()) {
			shootTimer.reset();

			if (playerDirection == 0){
				if (unlockedTrident){
					entities.add(new laserBeam(x, 50));
					entities.add(new laserBeam(x+20, 50));
					entities.add(new laserBeam(x-20, 50));
				}
				else if (unlockedTwoLaser){
					entities.add(new laserBeam(x+20, 50));
					entities.add(new laserBeam(x-20, 50));
				}
			}else{
				entities.add(new laserBeam(x, 50));
			}

		}
	}

	public void collided(entity otherEntity){

//		System.out.println("collided");
//		otherEntity.removeThisObject();
//		looseHealth();
	}

	@Override
	void draw(Graphics g) {
		// TODO Auto-generated method stub
		//g.fill3DRect(x,50,20,20, true);
		g.drawImage(image,x-28,50,330/5,240/5,null);
		for (int i=0;i<health;i++){
			//g.fill3DRect(25+i*25,25,20,20, true);
			g.drawImage(image, 25+i*25,25,55,55, null);
		}
		g.drawString(""+points+"Points", 750, 900);
	}

	public boolean hasCollided(int otherX,int otherY){
		return (otherX < x+20 && otherX > x-20 && otherY < y+20 && otherY > y-20);
	}
}

class wave{
	ArrayList<entity> entities;
	ArrayList<entity> waveEntities = new ArrayList<entity>();
	public wave(ArrayList<entity> entities){
		this.entities = entities;
	}

	public void add(entity e){
		this.waveEntities.add(e);
	}
	public void startWave(){
		for (entity e:waveEntities){
			entities.add(e);
		}
	}

	public boolean isDefeated(){
		for (entity e:waveEntities){
			if (!e.removeThisObject)  {
				return false;
			}
		}
		return true;
	}

}

//class waveGroup extends wave{
//	wave[] waves;
//
//	public waveGroup(wave[] waves) {
//		super(null);
//		// TODO Auto-generated constructor stub
//		this.waves = waves;
//	}
//
//	public void startWave(){
//		for (wave w:waves){
//			w.startWave();
//		}
//	}
//
//	public boolean isDefeated(){
//		for (wave w:waves){
//			if (!w.isDefeated())  {
//				return false;
//			}
//		}
//		return true;
//	}
//}

//class lineWave extends wave{
//
//	public lineWave(ArrayList<entity> entities,int startX,int startY) {
//		super(entities);
//		// TODO Auto-generated constructor stub
//		for (int i=0;i<4;i++) {
//			add(AlienFactory.newAlien(startX, startY+i*20,2));
//		}
//	}
//
//
//}

//class fanWave extends wave{
//
//	public fanWave(ArrayList<entity> entities,int startX,int startY) {
//		super(entities);
//		// TODO Auto-generated constructor stub
//		for (int i=0;i<4;i++) {
//			add(new alien(startX+i*50, startY,2));
//		}
//		for (int i=0;i<4;i++) {
//			add(new alien(startX-i*50, startY,2));
//		}
//	}
//
//
//}

class myPanel extends JPanel implements MouseListener,KeyListener{
	Graphics G;
	Timer graphicsTimer = new Timer(10);
	public static ArrayList<entity> entities = new ArrayList<entity>();
	LinkedList<wave> waves = new LinkedList<wave>();
	Iterator<wave> wavesIterator;
	wave currentWave;
	BufferedImage screenBuffer = new BufferedImage(1000,1000,BufferedImage.TYPE_INT_ARGB);
	Graphics bufferG;
	alienFactory AlienFactory;
	Random rand = new Random();
	long lastTime = 0;
	public static player myPlayer;
	int playerDirection = 0;
	boolean shoot = false;
	myPanel(){
		myPlayer = new player(0, 0,entities);
		entities.add(myPlayer);
		
		AlienFactory = new alienFactory(this);

		generateWaves(1);
		finalWave();
		end();

		wavesIterator = waves.iterator();
		nextWave();
	}

	public void generateWaves(int num){
		for (int i = 0; i < num; i++){
			wave temp = new wave(entities);
			temp.add(AlienFactory.newAlien(500,950));
			temp.add(AlienFactory.newAlien(600,950));
			temp.add(AlienFactory.newAlien(400,950));
			temp.add(AlienFactory.newAlien(500,850));
			temp.add(AlienFactory.newAlien(600,850));
			temp.add(AlienFactory.newAlien(400,850));

			temp.add(AlienFactory.newAlien(500,750, 2));
			temp.add(AlienFactory.newAlien(600,750, 2));
			temp.add(AlienFactory.newAlien(400,750, 2));
			temp.add(AlienFactory.newAlien(500,650, 2));
			temp.add(AlienFactory.newAlien(600,650, 2));
			temp.add(AlienFactory.newAlien(400,650, 2));

			waves.add(temp);
		}
	}


	public void nextWave() {
		currentWave = wavesIterator.next();
		currentWave.startWave();
	}

	public void finalWave(){
		wave temp = new wave(entities);

		temp.add(AlienFactory.newAlien(500,1050, 4));
		temp.add(AlienFactory.newAlien(600,1050, 4));
		temp.add(AlienFactory.newAlien(400,1050, 4));
		temp.add(AlienFactory.newAlien(500,1150, 4));
		temp.add(AlienFactory.newAlien(600,1150, 4));
		temp.add(AlienFactory.newAlien(400,1150, 4));

		temp.add(AlienFactory.newAlien(500,950, 3));
		temp.add(AlienFactory.newAlien(600,950, 3));
		temp.add(AlienFactory.newAlien(400,950, 3));
		temp.add(AlienFactory.newAlien(500,850, 3));
		temp.add(AlienFactory.newAlien(600,850, 3));
		temp.add(AlienFactory.newAlien(400,850, 3));

		temp.add(AlienFactory.newAlien(500,750, 2));
		temp.add(AlienFactory.newAlien(600,750, 2));
		temp.add(AlienFactory.newAlien(400,750, 2));
		temp.add(AlienFactory.newAlien(500,650, 2));
		temp.add(AlienFactory.newAlien(600,650, 2));
		temp.add(AlienFactory.newAlien(400,650, 2));

		waves.add(temp);
	}

	public void end(){
		wave end = new wave(entities);

		end.add(AlienFactory.newAlien(0,0,0,false));
		waves.add(end);
	}

	public void setGraphics() {
		G = getGraphics();
		bufferG = screenBuffer.createGraphics();
		bufferG.setFont(new Font("Arial Black", Font.BOLD, 30));
	}

	//	static long prev = 0;
	public void paintComponent(Graphics g) {
//		long now = System.currentTimeMillis();
//		System.out.println(prev-now);
//		prev = now;

		bufferG.setColor(new Color(255, 179, 71));
		bufferG.fillRect(0, 0, 1000, 1000);
		bufferG.setColor(Color.CYAN);


		myPlayer.playerDirection = playerDirection;
		myPlayer.shoot = shoot;


		//System.out.println("num entities: "+entities.size());
		for (int i=0;i<entities.size();i++) {
			entities.get(i).update();
			//System.out.println("x: "+entities.get(i).x+" y: "+entities.get(i).y);
		}

		for (int i=0;i<entities.size();i++) {
			if (entities.get(i).checkCollision){
				for (int j=0;j<entities.size();j++) {
					if (entities.get(j).hasCollided(entities.get(i).x,entities.get(i).y)){
						entities.get(i).collided(entities.get(j));
					}
				}
			}
		}

		for (int i=0;i<entities.size();i++) {
			if (entities.get(i).removeThisObject){
				entities.remove(i);

				if (currentWave.isDefeated()){
					if(waves.size() >= 1){
						nextWave();
					} else{

					}

				}
			}
		}

		for (int i=0;i<entities.size();i++) {
			entities.get(i).draw(bufferG);
		}
		g.drawImage(screenBuffer,0,0,null);
	}

	//	static int callCount = 0;
	public void redraw() {
//		callCount += 1;
		if (graphicsTimer.finished()) {

			graphicsTimer.reset();
//			System.out.println("callCount");
//			System.out.println(callCount);
//			callCount = 0;
			paintComponent(G);
		}


	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		//System.out.println(e.getKeyCode());
		if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			playerDirection = -1;
		}
		else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			playerDirection = 1;
		}

		if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			shoot = true;
		}

		if (e.getKeyCode() == KeyEvent.VK_ESCAPE){
			userInterface.playOn = false;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_LEFT && playerDirection == -1) {
			playerDirection = 0;
		}
		else if (e.getKeyCode() == KeyEvent.VK_RIGHT && playerDirection == 1) {
			playerDirection = 0;
		}

		if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			shoot = false;
		}

		if (e.getKeyCode() == KeyEvent.VK_ESCAPE){
			userInterface.playOn = true;

		}

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}
}

public class userInterface implements KeyListener{
	JFrame frame;
	myPanel panel;
	public static boolean playOn = true;
	userInterface() {
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		panel = new myPanel();
		frame.addKeyListener(panel);
		frame.getContentPane().add(panel);
		frame.setSize(1000,1000);
		frame.setVisible(true);

		panel.setGraphics();
		while (playOn) {
			panel.redraw();
		}
	}

	public void periodic(){

	}

	@Override
	public void keyPressed(KeyEvent e) {

		if (e.getKeyCode() == KeyEvent.VK_ESCAPE){
			userInterface.playOn = false;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {

		if (e.getKeyCode() == KeyEvent.VK_ESCAPE){
			userInterface.playOn = true;

		}

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

}
