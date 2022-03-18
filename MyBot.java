import hlt.*;

import java.util.ArrayList;
import java.util.*;
import java.util.Comparator;

public class MyBot {

    public static void main(final String[] args) {
        final Networking networking = new Networking();
        final GameMap gameMap = networking.initialize("Tamagocchi");

        // We now have 1 full minute to analyse the initial map.
        final String initialMapIntelligence =
                "width: " + gameMap.getWidth() +
                "; height: " + gameMap.getHeight() +
                "; players: " + gameMap.getAllPlayers().size() +
                "; planets: " + gameMap.getAllPlanets().size();
        Log.log(initialMapIntelligence);

        /*

        */

        Vector<Integer> busyShips = new Vector<>();
        Vector<Integer> shipGarbage = new Vector<>();
        Vector<Planet> taskPlanets = new Vector<>();
        Vector<Planet> allPlanets = new Vector<>();

        for (Planet p : gameMap.getAllPlanets().values()) {
            allPlanets.add(p);
        }

        Collections.sort(allPlanets, new Comparator<Planet>() {
            Position centerMap = new Position(gameMap.getWidth()/2, gameMap.getHeight()/2);
            public int compare(Planet p1, Planet p2) {
                return (int) Math.round(p2.getDistanceTo(centerMap)) - (int) Math.round(p1.getDistanceTo(centerMap));
            }
        });
        
        HashMap<Ship, Vector<Planet>> bestPaths = new HashMap<>();
        HashMap<Integer, Planet> orders = new HashMap<>();
        final ArrayList<Move> moveList = new ArrayList<>();
        for (;;) {
            moveList.clear();
            networking.updateMap(gameMap);
            int shipTask = -1;
            /* Checking if any of the busy ships got destroyed. */
            for (Integer shipKey : busyShips) {
                if (!gameMap.getMyPlayer().getShips().containsKey(shipKey)) {
                    shipGarbage.add(shipKey);
                    if (orders.containsKey(shipKey)){
                        taskPlanets.remove(orders.get(shipKey));
                        orders.remove(shipKey);
                    }
                }
            }
            for (Integer shipKey : shipGarbage) {
                busyShips.remove(shipKey);
            }
            shipGarbage.clear();

            for (Integer shipKey : gameMap.getMyPlayer().getShips().keySet()) {
                final Ship ship = gameMap.getMyPlayer().getShips().get(shipKey);
                shipTask++;

                if (ship.getDockingStatus() != Ship.DockingStatus.Undocked) {
                    /* No further tasks are given to this ship. */
                    continue;  
                }
                
                if (!busyShips.contains(shipKey)) {
                    busyShips.add(shipKey);
                }

                int auxTask = shipTask;

                for (final Planet planet : allPlanets) {

                    if (planet.isOwned()) {
                        continue;
                    }

                    if (auxTask % gameMap.getAllPlanets().size() != 0) {
                        auxTask--;
                        continue;
                    }

                    if (!taskPlanets.contains(planet)) {
                        taskPlanets.add(planet);
                    }

                    if (ship.canDock(planet)) {
                        busyShips.remove(shipKey);
                        taskPlanets.remove(planet);
                        if (orders.containsKey(shipKey))
                            orders.remove(shipKey);
                        moveList.add(new DockMove(ship, planet));
                        break;
                    }

                    int ok = 0;
                    for (final Ship auxShip : gameMap.getMyPlayer().getShips().values()) {
                        if (auxShip == ship) 
                            continue;
                        
                        Position auxShipNorth = new Position(auxShip.getXPos(),
                                                             auxShip.getYPos() + auxShip.getRadius());
                        Position auxShipSouth = new Position(auxShip.getXPos(),
                                                             auxShip.getYPos() - auxShip.getRadius());
                        Position auxShipWest = new Position(auxShip.getXPos() - auxShip.getRadius(),
                                                            auxShip.getYPos());
                        Position auxShipEast = new Position(auxShip.getXPos() + auxShip.getRadius(),
                                                            auxShip.getYPos());

                        Position auxShipNorthWest = new Position(auxShip.getXPos() - auxShip.getRadius()/Math.sqrt(2),
                                                                 auxShip.getYPos() + auxShip.getRadius()/Math.sqrt(2));
                        Position auxShipNorthEast = new Position(auxShip.getXPos() + auxShip.getRadius()/Math.sqrt(2),
                                                                 auxShip.getYPos() + auxShip.getRadius()/Math.sqrt(2));
                        Position auxShipSouthWest = new Position(auxShip.getXPos() - auxShip.getRadius()/Math.sqrt(2),
                                                                 auxShip.getYPos() - auxShip.getRadius()/Math.sqrt(2));
                        Position auxShipSouthEast = new Position(auxShip.getXPos() + auxShip.getRadius()/Math.sqrt(2),
                                                                 auxShip.getYPos() - auxShip.getRadius()/Math.sqrt(2));
                        /* N -> S */
                        if (Collision.segmentCircleIntersect(auxShipNorth, 
                                                             auxShipSouth, 
                                                             ship, 
                                                             ship.getRadius())) {
                            Entity newEntity = new Entity(999, 999, ship.getXPos() + ship.getRadius(),
                                                                    ship.getYPos(),
                                                                    1, 
                                                                    0.1);
                            final ThrustMove thrustMove = Navigation.navigateShipToDock(gameMap, ship, newEntity, Constants.MAX_SPEED);
                            if (thrustMove != null) {
                                moveList.add(thrustMove);
                            }
                            ok++;
                            break;
                        }
                    
                        /* W -> E */
                        if (Collision.segmentCircleIntersect(auxShipWest, 
                                                             auxShipEast, 
                                                             ship, 
                                                             ship.getRadius())) {
                            Entity newEntity = new Entity(999, 999, ship.getXPos(),
                                                                    ship.getYPos() + ship.getRadius(),
                                                                    1, 
                                                                    0.1);
                            final ThrustMove thrustMove = Navigation.navigateShipToDock(gameMap, ship, newEntity, Constants.MAX_SPEED);
                            if (thrustMove != null) {
                                moveList.add(thrustMove);
                            }
                            ok++;
                            break;                        
                        }

                        /* NW -> SE */
                        if (Collision.segmentCircleIntersect(auxShipNorthWest, 
                                                             auxShipSouthEast, 
                                                             ship, 
                                                             ship.getRadius())) {
                            Entity newEntity = new Entity(999, 999, ship.getXPos() + ship.getRadius(),
                                                                    ship.getYPos() + ship.getRadius(),
                                                                    1, 
                                                                    0.1);
                            final ThrustMove thrustMove = Navigation.navigateShipToDock(gameMap, ship, newEntity, Constants.MAX_SPEED);
                            if (thrustMove != null) {
                                moveList.add(thrustMove);
                            }
                            ok++;
                            break;
                        }
                    
                        /* NE -> SW */
                        if (Collision.segmentCircleIntersect(auxShipNorthEast, 
                                                             auxShipSouthWest, 
                                                             ship, 
                                                             ship.getRadius())) {
                            Entity newEntity = new Entity(999, 999, ship.getXPos() + ship.getRadius(),
                                                                    ship.getYPos() + ship.getRadius(),
                                                                    1, 
                                                                    0.1);
                            final ThrustMove thrustMove = Navigation.navigateShipToDock(gameMap, ship, newEntity, Constants.MAX_SPEED);
                            if (thrustMove != null) {
                                moveList.add(thrustMove);
                            }
                            ok++;
                            break;
                        }
                    }
                
                    if (ok==1) 
                        break;

                    if (!orders.containsKey(shipKey)) { 
                        final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, ship, planet, Constants.MAX_SPEED);
                    
                        if (newThrustMove != null) {
                            /* Register a new order. */
                            orders.put(shipKey, planet);
                            moveList.add(newThrustMove);
                        }
                    } else {
                        final ThrustMove sameThrustMove = Navigation.navigateShipToDock(gameMap, ship, planet, Constants.MAX_SPEED);
                        if (sameThrustMove != null) {
                            /* Keep same order. */
                            moveList.add(sameThrustMove);
                        }
                    }

                    break;
                }
            }
            Networking.sendMoves(moveList);
        }
    }
}