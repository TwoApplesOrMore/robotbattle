package Dictators.Model;

import Dictators.Attributes.Teammate;
import Dictators.Attributes.Teams;
import populations.EnemyBot;
import robocode.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class Dictator extends TeamRobot {
    int messages = 1;
    private ArrayList<Teammate> teammates = new ArrayList<>();
    Teams team;
    private EnemyBot enemy = new EnemyBot();
    private byte moveDirection = 1;

    private int wallMargin = 45;
    private int tooCloseToWall = 0;


    int x,y;

    /**
     * Run method executes the start of the battle and robot behaviour
     */
    public void run() {

      initiateColorScheme();


        teammates.add(new Teammate(this.getName(), this.getX(), this.getY()));
        Serializable message = this.getName() +";"+ this.getX() + ";"+ this.getY();
        broadcastMessage(message);

        enemy.reset();

        setAdjustRadarForRobotTurn(true);//keep the radar still while we turn
        setAdjustGunForRobotTurn(true); // Keep the gun still when we turn
        turnRadarRightRadians(Double.POSITIVE_INFINITY);//keep turning radar right
    }

    /**
     * onScannedRobot:  What to do when you see another robot
     */
    public void onScannedRobot(ScannedRobotEvent e) {

        // Don't fire on teammates
        if (isTeammate(e.getName())) {
            return;
        }

        // Keeps track of enemy bot currently being scanned
        if (enemy.none() || e.getDistance() < enemy.getDistance() || e.getName().equals(enemy.getName())) {
            enemy.update(e);
        }

        double absBearing = e.getBearingRadians() + getHeadingRadians();//enemies absolute bearing
        double latVel = e.getVelocity() * Math.sin(e.getHeadingRadians() - absBearing);//enemies later velocity
        double gunTurnAmt;//amount to turn our gun
        setTurnRadarLeftRadians(getRadarTurnRemainingRadians());//lock on the radar

        if (Math.random() > .9) {
            setMaxVelocity((12 * Math.random()) + 12);//randomly change speed
        }
        if (e.getDistance() > 150) {//if distance is greater than 150
            gunTurnAmt = robocode.util.Utils.normalRelativeAngle(absBearing - getGunHeadingRadians() + latVel / 22);//amount to turn our gun, lead just a little bit
            setTurnGunRightRadians(gunTurnAmt); //turn our gun
            setTurnRightRadians(robocode.util.Utils.normalRelativeAngle(absBearing - getHeadingRadians() + latVel / getVelocity()));//drive towards the enemies predicted future location
            setAhead((e.getDistance() - 140) * moveDirection);//move forward
            setFire(3);//fire
        } else {//if we are close enough...
            gunTurnAmt = robocode.util.Utils.normalRelativeAngle(absBearing - getGunHeadingRadians() + latVel / 15);//amount to turn our gun, lead just a little bit
            setTurnGunRightRadians(gunTurnAmt);//turn our gun
            setTurnLeft(-90 - e.getBearing()); //turn perpendicular to the enemy
            setAhead((e.getDistance() - 140) * moveDirection);//move forward
            setFire(3);//fire
        }
    }

    public void checkWallDistance() {
        if (closeToWallConditions()) {
            doMove();
        }
    }

    public boolean closeToWallConditions() {
        return ((getX() <= wallMargin || getX() >= getBattleFieldWidth() - wallMargin ||
                getY() <= wallMargin || getY() >= getBattleFieldHeight() - wallMargin)
        );
    }

    public void doMove() {
        if (getVelocity() == 0)
            moveDirection *= -1;

        // circle our enemy
        setTurnRight(enemy.getBearing() + 90);
        setAhead(100 * moveDirection);

    }

    @Override
    public void onHitWall(HitWallEvent h) {
        setTurnLeft(-90 - enemy.getBearing()); //turn perpendicular to the enemy
        turnLeft(45);
        doMove();
    }

    @Override
    public void onHitByBullet(HitByBulletEvent h) {
        setTurnLeft(-90 - enemy.getBearing()); //turn perpendicular to the enemy
        setAhead(enemy.getDistance());
    }

    private int randomPosition(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    @Override
    public void broadcastMessage(Serializable message) {
        try {
            super.broadcastMessage(message);
        } catch (IOException IOE){}
    }

    @Override
    public void onMessageReceived(MessageEvent event) {
        String message = event.getMessage().toString();
        String[] messagesplit = message.split(";");
        String name = messagesplit[0];
        Double x = Double.parseDouble(messagesplit[1]);
        Double y = Double.parseDouble(messagesplit[2]);
        teammates.add(new Teammate(name, x, y));
        if(messages == 3) {
            team = new Teams(teammates, this.getName());
        }
        messages++;
    }

}
