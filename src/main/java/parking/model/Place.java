package parking.model;

/**
 * Entité Place de parking.
 */
public class Place {

    public enum Type   { Auto, Moto, PMR }
    public enum Statut { Libre, Occupée }

    private int    id;
    private String numero;
    private Type   type;
    private Statut statut;
    private double tarifHoraire;

    public Place() {}

    public Place(int id, String numero, Type type, Statut statut, double tarifHoraire) {
        this.id           = id;
        this.numero       = numero;
        this.type         = type;
        this.statut       = statut;
        this.tarifHoraire = tarifHoraire;
    }

    // ---- Getters / Setters ----
    public int    getId()           { return id; }
    public void   setId(int id)     { this.id = id; }

    public String getNumero()             { return numero; }
    public void   setNumero(String n)     { this.numero = n; }

    public Type   getType()               { return type; }
    public void   setType(Type t)         { this.type = t; }

    public Statut getStatut()             { return statut; }
    public void   setStatut(Statut s)     { this.statut = s; }

    public double getTarifHoraire()           { return tarifHoraire; }
    public void   setTarifHoraire(double t)   { this.tarifHoraire = t; }

    @Override
    public String toString() {
        return String.format("Place{id=%d, numero='%s', type=%s, statut=%s, tarif=%.2f €/h}",
                id, numero, type, statut, tarifHoraire);
    }
}
