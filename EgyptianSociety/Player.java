import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class Player implements Runnable{
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";

    private String nickname;
    private String role;
    private List<Player> neighbors;
    private boolean alive;
    private List<Player> children;
    private int age;  // Nuovo campo: età del player
    private boolean isPaused;
    private final Object pauseLock = new Object();


    public Player(String nickname, String role, int age) {
        this.nickname = nickname;
        this.role = role;
        this.neighbors = new ArrayList<>();
        this.alive = true;
        this.children = new ArrayList<>();
        this.age = age;  // Inizializza l'età del player

    }

    public String getNickname() {
        return nickname;
    }

    public String getRole() {
        return role;
    }

    public void increaseAge() {
        this.age += 10;  // Incrementa l'età di 10 anni
    }


    public boolean isAlive() {
        return alive;
    }

    public void addChild(Player child) {
        this.children.add(child);
    }

    public void updateNeighbors(List<Player> tuttiGiocatori) {
        neighbors.clear();
        for (Player g : tuttiGiocatori) {
            if (g.isAlive() && g != this) {
                if (g.getRole().equals(this.role) || children.contains(g) ||
                    Role.isNext(g.getRole(), this.role)) {
                    neighbors.add(g);
                }
            }
        }
    }

    public boolean tryMakeChild(int childrenNumber) {
        double probability = Math.random();
        if (probability <= 0.30) {
            return true;
        }
        return false;
    }

    public List<Player> getChildren() {
        return children;
    }

    public void promoteToPharaoh() {
        this.role = "Pharaoh";
    }

    public boolean tryDeath() {
        if (this.age >= 90) {  // Se il player ha 90 anni o più, muore per vecchiaia
            this.alive = false;
            System.out.println("\n" +nickname + " dying of old age");
            return true;
        }
        double probability = Math.random();
        if ((role.equals("Pharaoh") && probability <= 0.03)) {
            alive = false;
            return true;
        } else if ((role.equals("Slave") && probability <= 0.10) ||
            (role.equals("Farmer") && probability <= 0.09) ||
            (role.equals("Merchant") && probability <= 0.08) ||
            (role.equals("Scribe") && probability <= 0.07) ||
            (role.equals("Noble") && probability <= 0.05) ||
            (role.equals("Priest") && probability <= 0.04)) {
            alive = false;
            return true;
        }
        return false;
    }

    public boolean tryHigherGrade(Player faraoneInCarica) {
        double probability = Math.random();
    
        // Evitiamo la promozione se il player è già al grado più alto
        if (role.equals("Priest") || role.equals("Pharaoh") || this == faraoneInCarica) {
            return false;
        }

        boolean promosso = false;
    
        if (role.equals("Slave") && probability <= 0.05) {
            role = "Farmer";
            promosso = true;
        } else if (role.equals("Farmer") && probability <= 0.10) {
            role = "Merchant";
            promosso = true;
        } else if (role.equals("Merchant") && probability <= 0.15) {
            role = "Scribe";
            promosso = true;
        } else if (role.equals("Scribe") && probability <= 0.20) {
            role = "Noble";
            promosso = true;
        } else if (role.equals("Noble") && probability <= 0.30) {
            role = "Priest";
            promosso = true;
        }
    
        return promosso;
    }
    

    public void setRole(String role) {
        this.role = role;
    }
    
    public int getHierarchy() {
        switch (role) {
            case "Pharaoh": return 1;
            case "Priest": return 2;
            case "Noble": return 3;
            case "Scribe": return 4;
            case "Merchant": return 5;
            case "Farmer": return 6;
            case "Slave": return 7;
            default: return 8; // In caso di role sconosciuto
        }
    }

    public List<Player> getNeighborsSorted() {
        //List<Player> neighbors = getNeighbors(); // Supponendo che getNeighbors() restituisca una lista di neighbors

        // Ordina i neighbors in base alla gerarchia
        Collections.sort(neighbors, new Comparator<Player>() {
            @Override
            public int compare(Player g1, Player g2) {
                return Integer.compare(g1.getHierarchy(), g2.getHierarchy());
            }
        });

        return neighbors;
    }

    // public List<Player> getNeighbors() {return neighbors;}

    @Override
    public void run() {
        while (alive) {
            synchronized (pauseLock) {
                while (isPaused) {
                    try {
                        pauseLock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }        
    }

    public void pause() {
        synchronized (pauseLock) {
            isPaused = true;
        }
    }

    public void resume() {
        synchronized (pauseLock) {
            isPaused = false;
            pauseLock.notifyAll();
        }
    }

    @Override
    public String toString() {
        String state = alive ? ANSI_GREEN + "Alive" + ANSI_RESET : ANSI_RED + "Dead" + ANSI_RESET;
        String colorRole = Role.getColorRole(role);
        return colorRole + nickname + " (" + role + ")" + ANSI_RESET + " - " + state + " - " + age + " years";
    }

    public String toStringPromotion() {
        String state = alive ? ANSI_GREEN + "Alive" + ANSI_RESET : ANSI_RED + "Dead" + ANSI_RESET;
        String colorRole = Role.getColorRole(role);
        return ANSI_YELLOW + colorRole + nickname + " (" + role + ")" + ANSI_RESET + " - " + state;
    }
    
}
