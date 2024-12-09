package controllers;

import dungeon.play.GameCharacter;
import dungeon.play.PlayMap;
import util.math2d.Point2D;

import java.util.Vector;

public class ExplorerController extends Controller{
    private String state = "Explorando";
    public ExplorerController(PlayMap map, GameCharacter controllingChar) {
        super(map, controllingChar, "MonteCarloController");
        this.map = map;
    }

    public ExplorerController(PlayMap map, GameCharacter controllingChar, String label) {
        super(map, controllingChar, label);
        this.map = map;
    }

    @Override
    public int getNextAction() {
        if(map.getHero().getHitpoints() <= 15) state = "Dañado";
        if(state == "Explorando") return explore();
        else if(state == "Dañado") return causious();
        else return -1;
    }

    private int explore() {
        int nextMove = -1;
        int lessVisited = Integer.MAX_VALUE;
        Point2D hero;
        for(int mov : generateValidMoves(map)) {
            hero = map.getHero().getNextPosition(mov);
            if(map.getVisited()[(int) hero.x][(int) hero.y] < lessVisited) {
                lessVisited = map.getVisited()[(int) hero.x][(int) hero.y];
                nextMove = mov;
            }
            if(map.getHero().getNextPosition(mov) == map.getExit(1)) return mov;
        }
        return nextMove;
    }

    private int causious() {
        int nextMove = -1;
        int lessVisited = Integer.MAX_VALUE;
        Point2D hero;
        for(int mov : generateValidMoves(map)) {
            hero = map.getHero().getNextPosition(mov);
            if(map.getVisited()[(int) hero.x][(int) hero.y] < lessVisited && !map.isMonster((int) hero.x, (int) hero.y)) {
                lessVisited = map.getVisited()[(int) hero.x][(int) hero.y];
                nextMove = mov;
            }
            if(hero == map.getExit(1)) return mov;
            if(map.isPotion((int) hero.x, (int) hero.y)) {
                state = "Explorando";
                return mov;
            }
        }
        return nextMove;
    }

    public Vector<Integer> generateValidMoves(PlayMap current){
        Vector<Integer> result = new Vector<Integer>();
        for(int i = 0; i < 4; i++){
            Point2D newMove = current.getHero().getNextPosition(i);
            if(current.isValidMove(newMove)){
                result.add(i);
            }
        }
        return result;
    }
}
