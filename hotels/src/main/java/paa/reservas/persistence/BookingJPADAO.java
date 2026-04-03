package paa.reservas.persistence;

import java.util.List;

import javax.persistence.EntityManager;

import paa.reservas.model.Booking;

public class BookingJPADAO extends JPADAO<Booking, Long> {

	public BookingJPADAO(EntityManager em) {
		super(em, Booking.class);
	}

	public List<Booking> findAllBookingsSortedByArrivalDate(String ArrivalDate){
		return null;
	}
	
}
