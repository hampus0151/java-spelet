import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class game extends Canvas implements Runnable {
    // Bounding boxes för målet och för musen
    Rectangle target;
    Rectangle striker;
    // Slumpgenerator för att slumpa ut nya mål
    Random R;

    BufferedImage targetImg;
    BufferedImage strikerImg;

    BufferStrategy bs;
    int width = 800;
    int height = 600;
    Thread thread;
    boolean running = false;

    int x = 0;
    Character player;

    public game() {
        R = new Random();
        /* Skapa boundingboxes på lämplig plats*/
        target = new Rectangle(R.nextInt(width - 16), R.nextInt(height - 16), 16, 16);
        striker = new Rectangle(width, height, 16, 16);

        try {
            targetImg = ImageIO.read(new File("Target.png"));
            strikerImg = ImageIO.read(new File("Striker.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        setSize(width, height);
        JFrame frame = new JFrame("Grafik med kollisioner");
        frame.add(this);
        // Lägg till en lyssnare för musen (MouseMotionListener eftersom vi vill kolla när musen rör sig)
        this.addMouseMotionListener(new MML());
        this.addKeyListener(new KML());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Dölj muspekaren genom att ersätta den med en transparant bild
        frame.setCursor(frame.getToolkit().createCustomCursor(
                new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB), new Point(0, 0),
                "null"));

        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Start, stop och run är metoder som kommer från Runnable. Det gör att vi kan starta en ny processortråd
     * som kör runmetoden där vi kan rita upp skärmen
     */
    public synchronized void start() {
        running = true;
        thread = new Thread(this);
        thread.start();
    }


    public synchronized void stop() {
        running = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /* Eftersom vi vill ta kontroll över hur snabbt vår animering updateras bestämmer vi en tid mellan varje uppdatering
       Tiden mellan två uppdateringar blir 1 /30 sekund (30 ups eller fps). Delta anger i hur nära vi är en ny uppdatering.
       När delta blir 1 är det dags att rita igen. delta nollställs inte eftersom det kan hända att något tagit lång tid
       och att vi måste göra flera uppdateringar efter varandra.
       Här ligger update och render i samma tidssteg. Det går att separera dessa. Egentligen kan vi rita ut hur fort som
       helst (lägga render utanför while(delta>1)) Det viktiga är att update anropas med konstant hastighet eftersom det
       är den som simulerar tiden i animeringar.
     */
    public void run() {
        double ns = 1000000000.0 / 30.0;
        double delta = 0;
        long lastTime = System.nanoTime();

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;

            while (delta >= 1) {
                // Uppdatera koordinaterna
                update();
                // Rita ut bilden med updaterad data
                render();
                delta--;
            }
        }
        stop();
    }

    /**
     * Eftersom vi inte längre behöver paint och repaint döper jag om metoden till render
     */
    public void render() {
        bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }
        Graphics g = bs.getDrawGraphics();
        // Rita ut den nya bilden
        draw(g);
        g.dispose();
        bs.show();
    }

    public void draw(Graphics g) {
        // rensa skärmen
        g.setColor(new Color(0xFFFFFF));
        g.fillRect(0, 0, width, height);
        // Rita ut biolderna men använd motsvarande rektangel för att få placering och storlek
        g.drawImage(targetImg, target.x, target.y, target.width, target.height, null);
        g.drawImage(strikerImg, striker.x, striker.y, striker.width, striker.height, null);
    }

    // Uppdatera inget i detta exempel. target skulle kunna röra sig...
    private void update() {
    }

    /* Tydligen krockar mouselistener-eventen med awt-komponenterna i guit
       Lösningen (och god sed) är att starta guit på följande sätt
     */
    public static void main(String[] args) {
        game minGrafik = new game();
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                minGrafik.setVisible(true);
            }
        });
        minGrafik.start();
    }


    //keylistner lyssnar efter tangenterna trycks
    private class KML implements KeyListener {


        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {

            int key = e.getKeyCode();
            if(key==39){
                player.moveRight = true;
            }
            if(key==39) {
                player.moveLeft = true;
            }


            int x = e.getX();
            int y = e.getY();
        }

        @Override
        public void keyReleased(KeyEvent e) {

        }
    }
}