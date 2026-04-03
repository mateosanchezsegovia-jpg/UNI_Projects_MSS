package paa.reservas;

import static org.junit.jupiter.api.Assertions.*;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;


import paa.reservas.business.BookingServiceException;
import paa.reservas.business.JPABookingService;
import paa.reservas.model.Hotel;
import org.junit.jupiter.api.*;

@TestMethodOrder(OrderAnnotation.class)
class JPABookingServiceTest {

	@Test
	@Order(1)
	void A_testCreateHotel() {
		JPABookingService service = new JPABookingService();
		Hotel hotel1=null;
		Hotel hotel2=null;
		
		try {
			hotel1= service.createHotel("Hotel1", "Espronceda", 1, 2, 0,15,15);
			assertNotNull(hotel1);
			assertEquals(service.findAllHotels().size(), 1);
			
			hotel2 = service.createHotel("Hotel2", "zurbano", 0, 0, 0, 0, 0);
			assertNotNull(hotel2);
			assertEquals(service.findAllHotels().size(), 2);
			
			
		}catch(BookingServiceException e) {
			fail ("No deberia saltar la excepcion.");
		}
		//assertEquals (service.findAllHotels().size(),1);
		assertNotNull(hotel1.getCode());
	}
	
	
	@Test 
	@Order(2)
	void B_testCreateHotelNOK(){
		JPABookingService service = new JPABookingService();
		Hotel hotel1=null;
		
		try {
			hotel1= service.createHotel(null, "Espronceda", 1, 2, 0,15,15);
			fail("Deria saltar la excepcion.");
		
		}catch(BookingServiceException e) {
			assertEquals (e.getMessage(), "Se ha de introducir un nombre de hotel");
		}
		
		try {
			hotel1= service.createHotel("", "Espronceda", 1, 2, 0,15,15);
			fail("Deberia saltar la excepcion.");
		
		}catch(BookingServiceException e) {
			assertEquals (e.getMessage(), "Se ha de introducir un nombre de hotel");
		}
		
		try {
			hotel1= service.createHotel("   ", "Espronceda", 1, 2, 0,15,15);
			fail("Deberia saltar la excepcion.");
		
		}catch(BookingServiceException e) {
			assertEquals (e.getMessage(), "Se ha de introducir un nombre de hotel");
		}
		
		
		//Prueba direccion
		
		
		try {
			hotel1= service.createHotel("hotel", null, 1, 2, 0,10,10);
			fail("Deberia saltar la excepcion.");
		
		}catch(BookingServiceException e) {
			assertEquals (e.getMessage(), "Se ha de introducir la dirección de un hotel");
		}
		
		try {
			hotel1= service.createHotel("hotel", "", 1, 2, 0,10,10);
			fail("Deberia saltar la excepcion.");
		
		}catch(BookingServiceException e) {
			assertEquals (e.getMessage(), "Se ha de introducir la dirección de un hotel");
		}
		
		try {
			hotel1= service.createHotel("hotel", "  ", 1, 2, 0,10,10);
			fail("Deberia saltar la excepcion.");
		
		}catch(BookingServiceException e) {
			assertEquals (e.getMessage(), "Se ha de introducir la dirección de un hotel");
		}
		
		
		//Prueba estrellas
		
		
		try {
			hotel1= service.createHotel("hotel", "zurbano", -1, 2, 0,10,10);
			fail("Deberia saltar la excepcion.");
		
		}catch(BookingServiceException e) {
			assertEquals (e.getMessage(), "Se ha de introducir un Numero de estrellas valido (Entre: "+0+","+5+")");
		}
		
		try {
			hotel1= service.createHotel("hotel", "zurbano", 7, 2, 0,10,10);
			fail("Deberia saltar la excepcion.");
		
		}catch(BookingServiceException e) {
			assertEquals (e.getMessage(), "Se ha de introducir un Numero de estrellas valido (Entre: 0,5)");
		}
		
		
		//Prueba longitud
		
		
		try {
			hotel1= service.createHotel("hotel", "zurbano", 1, -200, 0,10,10);
			fail("Deberia saltar la excepcion.");
		
		}catch(BookingServiceException e) {
			assertEquals (e.getMessage(), "Se ha de introducir una longitud valida (Entre: -180,180)");
		}
		
		try {
			hotel1= service.createHotel("hotel", "zurbano", 1, 200, 0,10,10);
			fail("Deberia saltar la excepcion.");
		
		}catch(BookingServiceException e) {
			assertEquals (e.getMessage(), "Se ha de introducir una longitud valida (Entre: -180,180)");
		}
		
		
		//Prueba latitud
		
		
		try {
			hotel1= service.createHotel("hotel", "zurbano", 1, 0, -100,10,10);
			fail("Deberia saltar la excepcion.");
		
		}catch(BookingServiceException e) {
			assertEquals (e.getMessage(), "Se ha de introducir una latitud valida (Entre: -90,90)");
		}
		
		try {
			hotel1= service.createHotel("hotel", "zurbano", 1, 2, 100,10,10);
			fail("Deberia saltar la excepcion.");
		
		}catch(BookingServiceException e) {
			assertEquals (e.getMessage(), "Se ha de introducir una latitud valida (Entre: -90,90)");
		}
		
		
		
		//Prueba habitaciones
		
		try {
			hotel1= service.createHotel("hotel", "zurbano", 1, 2, 1,0,0);
			fail("Deberia saltar la excepcion.");
		
		}catch(BookingServiceException e) {
			assertEquals (e.getMessage(), "El numero de habitaciones no puede ser 0");
		}
		try {
			hotel1= service.createHotel("hotel", "zurbano", 1, 2, 1,-10,10);
			fail("Deberia saltar la excepcion.");
		
		}catch(BookingServiceException e) {
			assertEquals (e.getMessage(), "El numero de habitacion dobles no es valido");
		}
		try {
			hotel1= service.createHotel("hotel", "zurbano", 1, 2, 1,101,10);
			fail("Deberia saltar la excepcion.");
		
		}catch(BookingServiceException e) {
			assertEquals (e.getMessage(), "El numero de habitacion dobles no es valido");
		}
		
		try {
			hotel1= service.createHotel("hotel", "zurbano", 1, 2, 1,10,-10);
			fail("Deberia saltar la excepcion.");
		
		}catch(BookingServiceException e) {
			assertEquals (e.getMessage(), "El numero de habitacion individuales no es valido");
		}
		try {
			hotel1= service.createHotel("hotel", "zurbano", 1, 2, 1,10,101);
			fail("Deberia saltar la excepcion.");
		
		}catch(BookingServiceException e) {
			assertEquals (e.getMessage(), "El numero de habitacion individuales no es valido");
		}

	}
	
	@Test 
	@Order(3)
	void C_testFindHotel(){
		
		JPABookingService service = new JPABookingService();
		try {
			Hotel hotel = service.createHotel("hotel", "josea", 1, 1, 1, 1, 1);
			assertNotNull(hotel);
			Hotel hotel1=service.findHotel(1L);
			assertNotNull(hotel1);
			
			Hotel hotelno = service.findHotel(10L);
			assertNull(hotelno);
		}catch(BookingServiceException e){
			e.printStackTrace();
		}
	
	}
	
	
	@Test
	@Order(4)
	void D_testOcupiedDoubleRooms(){
		
	}

}
