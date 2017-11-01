package com.example.changoo.fishing.data;

/**
 * Created by changoo on 2017-03-29.
 */

public class Data {
	private Double angle;
	private Double power;

    public Data(Double angle, Double power) {
        this.angle = angle;
        this.power = power;
    }

    @Override
    public String toString() {
        return "Data{" +
                "angle=" + angle +
                ", power=" + power +
                '}';
    }

    public Double getAngle() {
        return angle;
    }

    public void setAngle(Double angle) {
        this.angle = angle;
    }

    public Double getPower() {
        return power;
    }

    public void setPower(Double power) {
        this.power = power;
    }
}
