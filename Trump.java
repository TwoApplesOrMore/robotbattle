package Dictators;

/**
 * Created by Ryhazerus on 2/16/2017.
 */

import populations.EnemyBot;
import robocode.*;
import sampleteam.*;

import java.awt.*;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class Trump extends TeamRobot {
    int messages = 1;
    private ArrayList<Teammate> teammates = new ArrayList<>();
    Teammate teammate;
    boolean leader;

    private EnemyBot enemy = new EnemyBot();
    private byte moveDirection = 1;

    /**
     * Run method executes the start of the battle and robot behaviour
     */
    public void run() {
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

    public void doMove() {
        if (getVelocity() == 0)
            moveDirection *= -1;

        // circle our enemy
        setTurnRight(enemy.getBearing() + 90);
        setAhead(1000 * moveDirection);

    }

    @Override
    public void onHitWall(HitWallEvent h) {
        moveDirection -= moveDirection;
    }

    @Override
    public void onHitByBullet(HitByBulletEvent h) {
        doMove();
    }

    private int randomPosition(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

        /**
         * Set the body color scheme for our robots and the ones in our teammate
         * @return the color object to be passed to our other teammates
         */
    private RobotColors initiateColorScheme() {

        RobotColors c = new RobotColors();

        c.bodyColor = Color.black;
        c.gunColor = Color.gray;
        c.radarColor = Color.red;
        c.scanColor = Color.red;
        c.bulletColor = Color.black;

        setBodyColor(c.bodyColor);
        setGunColor(c.gunColor);
        setRadarColor(c.radarColor);
        setScanColor(c.scanColor);
        setBulletColor(c.bulletColor);

        return c;
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
            createTeam();
        }
        messages++;
    }

    public void createTeam(){
        int[] furthestindexes;
        //Bots that are furthest away.
        Teammate[] leaders = getleaders();
        Teammate[] slaves = getslaves(leaders);
        Teammate[][] teams = teamup(leaders, slaves);
        getstatus(teams);
        for (Teammate[] team : teams) {
            System.out.println("Team");
            for (Teammate teammate: team) {
                System.out.println(teammate.getName());
            }
        }
        System.out.println("Leader" + leader);
        System.out.println("Teammate" + teammate.getName());
    }

    public Teammate[] getleaders(){
        //Bots that are furthest away.
        Teammate[] furthest = {new Teammate(null, 0.00, 0.00), new Teammate(null, 0.00, 0.00)};
        //The distance that the bots above are.
        double furthestdistance = 0;
        //Loop through all the bots and calculate how far away they are, using the Pythagorean theorem.
        for (Teammate bot: teammates) {
            for (Teammate otherbot: teammates) {
                if(! bot.getName().equals(otherbot.getName())) {
                    double deltaX = bot.getX() - otherbot.getX();
                    double deltaY = bot.getY() - otherbot.getY();

                    double a = Math.pow(deltaX, 2);
                    double b = Math.pow(deltaY, 2);
                    double c = Math.sqrt(a + b);

                    if (c > furthestdistance) {
                        furthest[0] = bot;
                        furthest[1] = otherbot;
                        furthestdistance = c;
                    }
                }
            }
        }
        return furthest;
    }

    public Teammate[] getslaves(Teammate[] leaders){
        Teammate[] slaves = {new Teammate(null, 0.00, 0.00), new Teammate(null, 0.00, 0.00)};

        int slave = 0;
        for (Teammate mate: teammates) {
            if (! mate.getName().equals(leaders[0].getName()) && ! mate.getName().equals(leaders[1].getName())) {
                slaves[slave] = mate;
                slave++;
            }
        }
        return slaves;
    }

    public Teammate[][] teamup(Teammate[] leaders, Teammate[] slaves){
        double[] distances = {0.00, 0.00, 0.00, 0.00};
        int distance = 0;
        for (Teammate leader: leaders) {
            for (Teammate slave: slaves) {

                double deltaX = slave.getX() - leader.getX();
                double deltaY = slave.getY() - leader.getY();

                double a = Math.pow(deltaX, 2);
                double b = Math.pow(deltaY, 2);
                double c = Math.sqrt(a + b);

                distances[distance] = c;
                distance++;
            }
        }

        double team1 = distances[0] + distances[3];
        double team2 = distances[1] + distances[2];

        Teammate[][] team = {
                {new Teammate(null, 0, 0), new Teammate(null, 0, 0)},
                {new Teammate(null, 0, 0), new Teammate(null, 0, 0)}
        };
        if(team1 < team2){
            team[0][0] = leaders[0];
            team[0][1] = slaves[0];
            team[1][0] = leaders[1];
            team[1][1] =  slaves[1];
        } else {
            team[0][0] = leaders[1];
            team[0][1] = slaves[0];
            team[1][0] = leaders[0];
            team[1][1] =  slaves[1];
        }
        return team;
    }
    public void getstatus(Teammate[][] teams){
        int teamcode = 0;
        for (Teammate[] team : teams) {
            int teammatecode = 0;
            for (Teammate teammate: team) {
                if(teammate.getName().equals(this.getName())){
                    if(teamcode == 0 && teammatecode == 0){
                        leader = true;
                        this.teammate = teams[0][1];
                    } else if(teamcode == 0 && teammatecode == 1) {
                        leader = false;
                        this.teammate = teams[0][0];
                    } else if(teamcode == 1 && teammatecode == 0) {
                        leader = true;
                        this.teammate = teams[1][1];
                    } else if(teamcode == 1 && teammatecode == 1) {
                        leader = false;
                        this.teammate = teams[1][0];
                    }
                } teammatecode++;
            } teamcode++;
        }
    }


}