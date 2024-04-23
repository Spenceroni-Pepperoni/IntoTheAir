import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.image.BufferedImage;
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
	public entity(int x,int y) {
		this.x = x;
		this.y = y;
	}

	public entity(int x,int y,String entityName) {
		this.x = x;
		this.y = y;
		this.entityName = entityName;
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
		g.drawLine(x, y, x, y + 30);
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
	myPanel MyPanel;
	public alien(int x,int y,myPanel MyPanel,int speed){
		super(x,y);
		this.MyPanel = MyPanel;
		this.speed = speed;
	}
	public void update(){
		y-=speed;
		if (y < 0) {
			MyPanel.myPlayer.looseHealth();
			removeThisObject();
		}
		if (Math.random() < 0.001){
			MyPanel.entities.add(new alienLaserBeam(x,y-50,MyPanel));
		}
	}

	public void draw(Graphics g){
		g.fill3DRect(x,y,width,height, true);
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
		return new alien(x,y,MyPanel,1);
	}

	public alien newAlien(int x, int y,int speed){
		return new alien(x,y,MyPanel,speed);
	}
}

class player extends entity {
	public boolean gameOver;
	int health = 6;
	int playerDirection;
	public boolean shoot;
	private ArrayList<entity> entities;
	public boolean unlockedTwoLaser = true;
	public boolean unlockedTrident;
	public boolean unlockedHitEnemyLaser = false;
	public boolean unlockedReflectEnemyLaser = false;
	public boolean unlockedTeleportToCenter = false;
	public boolean unlockedChargeLaser = true;
	Timer shootTimer = new Timer(250);
	Timer chargeTimerStart = new Timer(250);
	Timer chargeTimerFinished = new Timer(1000);
	public player(int x, int y,ArrayList<entity> entities) {
		super(x, y,"player");
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
			System.out.println("shoot");

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
		g.fill3DRect(x,50,20,20, true);
		for (int i=0;i<health;i++){
			g.fill3DRect(25+i*25,25,20,20, true);
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
	long lastTime = 0;
	public static player myPlayer;
	int playerDirection = 0;
	boolean shoot = false;
	myPanel(){
		myPlayer = new player(0, 0,entities);
		entities.add(myPlayer);
		Random rand = new Random();
		alienFactory AlienFactory = new alienFactory(this);

		wave wave1 = new wave(entities);
		//Spawn the first wave
		wave1.add(AlienFactory.newAlien(500,950));
		wave1.add(AlienFactory.newAlien(600,950));
		wave1.add(AlienFactory.newAlien(400,950));
		wave1.add(AlienFactory.newAlien(500,850));
		wave1.add(AlienFactory.newAlien(600,850));
		wave1.add(AlienFactory.newAlien(400,850));
		waves.add(wave1);

		wave testWave = new wave(entities);
		//Spawn the first wave
		testWave.add(AlienFactory.newAlien(500,950+400));
		testWave.add(AlienFactory.newAlien(600,950+400,3));
		testWave.add(AlienFactory.newAlien(400,1150+400));
		testWave.add(AlienFactory.newAlien(200,850+400));
		testWave.add(AlienFactory.newAlien(600,850+400,3));
		testWave.add(AlienFactory.newAlien(400,850+400));
		int centerX = 500;
		int centerY = 1300+400;
		for(int i=0;i<15;i++) {
			testWave.add(AlienFactory.newAlien((int)rand.nextGaussian(centerX, 50),(int)rand.nextGaussian(centerY, 100)));
		}
		waves.add(testWave);

		wave testWave2 = new wave(entities);
		testWave2.add(AlienFactory.newAlien(500,950+350,3));
		testWave2.add(AlienFactory.newAlien(600,950+350,3));
		testWave2.add(AlienFactory.newAlien(600,1950+350,4));

		centerX = 500;
		centerY = 1300+350;
		for(int i=0;i<5;i++) {
			testWave2.add(AlienFactory.newAlien((int)rand.nextGaussian(centerX, 150),(int)rand.nextGaussian(centerY, 100)));
		}
		centerX = 500;
		centerY = 2300+350;
		for(int i=0;i<5;i++) {
			testWave2.add(AlienFactory.newAlien((int)rand.nextGaussian(centerX, 150),(int)rand.nextGaussian(centerY, 100),3));
		}
		waves.add(testWave2);

		wave wave2 = new wave(entities);
		for(int i=0;i<20;i++) {
			if (rand.nextBoolean()) {
				wave2.add(AlienFactory.newAlien(rand.nextInt(400)+rand.nextInt(400),rand.nextInt(1000)+1300,rand.nextInt(2)+1));
			}else {
				wave2.add(AlienFactory.newAlien(rand.nextInt(400)+rand.nextInt(400),rand.nextInt(1000)+1300,rand.nextInt(3)+1));
			}
		}

		for(int i=0;i<5;i++) {
			wave2.add(AlienFactory.newAlien(rand.nextInt(900),rand.nextInt(500)+2500,3));
		}
		waves.add(wave2);

		wave waveSnake = new wave(entities);
		for(int i=0;i<5;i++) {
			waveSnake.add(AlienFactory.newAlien(400,1950+i*25,1));
		}
		for(int i=0;i<5;i++) {
			waveSnake.add(AlienFactory.newAlien(800,2350+i*25,2));
		}
		for(int i=0;i<5;i++) {
			waveSnake.add(AlienFactory.newAlien(550,2950+i*25,3));
		}
		waves.add(waveSnake);

		wave wave3 = new wave(entities);
		centerX = 600;
		centerY = 900;
		for(int i=0;i<15;i++) {
			wave3.add(AlienFactory.newAlien((int)rand.nextGaussian(centerX, 50),(int)rand.nextGaussian(centerY, 70)));
		}
		centerX = 300;
		centerY = 1400;
		for(int i=0;i<15;i++) {
			wave3.add(AlienFactory.newAlien((int)rand.nextGaussian(centerX, 50),(int)rand.nextGaussian(centerY, 70)));
		}
		centerX = 500;
		centerY = 2400;
		for(int i=0;i<25;i++) {
			wave3.add(AlienFactory.newAlien((int)rand.nextGaussian(centerX, 90),(int)rand.nextGaussian(centerY, 90)));
		}
		wave3.add(AlienFactory.newAlien(500,900,3));
		wave3.add(AlienFactory.newAlien(246,700,3));

		wave3.add(AlienFactory.newAlien(500,1900,3));
		wave3.add(AlienFactory.newAlien(246,1700,3));

		wave3.add(AlienFactory.newAlien(500,2900,3));
		wave3.add(AlienFactory.newAlien(246,2700,3));
		waves.add(wave3);


		wavesIterator = waves.iterator();
		nextWave();
	}

	public void nextWave() {
		currentWave = wavesIterator.next();
		currentWave.startWave();
	}

	public void setGraphics() {
		G = getGraphics();
		System.out.print("screenbuffer is null: ");
		System.out.println(screenBuffer);
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
					System.out.println("wave defeated");
					nextWave();
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

public class userInterface {
	userInterface() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		myPanel panel = new myPanel();
		frame.addKeyListener(panel);
		frame.getContentPane().add(panel);
		frame.setSize(1000,1000);
		frame.setVisible(true);

		panel.setGraphics();
		while (true) {
			panel.redraw();
		}
	}
}