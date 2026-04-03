package paa.reservas.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;


@Entity
public class Booking implements Serializable{
	private static final long serialVersionUID = -7811408382815569706L;
	@Id
	@GeneratedValue
    private Long      code;
    private int       roomNumber;
    private int       numberOfPeople;
    private String    travellerName;
    private LocalDate arrivalDate;
    private LocalDate departureDate;
    @ManyToOne
	//@JoinColumn (name="HOTEL_CODE")
    private Hotel hotel;
    

    public Booking() {}

    public Booking(Long code, int roomNumber, int numberOfPeople, String travellerName, LocalDate arrivalDate, LocalDate departureDate, Hotel hotel) {
        this.code = code;
        this.roomNumber = roomNumber;
        this.numberOfPeople = numberOfPeople;
        this.travellerName = travellerName;
        this.arrivalDate = arrivalDate;
        this.departureDate = departureDate;
        this.hotel = hotel;
    }

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public int getNumberOfPeople() {
        return numberOfPeople;
    }

    public void setNumberOfPeople(int numberOfPeople) {
        this.numberOfPeople = numberOfPeople;
    }

    public String getTravellerName() {
        return travellerName;
    }

    public void setTravellerName(String travellerName) {
        this.travellerName = travellerName;
    }

    public LocalDate getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(LocalDate arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Booking)) return false;

        Booking booking = (Booking) o;

        return Objects.equals(code, booking.code);
    }

    @Override
    public int hashCode() {
        return code != null ? code.hashCode() : 0;
    }

}
