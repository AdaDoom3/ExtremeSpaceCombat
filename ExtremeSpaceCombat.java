import java.util.*;
import java.io.File;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;

import javax.swing.*;
import javax.sound.sampled.*;
import javax.imageio.ImageIO;
public class ExtremeSpaceCombat{



// Properties
  public static final String RESOURCE_NAME = "Settings";
  public static String getConstantString(String id){
    return ResourceBundle.getBundle(RESOURCE_NAME).getString(id.toLowerCase());}
  public static int getConstant(String id){return Integer.parseInt(getConstantString(id));}
  public static final int SPEED = getConstant("level.speed");
  public static final int ROWS = getConstant("level.size");
  public static final int COLUMNS = getConstant("level.size");
  public static final int TIME_LIMIT = getConstant("level.time.limit");
  public static final int IMAGE_SIZE = getConstant("level.tile.size");
  public static final int VOLUME_MUSIC = getConstant("level.volume.music");
  public static final int VOLUME_SOUNDS = getConstant("level.volume.sounds");
  public static final String TITLE = getConstantString("level.title");
  public static final String ICON = getConstantString("level.icon");
  public static final String BACKGROUND = getConstantString("level.background");
  public static final String PAUSE_KEY = getConstantString("level.pause");
  public static final String TEXT_VS_WIN = getConstantString("text.vs.win");
  public static final String TEXT_VS_TIE = getConstantString("text.vs.tie");
  public static final String TEXT_COOP_DIE = getConstantString("text.coop.die");
  public static final String TEXT_COOP_WIN = getConstantString("text.coop.win");
  public static final String TEXT_COOP_LOSE = getConstantString("text.coop.lose");
  public static final String DIRECTORY_JAR = getConstantString("directory.jar");
  public static final String DIRECTORY_ART = getConstantString("directory.art");
  public static final String EXTENSION_IMAGE = getConstantString("extension.image");
  public static final String EXTENSION_AUDIO = getConstantString("extension.audio");
  public static final String SOUND_END = getConstantString("sound.end");
  public static final String SOUND_SONG = getConstantString("sound.song");
  public static final String SOUND_SHOOT = getConstantString("sound.shoot");
  public static final String SOUND_START = getConstantString("sound.start");
  public static final String SOUND_EXPLODE_BOMB = getConstantString("sound.explode.bomb");
  public static final String SOUND_EXPLODE_PLAYER = getConstantString("sound.explode.player");
  public static final String SOUND_EXPLODE_BULLET = getConstantString("sound.explode.bullet");



// Direction
  public static final int NORTH = 0;
  public static final int EAST = 90;
  public static final int WEST = 270;
  public static final int SOUTH = 180;
  public static final int AHEAD = 0;
  public static final int LEFT = -90;
  public static final int RIGHT = 90;
  public static final int NORTHEAST = 45;
  public static final int SOUTHEAST = 135;
  public static final int SOUTHWEST = 225;
  public static final int NORTHWEST = 315;
  public static final int HALF_LEFT = -45;
  public static final int HALF_RIGHT = 45;
  public static final int FULL_CIRCLE = 360;
  public static final int HALF_CIRCLE = 180;
  public static int getRandomDirection(){
    switch(new Random().nextInt(4)){
      case 1: return NORTH;
      case 2: return EAST;
      case 3: return WEST;
      case 4: return SOUTH;
    }
    return NORTH; 
  }



// Sound
  public static Clip getSound(String path){
    try{
      Clip sound = (Clip)AudioSystem.getLine(new Line.Info(Clip.class));
      sound.open(AudioSystem.getAudioInputStream(new File(path + EXTENSION_AUDIO)));
      return sound;
    }catch(Exception error){return null;}
  }
  public static void playSound(String path){
    getSound(path).start();
  }



// Location
  public static class Location{
    private int x = 1; public int getX(){return x;}
    private int y = 1; public int getY(){return y;}
    public Location(int x, int y){this.x = x; this.y = y;}
    public double getDistance(Location location){
      return Math.abs(Math.sqrt(
        Math.pow((double)(location.getX() - getX()), 2) +
        Math.pow((double)(location.getY() - getY()), 2)));
    }
    public Location getAdjacent(int direction){
      int x = 0;
      int y = 0;
      int adjustedDirection = (direction + HALF_RIGHT / 2) % FULL_CIRCLE;
      if(adjustedDirection < 0) adjustedDirection += FULL_CIRCLE;
      adjustedDirection = (adjustedDirection / HALF_RIGHT) * HALF_RIGHT;
      switch(adjustedDirection){
        case SOUTHEAST: y =  1; 
        case EAST:      x =  1; break;
        case SOUTHWEST: x = -1;
        case SOUTH:     y =  1; break;
        case NORTHWEST: y = -1; 
        case WEST:      x = -1; break;
        case NORTHEAST: x =  1; 
        case NORTH:     y = -1;}
      return new Location(getX() + x, getY() + y);
    }
    public boolean equals(Location location){
      return location.getX() == getX() && location.getY() == getY();
    }
  }



// Grid
  public static class Grid<T>{
    Object[][] locations;
    public Grid(){
      if(ROWS <= 0 || COLUMNS <= 0) throw new IllegalArgumentException();
      locations = new Object[COLUMNS][ROWS];
    }
    public int getCount(Class object){
      int result = 0;
      for(Object objectb : getAll())
        if(objectb.getClass() == object) result++;
      return result;
    }
	public ArrayList<T> getKind(Class object){
      ArrayList<T> result = new ArrayList<T>();
      for(Object objectb : getAll())
        if(objectb.getClass() == object) result.add((T)objectb);
      return result;
    }
    public Object getInstance(Object object){
      for(Object objectb : getAll())
        if(objectb.getClass() == object.getClass()) return objectb;
      return null;
    }
    public ArrayList<T> getAll(){
      ArrayList<T> result = new ArrayList<T>();
      for(int x = 0;x < ROWS;x++) for(int y = 0;y < COLUMNS;y++)
        if(get(new Location(x, y)) != null) result.add(get(new Location(x, y)));
      return result;
    }
    public boolean isInvalid(Location location){
      if(location.getX() < 0 || location.getX() >= getNumberOfRows() ||
        location.getY() < 0 || location.getY() >= getNumberOfColumns())
        return true;
      return false;
    }
    public Location getRandomEmpty(){
      ArrayList<Location> emptySpots = new ArrayList<Location>();
      for(int x = 0;x < ROWS; x++) for(int y = 0;y < COLUMNS; y++)
        try{
          if(get(new Location(x, y)) == null) emptySpots.add(new Location(x, y));
        }catch(Exception error){}
      if(emptySpots.size() == 0) throw new IllegalArgumentException();
      return emptySpots.get(new Random().nextInt(emptySpots.size()));
    }
    public void add(Location location, T something){
      locations[location.getX()][location.getY()] = something;
    }
    public void remove(Location location) {locations[location.getX()][location.getY()] = null;}
    public T get(Location location) {return (T)locations[location.getX()][location.getY()];}
    public int getNumberOfRows() {return locations.length;}
    public int getNumberOfColumns() {return locations[0].length;}
  }



// Sprite
  public static class Sprite{
    private BufferedImage image; public BufferedImage getImage(){return image;}
    private Location location = null; public Location getLocation(){return location;}
    private Grid<Sprite> level = null; public Grid<Sprite> getGrid(){return level;}
    private int direction = 0; public int getDirection(){return direction;}
    public Color color;
    private int health =-1; public int getHealth(){return health;}
    private int h;
    private int duration = 0;
    public void die(){}
    public void act(){}
    public void fade(int duration){
      this.duration = duration;
    }
    public void damage(int amount){
      if(health > 0){
        health = health - amount;
        if(health <= 0) remove();
      }
    }
    public void setInvincible(boolean value){
      if(value){
        h = health;
        health = -1;
      }else health = h;
    }
    public boolean isInvincible(){
      return getGrid() != null && health <= 0;
    }
    public void setHealth(int amount){
      if(amount < 0 || isInvincible()) throw new IllegalArgumentException();
      health = amount;
    }
    public void step(){
      if(duration > 0){
        duration--; 
        if(duration == 0) remove();
      }
      act();
    }
    public void setDirection(int direction){
      this.direction = direction % FULL_CIRCLE;
      if(direction < 0) this.direction += FULL_CIRCLE;
    }
    public void remove(){
      if(getGrid() != null && getGrid().get(getLocation()) != this)
        throw new IllegalStateException();
      getGrid().remove(getLocation());
      die();
      level = null;
      location = null;
    }
    public int getDirectionToward(Location target){
      int dx = target.getX() - getLocation().getX();
      int dy = target.getY() - getLocation().getY();
      int angle = (int)Math.toDegrees(Math.atan2(-dy, dx));
      int compassAngle = RIGHT - angle;
      compassAngle += HALF_RIGHT / 2;
      if(compassAngle < 0) compassAngle += FULL_CIRCLE;
      return (compassAngle / HALF_RIGHT) * HALF_RIGHT;
    }
    public void move(Location location){
      if(getGrid() == null || getGrid().isInvalid(location)) throw new IllegalStateException();
      getGrid().remove(getLocation());
      if(getGrid().get(location) != null) getGrid().get(location).remove();
      put(getGrid(), location);
      this.location = location;
    }
    public void put(Grid<Sprite> level, Location location){
      if(level != null && level.isInvalid(location)) throw new IllegalStateException();
      Sprite sprite = level.get(location);
      if(sprite != null) sprite.remove();
      level.add(location, this);
      this.location = location;
      this.level = level;
      if(image == null) loadImage();
    }
    private void loadImage(){
      try{
        this.image = ImageIO.read(new File(getClass().getSimpleName().toLowerCase() + EXTENSION_IMAGE));
      }catch(Exception error){
        try{
          this.image = ImageIO.read(new File(getClass().getName().toLowerCase() + EXTENSION_IMAGE));
        }catch(Exception error2){}
      }
    }
    public void put(Grid<Sprite> level){
      put(level, level.getRandomEmpty());
    }
    public Location getAdjacent(int direction){
      Location result = getLocation().getAdjacent(direction);
      if(getGrid().isInvalid(result)) throw new IllegalStateException();
      return result;
    }
    public Location getAdjacent(){
      return getAdjacent(getDirection());
    }
    public boolean canMove(Location location){
      if(getGrid().get(location) != null || getGrid().isInvalid(location)) return false;
      return true;
    }
  }



// Player
  public static class Player extends Sprite{
    public final int START_HEALTH = getConstant("player.health");
    private ArrayList<String> moveKeys = new ArrayList<String>();
    private ArrayList<Boolean> state = new ArrayList<Boolean>();
    private String shootKey;
    private String previous;
    private KeyEventDispatcher dispatcher;
    private int id; public int getID(){return id;}
    public Player(){}
    public Player(int id){
      this.id = id;
      moveKeys.add(getConstantString("player." + id + ".up").toUpperCase()); state.add(false);
      moveKeys.add(getConstantString("player." + id + ".right").toUpperCase()); state.add(false);
      moveKeys.add(getConstantString("player." + id + ".down").toUpperCase()); state.add(false);
      moveKeys.add(getConstantString("player." + id + ".left").toUpperCase()); state.add(false);
      shootKey = getConstantString("player." + id + ".shoot").toUpperCase(); state.add(false);
      setHealth(START_HEALTH);
      try{
        color = (Color)Color.class.getField(getConstantString(
          "player." + id + ".color").toUpperCase()).get(null);
      }catch(Exception error){}
      dispatcher = new KeyEventDispatcher(){
        public boolean dispatchKeyEvent(KeyEvent event){
          String key = KeyStroke.getKeyStrokeForEvent(event).toString();
          if(!key.equals("pressed " + previous))
          for(int i,k = i = 0;i < moveKeys.size();i++){
            if(key.equals("pressed " + moveKeys.get(i))){
              if(!state.get(i)){
                state.set(i, true);
                Location next = null;
                try{next = getAdjacent(k);}catch(Exception error){}
                if(next != null && !getGrid().isInvalid(next) &&
                !(getGrid().get(next) instanceof Sprite)) move(next);
                setDirection(k);
              }
            }else if(key.equals("released " + moveKeys.get(i))) state.set(i, false);
            k = (RIGHT + k) % FULL_CIRCLE;
          }
          if(key.equals("pressed " + shootKey)){
            if(!state.get(4)){ 
              state.set(4, true);
              try{
                Location location = getAdjacent();
                if(!(getGrid().get(location) instanceof Sprite) && !getGrid().isInvalid(location))
                  new Bullet(getDirection()).put(getGrid(), location);
              }catch(Exception error){}
            }
          }else if(key.equals("released " + shootKey)) state.set(4, false);
          return false;
        }
      };
      KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);
    }
    public void die(){
      playSound(SOUND_EXPLODE_PLAYER);
      KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher);
    }
  }



// Bullet
  public static class Bullet extends Sprite{
    public final int BULLET_TIMING = getConstant("bullet.timing");
    public final String BULLET_SHOOT = getConstantString("bullet.shoot");
    private class BulletExplosion extends Sprite{}
    Long lastMove = System.currentTimeMillis() - BULLET_TIMING;
    boolean isFirstTime = true;
    Sprite owner = null; public Sprite getOwner(){return owner;}
    public Bullet(int direction){
      setDirection(direction);
    }
    public void put(Grid<Sprite> level, Location location){
      if(isFirstTime){
        //playSound(BULLET_SHOOT);
        isFirstTime = false;
      }
      super.put(level, location);
      owner = getGrid().get(getAdjacent(HALF_CIRCLE + getDirection()));
    }
    public void explode(){
      BulletExplosion explosion = new BulletExplosion();
      Grid<Sprite> level2 = getGrid();
      Location location = getLocation();
      explosion.setDirection(getDirection());
      remove();
      explosion.put(level2, location);
      explosion.fade(3);
    }
    public void act(){
      if(lastMove + BULLET_TIMING < System.currentTimeMillis()){
        Location nextLocation = null;
        try{
          nextLocation = getAdjacent();
          Sprite sprite = getGrid().get(nextLocation);
          if(sprite instanceof Bullet || sprite instanceof BulletExplosion || sprite == null){
            move(nextLocation);
            lastMove = System.currentTimeMillis();
          }else{
            if(!sprite.isInvincible()){
              playSound(SOUND_EXPLODE_BULLET);
              sprite.damage(1);
            }
            explode();
          }
        }catch(Exception error){explode();}
      }
    }
  }



// Bomb
  public static class Bomb extends Sprite{
    public final int START_HEALTH = getConstant("bomb.health");
    public final int FADE_DURATION = getConstant("bomb.fade");
    private class BombExplosionMiddle extends Sprite{}
    private class BombExplosionCorner extends Sprite{}
    private class BombExplosionSide extends Sprite{}
    public Bomb(){
      setHealth(START_HEALTH);
    }
    public void die(){
      playSound(SOUND_EXPLODE_BOMB);
      for(int i = NORTH;i <= WEST;i += RIGHT){
        BombExplosionSide side = new BombExplosionSide();
        BombExplosionCorner corner = new BombExplosionCorner();
        side.setDirection(i + RIGHT);
        corner.setDirection(i + RIGHT);
        try{side.put(getGrid(), getAdjacent(RIGHT + i));}catch(Exception e){}
        try{corner.put(getGrid(), getAdjacent(HALF_RIGHT + i));}catch(Exception e){}
        side.fade(FADE_DURATION);
        corner.fade(FADE_DURATION);
      }   
      BombExplosionMiddle middle = new BombExplosionMiddle();
      middle.put(getGrid(), getLocation());
      middle.fade(FADE_DURATION);
    }
  }



// Bot
  public static class Bot extends Sprite{
    public final int BOT_TIMING_MOVE = getConstant("bot.timing.move");
    public final int BOT_TIMING_SHOOT = getConstant("bot.timing.shoot");
    public final int START_HEALTH = getConstant("bot.health");
    private boolean canMove = false;
    private boolean canShoot = false;
    private Long currentMove = System.currentTimeMillis() - BOT_TIMING_MOVE;
    private Long currentShoot = System.currentTimeMillis() - BOT_TIMING_SHOOT;
    public Bot(){
      setHealth(START_HEALTH);
    }
    public void die(){
      playSound(SOUND_EXPLODE_PLAYER);
    }
    private boolean attack(){
      Location current;
      int range;
      for(Sprite sprite : getGrid().getKind(Player.class))
        for(int i = (sprite.getDirection() + RIGHT) % FULL_CIRCLE;;i = (i + RIGHT) % FULL_CIRCLE){
          current = sprite.getLocation();
          range = 0;
          try{
            while(true){
              current = current.getAdjacent(i);
              range++;
              if(range > 1 && getLocation().equals(current)){
                if(getDirection() == getDirectionToward(sprite.getLocation())){
                  if(canShoot){
                    new Bullet(getDirection()).put(getGrid(), getAdjacent());
                    return true;
                  }
                }else if(range > 2){
                  if(canMove){
                    setDirection(getDirectionToward(sprite.getLocation()));
                    move(getAdjacent());
                  }
                  return true;
                }
              }else if(getGrid().get(current) != null) throw new Exception();
            }
          }catch(Exception error){}
          if(i == sprite.getDirection()) break;
        }
      return false;
    }
    private boolean moveToAttack(){
      if(canMove){
        double least = ROWS + 0.0;
        Location closest = null;
        Location next = null;
        Location current;
        int range;
        for(Sprite sprite : getGrid().getKind(Player.class))
          for(int i = (sprite.getDirection() + RIGHT) % FULL_CIRCLE;
          i!= sprite.getDirection();i = (i + RIGHT) % FULL_CIRCLE){
            current = sprite.getLocation();
            range = 0;
            try{
              while(true){
                current = current.getAdjacent(i);
                if(getGrid().get(current) != this && getGrid().get(current) != null)
                  throw new Exception();
                range++;
                if(range > 2 && current.getDistance(getLocation()) < least){
                  least = current.getDistance(getLocation());
                  closest = current;
                }
              }
            }catch(Exception error){}
          }
        if(closest != null){
          least = ROWS + 0.0;
          for(int i = NORTH;i != FULL_CIRCLE;i += HALF_RIGHT)
            try{
              if(getGrid().get(getAdjacent(i)) == null && closest.getDistance(getAdjacent(i)) < least){
                least = closest.getDistance(getAdjacent(i));
                next = getAdjacent(i);
              }
            }catch(Exception error){}
          if(next != null){
            if((getDirectionToward(next) % RIGHT) != 0)
              setDirection(getDirectionToward(next) - HALF_RIGHT);
            else setDirection(getDirectionToward(next));
            move(next);
            return true;
          }
        }
      }
      return false;
    }
    public void act(){
      if(currentMove + BOT_TIMING_MOVE < System.currentTimeMillis()){
        canMove = true;
        currentMove = System.currentTimeMillis();
      }
      if(currentShoot + BOT_TIMING_SHOOT < System.currentTimeMillis()){
        canShoot = true;
        currentShoot = System.currentTimeMillis();
      } 
      if(!attack()) moveToAttack();
      canMove = false;
      canShoot = false;
    }
  }



// Shield
  public static class Shield extends Sprite{
    public Shield(){
      setInvincible(true);
      setDirection(getRandomDirection());
    }
  }




// Main
  public static void main(String[] args){
    Grid<Sprite> level = new Grid<Sprite>();
    BufferedImage background = null;
    JLabel label = new JLabel();
    try{background = ImageIO.read(new File(BACKGROUND + EXTENSION_IMAGE));}catch(Exception error){}
    BufferedImage buffer = new BufferedImage(COLUMNS * IMAGE_SIZE, ROWS * IMAGE_SIZE, background.getType());
    Graphics2D graphics = buffer.createGraphics();
    JFrame frame = new JFrame(TITLE);
    frame.setVisible(true);
    Insets insets = frame.getInsets();
    String result = "";
    Long start = System.currentTimeMillis();
    Long current = System.currentTimeMillis() - SPEED;
    frame.setSize(COLUMNS * IMAGE_SIZE + insets.left + insets.right, ROWS * IMAGE_SIZE + insets.top);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setResizable(false);
    try{frame.setIconImage(new ImageIcon(ImageIO.read(new File(ICON + EXTENSION_IMAGE))).getImage());
    }catch(Exception error){}
    for(int i = 1;i <= getConstant("player.count");i++) new Player(i).put(level);
    for(int i = 1;i <= getConstant("shield.count");i++) new Shield().put(level);
    for(int i = 1;i <= getConstant("bomb.count");i++) new Bomb().put(level);
    for(int i = 1;i <= getConstant("bot.count");i++) new Bot().put(level);
    Clip song = getSound(SOUND_SONG);
    int startCount = getConstant("bot.count");
    song.start();
    label = new JLabel(new ImageIcon(buffer));
    frame.add(label);
    while(true)if(current + SPEED < System.currentTimeMillis()){
      if((System.currentTimeMillis() - start) / 1000 > TIME_LIMIT){
        if(level.getCount(Bot.class) > 0) result = TEXT_COOP_LOSE;
        else result = TEXT_VS_TIE;
        break;
      }
      if(startCount > 0 && level.getCount(Bot.class) <= 0){
        result = TEXT_COOP_WIN + (System.currentTimeMillis() - start) / 1000 + " seconds";
        break;
      }
      if(startCount <= 0 && level.getCount(Player.class) < 2){
        Player winner = (Player)level.getInstance(new Player());
        result = TEXT_VS_WIN + winner.getID();
        break;
      }
      if(startCount > 0 && level.getCount(Player.class) <= 0){
        result = TEXT_COOP_DIE;
        break;
      }
      for(Sprite sprite : level.getAll()) try{sprite.step();}catch(Exception error){}
      current = System.currentTimeMillis();
      for(Sprite sprite : level.getAll()){
        if(sprite.color != null) graphics.setXORMode(sprite.color);
        AffineTransform transform = AffineTransform.getTranslateInstance(
          sprite.getLocation().getX() * IMAGE_SIZE,
          sprite.getLocation().getY() * IMAGE_SIZE);
        transform.rotate(Math.toRadians(sprite.getDirection()), IMAGE_SIZE / 2.0, IMAGE_SIZE / 2.0);
        graphics.drawImage(sprite.getImage(), transform, null);
        if(sprite.color != null) graphics.setPaintMode();
      }
      label.repaint(0, 0, 0, frame.getWidth(), frame.getHeight());
      frame.setVisible(true);
      graphics.drawImage(background, 0, 0, null);
    }
    song.stop();
    playSound(SOUND_END);
    JOptionPane.showMessageDialog(null, result, TITLE, JOptionPane.PLAIN_MESSAGE, null);
    frame.dispose();
  }
}
