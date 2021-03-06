package org.ttc.core.game;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import static java.lang.Math.*;
import java.util.ArrayList;
import javax.swing.Timer;
import org.apache.commons.lang.SerializationUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.ttc.client.Help;
import static org.ttc.client.Help.border;
import org.ttc.client.V2;

/**
 *
 * @author yew_mentzaki & whizzpered
 */
public class Room {

    ArrayList<Unit> units = new ArrayList<Unit>();
    ArrayList<Bullet> bullets = new ArrayList<Bullet>();
    Base[] bases = new Base[17];
    public Player[] players = new Player[9];
    public static Image grass;
    public int player;
    int serialnumber;
    int victoryStatus = 0;

    public Unit[] units() {
        Unit[] u = new Unit[units.size()];
        try {
            for (int i = 0; i < units.size(); i++) {
                u[i] = units.get(i);
            }
        } catch (Exception e) {
        }
        return u;
    }

    public Bullet[] bullets() {
        Bullet[] b = new Bullet[bullets.size()];
        try {
            for (int i = 0; i < bullets.size(); i++) {
                b[i] = bullets.get(i);
            }
        } catch (Exception e) {
        }
        return b;
    }

    public byte[] getUnit(int index) {
        return SerializationUtils.serialize(units()[index]);
    }

    public boolean setUnit(int index, byte[] bytes) {
        Unit unit = (Unit) SerializationUtils.deserialize(bytes);
        Unit u = units()[index];
        u.x = unit.x;
        u.y = unit.y;
        u.tx = unit.tx;
        u.ty = unit.ty;
        u.a = unit.a;
        u.ta = unit.ta;
        u.ha = unit.ha;
        u.hta = unit.hta;
        u.type = unit.type;
        u.reload = unit.reload;
        u.ammo = unit.ammo;
        u.hp = unit.hp;
        u.timer = unit.timer;
        u.ai = unit.ai;
        u.lagg = 0;
        return true;

    }

    public void setColors() {
        String colorExamples[] = new String[]{
            "#ff0034",
            "#ee9900",
            "#f6ef00",
            "#4cbb17",
            "#33cbff",
            "#0000ff",
            "#ff5daf",
            "#904602",
            "#CD00CD",
            "#00A85F",
        };

        for (int i = 1; i < players.length; i++) {
            if (players[i] == null) {
                continue;
            }
            players[i].color = Color.decode(colorExamples[Unit.r.nextInt(10)]);
            while (ic(i)) {
                players[i].color = Color.decode(colorExamples[Unit.r.nextInt(10)]);
            }
        }
        this.players[8].color = new Color(128, 128, 128);
    }

    private boolean ic(int i) {
        for (int j = 0; j < players.length; j++) {
            if (i == j || players[j] == null) {
                continue;
            }
            if (players[j].color.r == players[i].color.r && players[j].color.g == players[i].color.g && players[j].color.b == players[i].color.b) {
                return true;
            }
        }
        return false;
    }

    public Room(int players) {
        for (int i = 0; i < players; i++) {
            this.players[i] = new Player("none", 0);
        }
        this.players[8] = new Player("neutral", 0);

        for (int i = 0; i < 8; i++) {
            bases[i] = new Base(this, (int) (cos((float) i / 4 * Math.PI) * 1000), (int) (sin((float) i / 4 * Math.PI) * 1000), this.players[i / (8 / players)]);
            bases[i + 8] = new Base(this, (int) (cos((float) i / 4 * Math.PI + (Math.PI / 8)) * 550), (int) (sin((float) i / 4 * Math.PI + (Math.PI / 8)) * 550), this.players[i / (8 / players)]);
        }
        bases[16] = new Base(this, (int) (0), (int) (0), this.players[8]);
        for (int i = 0; i < 17; i++) {
            for (int j = 0; j < 2; j++) {
                Unit unit = new Unit(this, 0, 0, bases[i]);
                units.add(unit);
                if (j == 0) {
                    unit.base.owner.camerax = (int) (unit.x);
                    unit.base.owner.cameray = (int) (unit.y);
                }
            }
        }
        t.start();
    }
    public Timer t = new Timer(15, new ActionListener() {

        public void actionPerformed(ActionEvent e) {
            int playerBases = 0;
            for (Base base : bases) {
                base.tick();
                if (base.owner == players[player]) {
                    playerBases++;
                }
            }
            if (playerBases == 0) {
                victoryStatus = 2;
            }
            if (playerBases == 17) {
                victoryStatus = 1;
            }
            for (Unit unit : units()) {
                unit.tick();
            }
            for (Bullet bullet : bullets()) {
                bullet.tick();

            }
            for (Bullet bullet : bullets()) {
                if (bullet.t >= 8 | bullet.t2 <= 0) {
                    bullets.remove(bullet);
                }
            }
        }
    });

    public Order[] getOrders() {
        Order[] order = new Order[selected.size()];
        for (int i = 0; i < selected.size(); i++) {
            Unit u = selected.get(i);
            order[i] = new Order(units.indexOf(u), (int) u.tx, (int) u.ty, (int) u.x, (int) u.y, (double) u.a, (double) u.ha);
        }
        return order;
    }

    public void setOrders(Order[] orders) {
        for (Order o : orders) {
            Unit u = units()[o.uid];
            u.tx = o.tx;
            u.ty = o.ty;
            /*
             u.x = o.x;
             u.y = o.y;
             */
            u.a = o.a;
            u.ha = o.ha;
        }
    }

    private Graphics g = new Graphics();
    private ArrayList<Unit> selected = new ArrayList<Unit>();

    public void render(int player) {
        if (t.isRunning()) {
            if (Keyboard.isKeyDown(Keyboard.KEY_LEFT) || Keyboard.isKeyDown(Keyboard.KEY_A)) {
                players[player].camerax -= 5;
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT) || Keyboard.isKeyDown(Keyboard.KEY_D)) {
                players[player].camerax += 5;
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_UP) || Keyboard.isKeyDown(Keyboard.KEY_W)) {
                players[player].cameray -= 5;
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_DOWN) || Keyboard.isKeyDown(Keyboard.KEY_S)) {
                players[player].cameray += 5;
            }
        }
        {
            double dist = Math.sqrt(Math.pow(players[player].camerax, 2) + Math.pow(players[player].cameray, 2));
            if (dist > 1000) {
                double a = Math.atan2(-players[player].cameray, -players[player].camerax);
                players[player].camerax += (dist - 1000) * Math.cos(a);
                players[player].cameray += (dist - 1000) * Math.sin(a);
            }
        }
        int cx = players[player].camerax - Display.getWidth() / 2;
        int cy = players[player].cameray - Display.getHeight() / 2;

        try {

            for (int x = cx / 128 - 1; x <= cx / 128 + Display.getWidth() / 128 + 1; x += 1) {
                for (int y = cy / 128 - 1; y <= cy / 128 + Display.getHeight() / 128 + 1; y += 1) {
                    grass.draw(x * 128 - cx, y * 128 - cy);
                }
            }
            GL11.glTranslated(-cx, -cy, 0);

            for (Base base : bases) {
                base.render(g);
            }
            if (Mouse.isButtonDown(1) && t.isRunning()) {
                players[player].camerax -= Mouse.getDX();
                players[player].cameray += Mouse.getDY();
            }
            if (!Mouse.isButtonDown(0)) {
                selected.clear();
            }
            int mx = cx + Mouse.getX();
            int my = cy + Display.getHeight() - Mouse.getY();
            if (t.isRunning()) {
                if (Mouse.getX() < 25) {
                    players[player].camerax -= 5;
                } else if (Mouse.getX() > Display.getWidth() - 25) {
                    players[player].camerax += 5;
                }
                if (Mouse.getY() < 25) {
                    players[player].cameray += 5;
                } else if (Mouse.getY() > Display.getHeight() - 25) {
                    players[player].cameray -= 5;
                }
            }
            for (Unit unit : units()) {
                unit.render(g);
                if (unit.owner == players[player] && Mouse.isButtonDown(0) && !selected.contains(unit)) {

                    if (sqrt(pow(unit.x - mx, 2) + pow(unit.y - my, 2)) < 100) {
                        selected.add(unit);
                    }
                }
                if (unit.timer < 10 && selected.contains(unit)) {
                    selected.remove(unit);
                }
            }
            if (t.isRunning()) {
                for (Unit unit : selected) {
                    g.setColor(Color.yellow);
                    g.drawLine(mx, my, (int) unit.x, (int) unit.y);

                    unit.tx = mx;
                    unit.ty = my;
                }
            }
            for (Bullet bullet : bullets()) {
                if (bullet != null) {
                    bullet.render(g);
                }
            }

            GL11.glTranslated(cx, cy, 0);
            GL11.glLoadIdentity();
            if (!t.isRunning()) {
                return;
            }
            for (Base base : bases) {
                base.renderInterface(g);
            }
            for (Unit unit : units()) {
                unit.renderInterface(g);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        g.setColor(Color.black);
        g.drawRect(cx / 20 + 75, cy / 20 + 75, Display.getWidth() / 20, Display.getHeight() / 20);
        for (int i = 0; i < 5; i++) {
            Unit.iconImage[i].draw(10, 170 + i * 80, 70, 70);
            if ((Mouse.isButtonDown(0) && sqrt(pow((45) - (Mouse.getX()), 2) + pow((170 + i * 80 + 35) - (Display.getHeight() - Mouse.getY()), 2)) < 35) | Keyboard.isKeyDown(Keyboard.KEY_1 + i)) {
                players[player].mode = i;
            }
        }
        modeSwitch = (modeSwitch * 10 + 170 + players[player].mode * 80) / 11;
        Unit.iconImage[5].draw(80, (int) modeSwitch, 70, 70);
        Help.render(g);
        Unit.victory_back.setImageColor(players[player].color.r, players[player].color.g, players[player].color.b, 1);
        if (victoryStatus == 1) {
            Unit.victory_back.draw((Display.getWidth() - Unit.victory_back.getWidth()) / 2, (Display.getHeight() - Unit.victory_back.getHeight()) / 2);
            Unit.victory.draw((Display.getWidth() - Unit.victory.getWidth()) / 2, (Display.getHeight() - Unit.victory.getHeight()) / 2);
            border.draw(0, Display.getHeight() - 20, Display.getWidth(), 20);
            V2.fontRender.drawString(V2.localeText("space"), Display.getWidth() - V2.fontRender.getWidth(V2.localeText("space")) - 50, Display.getHeight() - 40, Color.white);
        }
        if (victoryStatus == 2) {
            Unit.victory_back.draw((Display.getWidth() - Unit.victory_back.getWidth()) / 2, (Display.getHeight() - Unit.victory_back.getHeight()) / 2);
            Unit.fail.draw((Display.getWidth() - Unit.fail.getWidth()) / 2, (Display.getHeight() - Unit.fail.getHeight()) / 2);
            border.draw(0, Display.getHeight() - 20, Display.getWidth(), 20);
            V2.fontRender.drawString(V2.localeText("space"), Display.getWidth() - V2.fontRender.getWidth(V2.localeText("space")) - 50, Display.getHeight() - 40, Color.white);
        }
        if (victoryStatus > 0 && Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
            t.stop();
            Help.space = false;
        }
    }
    double modeSwitch = 170;
}
