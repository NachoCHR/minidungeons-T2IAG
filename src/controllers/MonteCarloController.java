package controllers;
import dungeon.play.GameCharacter;
import dungeon.play.PlayMap;

import java.util.*;

import util.math2d.Point2D;

public class MonteCarloController extends Controller {
    private static final int SIMULATION_COUNT = 100;  // Número de simulaciones
    private static final double EXPLORATION_CONSTANT = Math.sqrt(2); // Constante de exploración (para UCT)
    private PlayMap map;

    public MonteCarloController(PlayMap map, GameCharacter controllingChar) {
        super(map, controllingChar, "MonteCarloController");
        this.map = map;
    }

    public MonteCarloController(PlayMap map, GameCharacter controllingChar, String label) {
        super(map, controllingChar, label);
        this.map = map;
    }

    // Obtener las posiciones de los elementos de interés
    public List<Point2D> getPossibleMoves(PlayMap current) {
        List<Point2D> moves = new ArrayList<>();
        int heroX = current.getHero().getX();
        System.out.println("Posición X del Hero es: " + heroX);
        int heroY = current.getHero().getY();
        System.out.println("Posición Y del Hero es: " + heroY);

        // Añadir movimientos válidos: Up, Right, Down, Left
        if (current.isValidMove(heroX, heroY - 1)) moves.add(new Point2D(heroX, heroY - 1)); // UP
        if (current.isValidMove(heroX + 1, heroY)) moves.add(new Point2D(heroX + 1, heroY)); // RIGHT
        if (current.isValidMove(heroX, heroY + 1)) moves.add(new Point2D(heroX, heroY + 1)); // DOWN
        if (current.isValidMove(heroX - 1, heroY)) moves.add(new Point2D(heroX - 1, heroY)); // LEFT
        System.out.println("Possible moves: " + moves);

        return moves;
    }
    public int getNextAction() {
        System.out.println("Entering getNextAction");
        try {
            PlayMap currentState = map.clone();  // Estado actual del juego
            MCTSNode rootNode = new MCTSNode(currentState, null);  // Nodo raíz que representa el estado inicial

            // Ejecutar simulaciones para construir el árbol de MCTS
            for (int i = 0; i < SIMULATION_COUNT; i++) {
                System.out.println("Simulation number:" + i);
                MCTSNode selectedNode = selectNode(rootNode);
                System.out.println("Se seleccionó el Nodo");
                double simulationResult = simulate(selectedNode.getState());
                System.out.println("Se simuló el Node");
                backpropagate(selectedNode, simulationResult);
                System.out.println("Se propagó el Node");
            }

            // Seleccionar la mejor acción basado en las visitas
            MCTSNode bestNode = bestChild(rootNode);
            int action = translateToAction(bestNode.getState().getHero().getPosition());
            System.out.println("Selected action: " + action);
            return action;
        } catch (Exception e) {
            System.err.println("Error in getNextAction: " + e.getMessage());
            e.printStackTrace();
            return -1;  // Acción no válida en caso de error
        }
    }

    // Fase de selección: selecciona un nodo para expandir usando UCT (Upper Confidence Bound for Trees)
    private MCTSNode selectNode(MCTSNode node) {
        while (!node.getChildren().isEmpty()) {
            node = bestUCTChild(node);
        }
        return node;
    }

    // Selecciona el mejor nodo hijo usando la fórmula UCT
    private MCTSNode bestUCTChild(MCTSNode node) {
        double bestValue = Double.NEGATIVE_INFINITY;
        MCTSNode bestChild = null;

        for (MCTSNode child : node.getChildren()) {
            double uctValue = (child.getReward() / (child.getVisits() + 1)) +
                    EXPLORATION_CONSTANT * Math.sqrt(Math.log(node.getVisits() + 1) / (child.getVisits() + 1));

            if (uctValue > bestValue) {
                bestValue = uctValue;
                bestChild = child;
            }
        }

        return bestChild;
    }

    // Fase de expansión: agrega un nuevo nodo hijo basado en un movimiento
    private MCTSNode expandNode(MCTSNode parent) {
        List<Point2D> possibleMoves = getPossibleMoves(parent.getState());
        MCTSNode bestChild = null;
        double bestReward = Double.NEGATIVE_INFINITY;

        for (Point2D move : possibleMoves) {
            PlayMap newState = parent.getState().clone();
            newState.updateGame(translateToAction(move));
            MCTSNode newChild = parent.addChild(newState);

            double reward = simulate(newState);
            if (reward > bestReward) {
                bestReward = reward;
                bestChild = newChild;
            }
        }

        return bestChild;
    }

    // Simula el juego desde un estado dado
    private double simulate(PlayMap state) {
        System.out.println("-----Se ingresa a la simulación----");
        PlayMap simulationState = state.clone();
        System.out.println("Se clona el estado");

        while (!simulationState.isGameHalted()) {
            System.out.println("------SIMULACIÓN EN BUCLE----");
            List<Point2D> possibleMoves = getPossibleMoves(simulationState);
            int randomMove = chooseRandomMove(possibleMoves);
            System.out.println("Chosen action: " + randomMove);

            simulationState.updateGame(randomMove);

            System.out.println("Hero position: " + simulationState.getHero().getPosition());
            System.out.println("Hero HP: " + simulationState.getHero().getHitpoints());
        }

        return evaluate(simulationState);
    }

    // Mtodo para seleccionar un movimiento aleatorio de la lista de movimientos posibles
    private int chooseRandomMove(List<Point2D> possibleMoves) {
        if (possibleMoves.isEmpty()) {
            System.err.println("No possible moves available.");
            return -1; // O cualquier valor que indique un estado inválido
        }
        int randomIndex = (int) (Math.random() * possibleMoves.size());
        System.out.println("Chosen move index: " + randomIndex + ", Move: " + possibleMoves.get(randomIndex));
        return randomIndex; // Si se espera un índice como acción
    }

    // Retropropagación: actualiza las estadísticas de los nodos en el camino desde el nodo seleccionado
    private void backpropagate(MCTSNode node, double result) {
        while (node != null) {
            node.incrementVisits();
            node.addReward(result);
            node = node.getParent();
        }
    }

    // Selecciona la mejor acción del nodo raíz basado en el número de visitas
    private MCTSNode bestChild(MCTSNode node) {
        MCTSNode bestChild = null;
        int maxVisits = -1;

        for (MCTSNode child : node.getChildren()) {
            if (child.getVisits() > maxVisits) {
                maxVisits = child.getVisits();
                bestChild = child;
            }
        }

        return bestChild;
    }

    // Función de evaluación
    private double evaluate(PlayMap state) {
        if (!state.isGameHalted()) {
            if (state.getHero().isAlive()) {
                return 1.0;  // El héroe ha ganado
            } else {
                return -1.0;  // El héroe ha perdido
            }
        }
        return 0.0;  // Estado intermedio
    }

    // Función para traducir un movimiento a una acción
    private int translateToAction(Point2D move) {
        if ((int)move.x == 0 && (int)move.y == -1) return 0;  // Arriba
        if ((int)move.x == 1 && (int)move.y == 0) return 1;   // Derecha
        if ((int)move.x == 0 && (int)move.y == 1) return 2;   // Abajo
        if ((int)move.x == -1 && (int)move.y == 0) return 3;  // Izquierda
        return -1;  // Acción no válida
    }
}

class MCTSNode {
    private PlayMap state;
    private MCTSNode parent;
    private List<MCTSNode> children;
    private int visits;
    private double reward;  // Acumulación de recompensa

    public MCTSNode(PlayMap state, MCTSNode parent) {
        this.state = state;
        this.parent = parent;
        this.children = new ArrayList<>();
        this.visits = 0;
        this.reward = 0;
    }

    public PlayMap getState() { return state;}
    public MCTSNode getParent() { return parent; }
    public List<MCTSNode> getChildren() { return children; }
    public int getVisits() { return visits; }
    public double getReward() { return reward; }

    public void incrementVisits() { visits++; }
    public void addReward(double value) { reward += value; }

    public MCTSNode addChild(PlayMap state) {
        MCTSNode child = new MCTSNode(state, this);
        children.add(child);
        return child;
    }
}
