package Dictators.Attributes;

import java.util.ArrayList;

/**
 * Created by Thomas on 02-03-17.
 */
public class Teams {
    String robotname;
    private ArrayList<Teammate> teammates;
    private Teammate teammate;
    boolean leader = false;
    Teammate[] leaders;
    Teammate[] slaves;
    Teammate[][] teams;

    public Teams(ArrayList<Teammate> teammates, String robotname) {
        this.robotname = robotname;
        this.teammates = teammates;
        createTeam();
    }

    public Teammate getTeammate() {
        return teammate;
    }

    public boolean isLeader() {
        return leader;
    }

    public void createTeam(){
        leaders = getleaders();
        slaves = getslaves(leaders);
        teams = teamup(leaders, slaves);
        getstatus();
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
    public void getstatus(){
        int teamcode = 0;
        for (Teammate[] team : teams) {
            int teammatecode = 0;
            for (Teammate teammate: team) {
                if(teammate.getName().equals(robotname)){
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
