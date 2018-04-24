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

    private String getCrimeDate() {
        return this.crimeDate;
    }

    private String getNeighborhood() {
        return this.neighborhood;
    }

    private String getDescription() {
        return this.description;
    }

    private String getPremise() {
        return this.premise;
    }

    private String getWeapon() {
        return this.weapon;
    }

    private void setCrimeDate(String cd) {
        this.crimeDate = cd;
    }

    private void setDescription(String des) {
        this.description = des;
    }

    private void setPremise(String prem) {
        this.premise = prem;
    }

    private void setWeapon(String wp) {
        this.weapon = wp;
    }
}
