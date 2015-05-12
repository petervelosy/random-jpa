package com.kuro.random.jpa.testUtil.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by Kumar Rohit on 4/26/15.
 */
@Entity
@Table(name = "shift")
public class Shift {
    @Column(name = "shift_id")
    private Integer shiftId;
    private String name;
    private Date startDate;
    private Date endDate;

    public Integer getShiftId() {
        return shiftId;
    }

    public void setShiftId(final Integer shiftId) {
        this.shiftId = shiftId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(final Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(final Date endDate) {
        this.endDate = endDate;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Shift shift = (Shift) o;

        return !(shiftId != null ? !shiftId.equals(shift.shiftId) : shift.shiftId != null);

    }

    @Override
    public int hashCode() {
        return shiftId != null ? shiftId.hashCode() : 0;
    }
}
