package com.gec.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("tickets")
public class Tickets implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ticket_id", type = IdType.AUTO)
    private Integer ticketId;

    private Integer userId;

    private Integer tripId;

    private String seatNumber;

    private BigDecimal price;

    private LocalDateTime purchaseTime;

    private Boolean isRefunded;

    public Integer getTicketId() {
        return ticketId;
    }

    public void setTicketId(Integer ticketId) {
        this.ticketId = ticketId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getTripId() {
        return tripId;
    }

    public void setTripId(Integer tripId) {
        this.tripId = tripId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDateTime getPurchaseTime() {
        return purchaseTime;
    }

    public void setPurchaseTime(LocalDateTime purchaseTime) {
        this.purchaseTime = purchaseTime;
    }

    public Boolean getIsRefunded() {
        return isRefunded;
    }

    public void setIsRefunded(Boolean isRefunded) {
        this.isRefunded = isRefunded;
    }

    @Override
    public String toString() {
        return "Tickets{" +
        "ticketId = " + ticketId +
        ", userId = " + userId +
        ", tripId = " + tripId +
        ", seatNumber = " + seatNumber +
        ", price = " + price +
        ", purchaseTime = " + purchaseTime +
        ", isRefunded = " + isRefunded +
        "}";
    }
}
