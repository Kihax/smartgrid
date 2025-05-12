package fr.imta.smartgrid.model;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.json.JsonObject;
import jakarta.persistence.*;

@Entity
@Table(name = "sensor")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Sensor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    private String dtype;

    private String description;

    @ManyToOne
    @JoinColumn(name = "grid")
    private Grid grid;

    @ManyToMany(mappedBy = "sensors")
    private List<Person> owners = new ArrayList<>();

    @OneToMany(mappedBy = "sensor")
    private List<Measurement> measurements = new ArrayList<>();

    public String toJSON(EntityManager db) {
        JsonObject res = new JsonObject();

        return res.toString();
    }

    // Getters et setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Grid getGrid() {
        return grid;
    }

    public void setGrid(Grid grid) {
        this.grid = grid;
    }

    public String getDtype() {
        return dtype;
    }
    public void setDtype(String dtype) {
        this.dtype = dtype;
    }

    public List<Person> getOwners() {
        return owners;
    }

    public void setOwners(List<Person> owners) {
        this.owners = owners;
    }

    public List<Measurement> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(List<Measurement> measurements) {
        this.measurements = measurements;
    }
}
