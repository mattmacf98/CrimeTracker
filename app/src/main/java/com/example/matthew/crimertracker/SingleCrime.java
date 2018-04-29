package com.example.matthew.crimertracker;

/**
 * Created by joshuarusso on 4/29/18.
 */

public class SingleCrime {

    public String crimeDate;
    public String neighborhood;
    public String description;
    public String premise;
    public String weapon;

    public SingleCrime() {

    }

    public SingleCrime(String cd, String nb, String des, String prem, String wp) {
        this.crimeDate = cd;
        this.neighborhood = nb;
        this.description = des;
        this.premise = prem;
        this.weapon = wp;
    }
}
