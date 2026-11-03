package com.gec.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("trips")
public class Trips implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "trip_id", type = IdType.AUTO)
    private Integer tripId;

    private String departure;

    private String destination;

    private LocalDateTime departureTime;

    private LocalDateTime arrivalTime;

    public Integer getTripId() {
        return tripId;
    }

    public void setTripId(Integer tripId) {
        this.tripId = tripId;
    }

    public String getDeparture() {
        return departure;
    }

    public void setDeparture(String departure) {
        this.departure = departure;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    @Override
    public String toString() {
        return "Trips{" +
        "tripId = " + tripId +
        ", departure = " + departure +
        ", destination = " + destination +
        ", departureTime = " + departureTime +
        ", arrivalTime = " + arrivalTime +
        "}";
    }
}
