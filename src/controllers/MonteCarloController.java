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

    private int chooseRandomMove(Vector<Integer> validMoves, int lastMove) {
        if (validMoves.isEmpty()) {
            System.err.println("No valid moves available.");
            return -1; // Indicador de que no hay movimientos válidos
        }

        Random random = new Random();
        int move;

        do {
            move = validMoves.get(random.nextInt(validMoves.size()));
        } while (validMoves.size() > 1 && move == getOppositeMove(lastMove));

        return move;
    }

    private int getOppositeMove(int move) {
        switch (move) {
            case 0: return 1; // Arriba -> Abajo
            case 1: return 0; // Abajo -> Arriba
            case 2: return 3; // Izquierda -> Derecha
            case 3: return 2; // Derecha -> Izquierda
            default: return -1; // Movimiento inválido
        }
    }

    public int getNextAction() {
        System.out.println("Entering getNextAction");
        try {
            PlayMap currentState = map.clone();  // Estado actual del juego
            MCTSNode rootNode = new MCTSNode(currentState, null, -1);  // Nodo raíz que representa el estado inicial

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
            int action = bestNode.getAction();
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
        Vector<Integer> possibleMoves = generateValidMoves(parent.getState());
        MCTSNode bestChild = null;
        double bestReward = Double.NEGATIVE_INFINITY;

        for (int move : possibleMoves) {
            PlayMap newState = parent.getState().clone();
            newState.updateGame(move);
            MCTSNode newChild = parent.addChild(newState, move);

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

        int lastMove = -1;  // Inicialmente, no hay movimientos previos

        while (!simulationState.isGameHalted()) {
            System.out.println("------SIMULACIÓN EN BUCLE----");
            Vector<Integer> possibleMoves = generateValidMoves(simulationState);
            int randomMove = chooseRandomMove(possibleMoves, lastMove); // Usa el último movimiento
            System.out.println("Chosen action: " + randomMove);
            System.out.println("GameHalted de la simulación:" + simulationState.isGameHalted());

            simulationState.updateGame(randomMove);

            // Actualiza el historial
            lastMove = randomMove;

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
}

class MCTSNode {
    private PlayMap state;
    private MCTSNode parent;
    private List<MCTSNode> children;
    private int visits;
    private double reward;  // Acumulación de recompensa
    private  int action;

    public MCTSNode(PlayMap state, MCTSNode parent, int action) {
        this.state = state;
        this.parent = parent;
        this.children = new ArrayList<>();
        this.visits = 0;
        this.reward = 0;
        this.action = action;
    }

    public int getAction(){ return action; }
    public PlayMap getState() { return state;}
    public MCTSNode getParent() { return parent; }
    public List<MCTSNode> getChildren() { return children; }
    public int getVisits() { return visits; }
    public double getReward() { return reward; }

    public void incrementVisits() { visits++; }
    public void addReward(double value) { reward += value; }

    public MCTSNode addChild(PlayMap state, int act) {
        MCTSNode child = new MCTSNode(state, this, act);
        children.add(child);
        return child;
    }
}
