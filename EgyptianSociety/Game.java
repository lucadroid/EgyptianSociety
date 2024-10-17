import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Game {
    private List<Player> players;
    private List<Thread> playerThreads = new ArrayList<>();
    private int round;
    private Map<String, Integer> counterNames;
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private Player pharaohInCharge;
    private String threadname;

    public Game() {
        players = new ArrayList<>();
        round = 0;
        counterNames = new HashMap<>();
        initialize();
    }

    public void initialize() {
        printSocialScale(); // Stampa la scala sociale
    
        // Ruoli da assegnare inizialmente, uno per ogni classe sociale
        String[] ruoliIniziali = {"Pharaoh", "Priest", "Noble", "Scribe", "Merchant", "Farmer", "Slave"};
        int[] etaIniziali = {40, 60, 20, 20, 30, 20, 20};  // Età initialize dei 7 players
        
        // Creiamo un player per ciascun role
        for (int i = 0; i < ruoliIniziali.length; i++) {
            Player newPlayer = createNewPlayer(ruoliIniziali[i], etaIniziali[i]);
            players.add(newPlayer);
            Thread thread = new Thread(newPlayer);
            threadname = newPlayer.getNickname();
            thread.setName(threadname);                                           // Cambia il nome del thread
            playerThreads.add(thread);
            thread.start();
            System.out.println("New initial player added: " + newPlayer+ "          thread name: "+threadname +"\n");
        }
    
        for (Player g : players) {
            if (g.getRole().equals("Pharaoh")) {
                pharaohInCharge = g;
                break;
            }
        }    

        // Aggiorna i vicini per tutti i players
        updateNeighborsAll();
        
        // Stampa i players initialize
        printPlayers();
    }

    private void printSocialScale() {
        System.out.println("=== Social Scale ===");
        System.out.println("1. Pharaoh (Fluo yellow)");
        System.out.println("2. Priest (White)");
        System.out.println("3. Noble (Blue)");
        System.out.println("4. Scribe (Brown)");
        System.out.println("5. Merchant (Pink)");
        System.out.println("6. Farmer (Green)");
        System.out.println("7. Slave (Purple)");
        System.out.println("=====================");
    }
    public void executeRound() {
        round++;
        System.out.println("\n\n\n=== Round " + round + " ===");

        for (Player player : players) {
            player.resume();
        }

        // Incremento dell'età di tutti i players di 10 anni
        for (Player g : players) {
            if (g.isAlive()) {
                g.increaseAge();
            }
        }

        // Lista dei players aggiunti durante il round
        List<Player> newPlayers = new ArrayList<>();

        // Nascita dei figli
        for (Player g : players) {
            if (g.isAlive()) {
                if (g.tryMakeChild(counterNames.get(g.getRole()))) {
                    Player newPlayer = createNewPlayer(g.getRole(),0);
                    newPlayers.add(newPlayer);
                    Thread thread = new Thread(newPlayer);
                    threadname = newPlayer.getNickname();
                    thread.setName(threadname);                                           // Cambia il nome del thread
                    playerThreads.add(thread);
                    thread.start();
                    System.out.println(g.getNickname() + " gave " + "\u001B[36m" + "BIRTH " + newPlayers.get(newPlayers.size() - 1).getNickname()+ ANSI_RESET);
                }
            }
        }

        // Aggiunta dei new players alla lista principale
        players.addAll(newPlayers);

        // Morte dei players
        for (Player g : players) {
            if (g.isAlive() && g.tryDeath()) {
                System.out.println(ANSI_RED + g.getNickname() + " is " + "DEAD." + ANSI_RESET);
                
                // Gestione speciale per la morte del Pharaoh
                if (g == pharaohInCharge) {
                    chooseNewPharaoh(g);
                }
            } else if (g.isAlive() && g.tryHigherGrade(pharaohInCharge)) {
                System.out.println("\n" + g.getNickname() + " has been promoted to " + g.getRole());
            
            }
        }

        // Promozioni di grado
        for (Player g : players) {
            if (g.isAlive() && g.tryDeath()) {
                System.out.println(ANSI_RED + g.getNickname() + " is " + "DEAD." + ANSI_RESET);
                
                // Verifica se il player morto è il pharaoh in carica
                if (g == pharaohInCharge) {
                    chooseNewPharaoh(g);
                }
            }
        }
        removeDeadPlayers();
        updateNeighborsAll();
        printPlayers();
    }

    private void chooseNewPharaoh(Player deadPharaoh) {
        Player newPharaoh = null;
    
        // Se ha figli, scegli il figlio nato per primo (che è il più anziano)
        if (!deadPharaoh.getChildren().isEmpty()) {
            newPharaoh = Collections.min(deadPharaoh.getChildren(), new Comparator<Player>() {
                @Override
                public int compare(Player g1, Player g2) {
                    // Supponiamo che i figli siano aggiunti in ordine di nascita
                    return g1.getNickname().compareTo(g2.getNickname());
                }
            });
        } 
        // Se non ha figli, cerca un Priest vivo
        else {
            for (Player g : players) {
                if (g.isAlive() && g.getRole().equals("Priest")) {
                    newPharaoh = g;
                    break;
                }
            }
        }
    
        // Se non ci sono figli né Sacerdoti, scegli un qualsiasi player vivo a caso
        if (newPharaoh == null) {
            List<Player> playersAlive = new ArrayList<>();
            for (Player g : players) {
                if (g.isAlive()) {
                    playersAlive.add(g);
                }
            }
            if (!playersAlive.isEmpty()) {
                newPharaoh = playersAlive.get((int) (Math.random() * playersAlive.size()));
            }
        }
    
        // Promuovi il new pharaoh
        if (newPharaoh != null) {
            newPharaoh.setRole("Pharaoh");
            pharaohInCharge = newPharaoh;
            System.out.println("\n" + ANSI_YELLOW + newPharaoh.getNickname() + " was nominated the new Pharaoh!" + ANSI_RESET);
        }
    }
    
    
    
    public void addPlayer() {
        double probabilita = Math.random();
        String role;
    
        if (probabilita <= 0.05) {
            role = "Priest";
        } else if (probabilita <= 0.10) { 
            role = "Noble";
        } else if (probabilita <= 0.20) {
            role = "Scribe";
        } else if (probabilita <= 0.30) { 
            role = "Merchant";
        } else if (probabilita <= 0.50) { 
            role = "Farmer";
        } else {
            role = "Slave"; 
        }
    
        Player newPlayer = createNewPlayer(role,0);
        players.add(newPlayer);
        Thread thread = new Thread(newPlayer);
        threadname = newPlayer.getNickname();
        thread.setName(threadname);                                           // Cambia il nome del thread
        playerThreads.add(thread);
        thread.start();
        System.out.println("New player added: " + newPlayer + "          thread name: "+threadname);
        updateNeighborsAll();
    }
    

    private Player createNewPlayer(String role, int eta) {
        int number = counterNames.getOrDefault(role, 0) + 1;
        counterNames.put(role, number);
        String nickname = role + number;
        return new Player(nickname, role, eta);  // Passa l'età correttamente qui
    }

    private void updateNeighborsAll() {
        for (Player g : players) {
            g.updateNeighbors(players);
        }
    }

    public void printAliveThreads() {
    Thread.getAllStackTraces().keySet().stream()
        .filter(thread -> !isDefaultThread(thread))  // Filter out default threads
        .sorted((t1, t2) -> t1.getName().compareTo(t2.getName()))  // Sort by thread name
        .forEach(thread -> System.out.println("Alive Thread: " + thread.getName()));  // Print sorted threads
    }

    private static boolean isDefaultThread(Thread Thread) {
        // Define names or patterns for default threads
        String name = Thread.getName();
        return name.equals("main") ||
               name.startsWith("GC") ||
               name.startsWith("Reference Handler") ||
               name.startsWith("Finalizer") ||
               name.startsWith("Signal Dispatcher") ||
               name.startsWith("Attach Listener") ||
               name.startsWith("Notification Thread") || name.startsWith("Monitor Ctrl-Break") ||
               name.startsWith("Common-Cleaner") ;
    }

    public List<Player> printPlayers() {
        System.out.println("\nOnline players:");
        printAliveThreads();
    
        // Filtriamo e ordiniamo i players vivi
        List<Player> playersAlive = new ArrayList<>();
        for (Player g : players) {
            if (g.isAlive()) {
                playersAlive.add(g);
            }
        }
    
        // Ordiniamo i players per classe sociale
        Collections.sort(playersAlive, new Comparator<Player>() {
            @Override
            public int compare(Player g1, Player g2) {
                return Role.getGradeIndex(g1.getRole()) - Role.getGradeIndex(g2.getRole());
            }
        });
    
        // Stampiamo i players vivi ordinati
        for (int i = 0; i < playersAlive.size(); i++) {
            System.out.println((i + 1) + ". " + playersAlive.get(i)+ "          thread name: "+playersAlive.get(i).getNickname());
        }
    
        return playersAlive; // Restituiamo la lista dei players vivi
    }

    public void printNeighbors(Player player) {
        System.out.println("\nNeighbors of " + player.getNickname() + ":");
        for (Player neighbors : player.getNeighborsSorted()) {
            if (neighbors.isAlive()) {
                System.out.println(neighbors);
            }
        }
    }

    public void removeDeadPlayers() {
        players.removeIf(player -> !player.isAlive());
        playerThreads.removeIf(thread -> !thread.isAlive());
    }

    public void menu() {

        for (Player player : players) {
            player.pause();
        }
        @SuppressWarnings("resource")
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n1. Next round");
            System.out.println("2. Add player");
            System.out.println("3. Check a player's neighbors");
            System.out.println("4: Check running threads");
            System.out.println("5. End game");
            int option = scanner.nextInt();
    
            switch (option) {
                case 1:
                    executeRound();
                    break;
                case 2:
                    addPlayer();
                    break;
                case 3:
                    // Stampiamo solo i players vivi e otteniamo la lista di essi
                    List<Player> playersAlive = printPlayers();
                    if (playersAlive.isEmpty()) {
                        System.out.println("There are no alive players.");
                        break;
                    }
                    
                    System.out.println("Choose a player to view neighbors:");
                    int indexPlayer = scanner.nextInt() - 1;
    
                    // Verifica che l'index scelto sia valido
                    if (indexPlayer >= 0 && indexPlayer < playersAlive.size()) {
                        printNeighbors(playersAlive.get(indexPlayer));
                    } else {
                        System.out.println("Player number not valid.");
                    }
                    break;
                case 5:
                    System.out.println("Game Over.");
                    System.exit(0); 
                case 4: // Print alive thread names except main and finalizer and reference handler
                    printAliveThreads();
                    break;
                default:
                    System.out.println("Option not valid.");
                    break;
            }
        }
    }    
}
