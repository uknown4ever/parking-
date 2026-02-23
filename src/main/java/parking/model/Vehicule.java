package parking.model;

/**
 * Entité Véhicule.
 */
public class Vehicule {

    public enum Categorie { Auto, Moto, PMR }

    private int       id;
    private String    matricule;
    private String    marque;
    private Categorie categorie;

    public Vehicule() {}

    public Vehicule(int id, String matricule, String marque, Categorie categorie) {
        this.id        = id;
        this.matricule = matricule;
        this.marque    = marque;
        this.categorie = categorie;
    }

    public int       getId()                     { return id; }
    public void      setId(int id)               { this.id = id; }

    public String    getMatricule()              { return matricule; }
    public void      setMatricule(String m)      { this.matricule = m; }

    public String    getMarque()                 { return marque; }
    public void      setMarque(String m)         { this.marque = m; }

    public Categorie getCategorie()              { return categorie; }
    public void      setCategorie(Categorie c)   { this.categorie = c; }

    @Override
    public String toString() {
        return String.format("Vehicule{id=%d, matricule='%s', marque='%s', categorie=%s}",
                id, matricule, marque, categorie);
    }
}
