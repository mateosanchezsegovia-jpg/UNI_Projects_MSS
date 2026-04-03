package paa.reservas.business;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import paa.reservas.model.Booking;
import paa.reservas.model.Hotel;
import paa.reservas.persistence.DAOException;
import paa.reservas.persistence.HotelJPADAO;

public class JPABookingService implements BookingService{
	
	
	private static final String PERSISTENCE_UNIT_NAME = "paa";
	
	public static final double LongitudeMAX=180;
	public static final double LongitudeMIN=-180;
	public static final double LatitudeMAX=90;
	public static final double LatitudeMIN=-90;
	public static final double StarsMAX=5;
	public static final double StarsMIN=0;
	
	private EntityManagerFactory emf;
	
	
	public JPABookingService(){

		emf= Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
		
	}
	
	

	@Override
	public Hotel createHotel(String name, String address, int stars, double longitude, double latitude, int doubleRooms,
			int singleRooms) throws BookingServiceException {

		if (name==null || name.trim().isEmpty()) {
			throw new BookingServiceException("Se ha de introducir un nombre de hotel");
		}
		
		if (address==null || address.trim().isEmpty()) {
			throw new BookingServiceException("Se ha de introducir la dirección de un hotel");
		}
		
		if (longitude>LongitudeMAX || longitude<LongitudeMIN) {
			throw new BookingServiceException("Se ha de introducir una longitud valida (Entre: "+LongitudeMIN+","+LongitudeMAX+")");
		}
		
		if (latitude>LatitudeMAX || latitude<LatitudeMIN) {
			throw new BookingServiceException("Se ha de introducir una latitud valida (Entre: "+LatitudeMIN+","+LatitudeMAX+")");
		}
		
		if (stars>StarsMAX || latitude<StarsMIN) {
			throw new BookingServiceException("Se ha de introducir un Numero de estrellas valido (Entre: "+StarsMIN+","+StarsMAX+")");
		}
		
		if (doubleRooms==0 && singleRooms==0) {
			throw new BookingServiceException("El numero de habitaciones no puede ser 0");
		}
		
		if (doubleRooms < 0 || doubleRooms > MAXIMUM_DOUBLE_ROOM_NUMBER-MINIMUM_DOUBLE_ROOM_NUMBER+1) {
			throw new BookingServiceException("El numero de habitacion dobles no es valido");
		}
		
		if (singleRooms < 0 || singleRooms > MAXIMUM_SINGLE_ROOM_NUMBER-MINIMUM_SINGLE_ROOM_NUMBER+1) {
			throw new BookingServiceException("El numero de habitacion individuales no es valido");
		}
		
		Hotel nHotel = new Hotel(null, name, address, stars, longitude, latitude, doubleRooms, singleRooms);
		
		EntityManager em = emf.createEntityManager();
		EntityTransaction et = em.getTransaction();
		
		HotelJPADAO hDAO;
		
		hDAO= new HotelJPADAO(em);
		nHotel=hDAO.create(nHotel);
		
		
		
		try {
			et.begin();
			hDAO = new HotelJPADAO (em);
			nHotel = hDAO.create(nHotel);
			et.commit(); 	
		}catch(DAOException e) {
			if (et.isActive()) {
				et.rollback();
			}
			throw new BookingServiceException ("El hotel ya ha sido registrado");
		}catch (Exception e) {
			try {
				if (et.isActive()) {
					et.rollback();
				}
			}catch (Exception ex) {
				throw ex;
			}
			
		}finally {
			em.close();
		}
		
		return nHotel;
	}

	@Override
	public Hotel findHotel(Long hotelCode) {

		EntityManager em = emf.createEntityManager();
		HotelJPADAO dao = new HotelJPADAO(em);
		
		
		
		return dao.findById(hotelCode);
	}

	@Override
	public List<Hotel> findAllHotels() {
		EntityManager em = emf.createEntityManager();
		HotelJPADAO dao = new HotelJPADAO(em);
		
		return dao.findAll();
	}

	@Override
	public int occupiedDoubleRooms(Long hotelCode, LocalDate date) throws BookingServiceException {

		
		
		return 0;
	}

	@Override
	public int occupiedSingleRooms(Long hotelCode, LocalDate date) throws BookingServiceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Booking makeBooking(Long hotelCode, int numberOfPeople, String travellerName, LocalDate arrivalDate,
			LocalDate departureDate, LocalDate operationDate) throws BookingServiceException {

		if (hotelCode==null || null==findHotel(hotelCode)) {
			throw new BookingServiceException("No se ha podido realizar la reserva. El hotel no existe");

		}
		
		if (numberOfPeople>2 || numberOfPeople<1) {
			throw new BookingServiceException("No se ha podido realizar la reserva. Solo hay reservas para 1 o dos personas");

		}
		
		if (travellerName==null || travellerName.trim().isEmpty()) {
			throw new BookingServiceException("No se ha podido realizar la reserva. Se ha de introducir un nombre");

		}
		
		if (arrivalDate==null || arrivalDate.isBefore(LocalDate.now())) {
			throw new BookingServiceException("No se ha podido realizar la reserva. Se ha de indtroducir una fecha de llegada valida");

		}
		
		
		if (departureDate==null || departureDate.isBefore(arrivalDate)) {
			throw new BookingServiceException("No se ha podido realizar la reserva. Se ha de indtroducir una fecha de salida valida");
			
		}
		
		if (operationDate==null) {
			throw new BookingServiceException("No se ha podido realizar la reserva. Se ha de indtroducir una fecha de reserva valida");
		}
		
		
		return null;
	}

	@Override
	public void cancelBooking(Long bookingCode, LocalDate operationDate) throws BookingServiceException {
		// TODO Auto-generated method stub
		
	}

}

