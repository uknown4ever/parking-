package parking.model;

import java.time.LocalDateTime;
import java.time.Duration;

/**
 * Entité Stationnement (association Place ↔ Véhicule).
 */
public class Stationnement {

    private int           id;
    private Place         place;
    private Vehicule      vehicule;
    private LocalDateTime dateEntree;
    private LocalDateTime dateSortie;   // null si encore en cours
    private Double        montant;      // null si encore en cours

    public Stationnement() {}

    public Stationnement(int id, Place place, Vehicule vehicule,
                         LocalDateTime dateEntree, LocalDateTime dateSortie, Double montant) {
        this.id         = id;
        this.place      = place;
        this.vehicule   = vehicule;
        this.dateEntree = dateEntree;
        this.dateSortie = dateSortie;
        this.montant    = montant;
    }

    /** Calcule le montant automatiquement à la sortie. */
    public double calculerMontant() {
        if (dateSortie == null) return 0;
        long minutes = Duration.between(dateEntree, dateSortie).toMinutes();
        double heures = minutes / 60.0;
        return Math.round(heures * place.getTarifHoraire() * 100.0) / 100.0;
    }

    public boolean isEnCours() { return dateSortie == null; }

    // ---- Getters / Setters ----
    public int           getId()                    { return id; }
    public void          setId(int id)              { this.id = id; }

    public Place         getPlace()                 { return place; }
    public void          setPlace(Place p)          { this.place = p; }

    public Vehicule      getVehicule()              { return vehicule; }
    public void          setVehicule(Vehicule v)    { this.vehicule = v; }

    public LocalDateTime getDateEntree()            { return dateEntree; }
    public void          setDateEntree(LocalDateTime d) { this.dateEntree = d; }

    public LocalDateTime getDateSortie()            { return dateSortie; }
    public void          setDateSortie(LocalDateTime d) { this.dateSortie = d; }

    public Double        getMontant()               { return montant; }
    public void          setMontant(Double m)       { this.montant = m; }

    @Override
    public String toString() {
        return String.format("Stationnement{id=%d, place=%s, vehicule=%s, entree=%s, sortie=%s, montant=%s}",
                id,
                place != null ? place.getNumero() : "?",
                vehicule != null ? vehicule.getMatricule() : "?",
                dateEntree, dateSortie,
                montant != null ? String.format("%.2f €", montant) : "En cours");
    }
}
