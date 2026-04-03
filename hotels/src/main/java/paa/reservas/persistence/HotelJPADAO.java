package paa.reservas.persistence;

import javax.persistence.EntityManager;

import paa.reservas.model.Hotel;

public class HotelJPADAO extends JPADAO<Hotel, Long> {

	public HotelJPADAO(EntityManager em) {
		super(em, Hotel.class);
	}
	
	
	
	

}
