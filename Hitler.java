package Dictators;

/**
 * Created by Ryhazerus on 2/16/2017.
 */

import populations.EnemyBot;
import robocode.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class Hitler extends TeamRobot {
    int messages = 1;
    private ArrayList<Teammate> teammates = new ArrayList<>();
    Teammate teammate;
    boolean leader;

    // Enemy bot currently scanned
    private EnemyBot enemy = new EnemyBot();

    // The movement position
    private byte moveDirection = 1;

    public void run() {
        teammates.add(new Teammate(this.getName(), this.getX(), this.getY()));
        Serializable message = this.getName() +";"+ this.getX() + ";"+ this.getY();
        broadcastMessage(message);

        enemy.reset();

        //keep the radar still while we turn
        setAdjustRadarForRobotTurn(true);

        // Keep the gun still when we turn
        setAdjustGunForRobotTurn(true);

        //keep turning radar right
        turnRadarRightRadians(Double.POSITIVE_INFINITY);
    }

    /**
     * onScannedRobot:  What to do when we see another robot
     */
    public void onScannedRobot(ScannedRobotEvent e) {

        // Check if Scanned robots are Teammates, if they are don't fire
        if (isTeammate(e.getName())) {
            return;
        }

        // Keeps track of enemy bot currently being scanned
        if (enemy.none() || e.getDistance() < enemy.getDistance() || e.getName().equals(enemy.getName())) {
            enemy.update(e);
        }

        // Enemies absolute bearing
        double absBearing = e.getBearingRadians() + getHeadingRadians();
        // Enemies later velocity
        double latVel = e.getVelocity() * Math.sin(e.getHeadingRadians() - absBearing);
        // Amount to turn our gun
        double gunTurnAmt;
        // Lock on the radar
        setTurnRadarLeftRadians(getRadarTurnRemainingRadians());

        // Randomly change speed
        if (Math.random() > .9) {
            setMaxVelocity((12 * Math.random()) + 12);
        }

        // If distance is greater than 150
        if (e.getDistance() > 150) {
            // Amount to turn our gun, lead just a little bit
            gunTurnAmt = robocode.util.Utils.normalRelativeAngle(absBearing - getGunHeadingRadians() + latVel / 22);
            // Turn our gun
            setTurnGunRightRadians(gunTurnAmt);
            //drive towards the enemies predicted future location
            setTurnRightRadians(robocode.util.Utils.normalRelativeAngle(absBearing - getHeadingRadians() + latVel / getVelocity()));
            // Move forward in enemy direction
            setAhead((e.getDistance() - 140) * moveDirection);
            //fire with firepower 3
            // TODO: Make firepower dynamic based on location
            setFire(3);
        } else {// Else if we are close enough (under 150)
            // Amount to turn our gun, lead just a little bit
            gunTurnAmt = robocode.util.Utils.normalRelativeAngle(absBearing - getGunHeadingRadians() + latVel / 15);
            // Turn our gun
            setTurnGunRightRadians(gunTurnAmt);
            // Turn perpendicular to the enemy
            setTurnLeft(-90 - e.getBearing());
            // Move forward
            setAhead((e.getDistance() - 140) * moveDirection);
            // Fire with firepower 3
            // TODO: Make firepower dynamic based on location
            setFire(3);
        }
    }

    /**
     * Moves randomly in circular motion surrounding enemy
     * TODO: Does not circulate enemy, jsut does circles
     * TODO: FIX IT
     */
    private void doMove() {

        // Check if we are not standing still
        if (getVelocity() == 0) {
            moveDirection *= -1;
        }

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
