package com.example.matthew.crimertracker;

/**
 * Created by joshuarusso on 4/23/18.
 */

public class SingleCrime {
    private String crimeDate;
    private String neighborhood;
    private String description;
    private String premise;
    private String weapon;

    public SingleCrime(String cd, String nb, String des, String prem, String wp) {
        this.crimeDate = cd;
        this.neighborhood = nb;
        this.description = des;
        this.premise = prem;
        this.weapon = wp;
    }

    public String getCrimeDate() {
        return this.crimeDate;
    }

    public String getNeighborhood() {
        return this.neighborhood;
    }

    public String getDescription() {
        return this.description;
    }

    public String getPremise() {
        return this.premise;
    }

    public String getWeapon() {
        return this.weapon;
    }

    public void setCrimeDate(String cd) {
        this.crimeDate = cd;
    }

    public void setDescription(String des) {
        this.description = des;
    }

    public void setPremise(String prem) {
        this.premise = prem;
    }

    public void setWeapon(String wp) {
        this.weapon = wp;
    }

    public String toString() {
        return "crimedate: " + this.crimeDate;
    }
}
