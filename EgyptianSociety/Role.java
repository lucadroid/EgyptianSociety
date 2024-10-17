public class Role {
    private static final String[] GRADES = {
        "Pharaoh", "Priest", "Noble", "Scribe", "Merchant", "Farmer", "Slave"
    };

    // Metodo per ottenere il role successivo nel grado sociale
    public static String nextGrade(String role) {
        for (int i = 0; i < GRADES.length - 1; i++) {
            if (GRADES[i].equals(role)) {
                return GRADES[i + 1];
            }
        }
        return role;
    }

    // Metodo per verificare se due ruoli sono prossimi tra di loro
    public static boolean isNext(String role1, String role2) {
        for (int i = 0; i < GRADES.length - 1; i++) {
            if (GRADES[i].equals(role1) && GRADES[i + 1].equals(role2)) {
                return true;
            }
            if (GRADES[i].equals(role2) && GRADES[i + 1].equals(role1)) {
                return true;
            }
        }
        return false;
    }

    // Metodo per ottenere l'indice di un role
    public static int getGradeIndex(String role) {
        for (int i = 0; i < GRADES.length; i++) {
            if (GRADES[i].equals(role)) {
                return i;
            }
        }
        return -1; // Restituisce -1 se il role non è trovato
    }

    // Metodo per ottenere il colore associato a un role
    public static String getColorRole(String role) {
        switch (role) {
            case "Pharaoh":
                return "\u001B[93m"; // Giallo fluorescente
            case "Priest":
                return "\u001B[37m"; // Bianco
            case "Noble":
                return "\u001B[94m"; // Azzurro
            case "Scribe":
                return "\u001B[90m"; // Grigio
            case "Merchant":
                return "\u001B[95m"; // Rosa
            case "Farmer":
                return "\u001B[38;5;208m"; // Arancione
            case "Slave":
                return "\u001B[38;5;94m"; // Marrone
            default:
                return "\u001B[0m";  // Resetta colore
        }
    }
}
