package paa.reservas.persistence;

import java.util.List;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.Query;

public class JPADAO<T, K> implements DAO<T, K> {
    

	protected EntityManager em; //gestor de entidades
    protected Class<T> clazz;

    public JPADAO(EntityManager em, Class<T> entityClass) {
        this.clazz = entityClass;
        this.em = em;
    }

    @Override
    public T findById(K id) {

    	return em.find(clazz, id);
    	
    }

    @Override
    public T create(T t) {
       
    	try {
    		em.persist(t);
    		em.flush();
    		em.refresh(t);
    		return t;
    		} catch (EntityExistsException ex) {
    		throw new DAOException("La entidad ya existe", ex);
    		}
    	
    }

    @Override
    public T update(T t) {

    	 return (T) em.merge(t);
    	
    }

    @Override
    public void delete(T t) {

    	t = em.merge(t); em.remove(t);
    	
    }

    @SuppressWarnings("unchecked")
	@Override
    public List<T> findAll() {
        // Complete este método, que debe listar todos los elementos de la clase T.
        // Necesitará hacer consultas a la base datos mediante una TypedQuery, bien
        // empleando una sentencia JPQL o una CriteriaQuery
    	
    	List<T> listado = null;
    	Query q = em.createQuery("select t from " + clazz.getName() + " t ");
    	listado = q.getResultList();
    	return listado;

    	
    	
    	
   }
}


