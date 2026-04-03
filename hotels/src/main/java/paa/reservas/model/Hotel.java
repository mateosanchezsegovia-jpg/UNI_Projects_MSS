package paa.reservas.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class Hotel implements Serializable{
	private static final long serialVersionUID = 1063693850272523163L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long   code;
	
    private String name;
    private String address;
    private double longitude, latitude;
    private int stars;
    private int doubleRooms, singleRooms;
    @OneToMany(mappedBy="hotel")
    private List<Booking> bookings;

    public Hotel() {}

    public Hotel(Long code, String name, String address, int stars, double longitude, double latitude, int doubleRooms, int singleRooms) {
        this.code = code;
        this.name = name;
        this.address = address;
        this.stars = stars;
        this.longitude = longitude;
        this.latitude = latitude;
        this.doubleRooms = doubleRooms;
        this.singleRooms = singleRooms;
        this.bookings = new ArrayList<>();
    }

    public Long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    
    public int getStars() {
        return this.stars;
    }
    
    public void setStars(int stars) {
        this.stars = stars;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public int getDoubleRooms() {
        return doubleRooms;
    }

    public void setDoubleRooms(int doubleRooms) {
        this.doubleRooms = doubleRooms;
    }

    public int getSingleRooms() {
        return singleRooms;
    }

    public void setSingleRooms(int singleRooms) {
        this.singleRooms = singleRooms;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Hotel)) return false;

        Hotel hotel = (Hotel) o;

        return Objects.equals(code, hotel.code);
    }

    @Override
    public int hashCode() {
        return code != null ? code.hashCode() : 0;
    }
}
