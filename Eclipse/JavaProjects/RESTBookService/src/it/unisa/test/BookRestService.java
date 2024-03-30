/**
 * 
 */
package it.unisa.test;

import java.util.List;

import it.unisa.entity.Book;
import it.unisa.qualifier.Resource;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author pipp8
 *
 */
@Path("/bookservice")
public class BookRestService{

		@Inject
		private EntityManager em;
		
		@GET
		@Produces(MediaType.APPLICATION_JSON)
		@Path("status")
		public Response getStatus() {
			return Response.ok("{\"status\":\"Server is running ...\"}").build();
		}


		@GET
		@Produces(MediaType.APPLICATION_JSON)
		@Path("books")
		public Response getBooks() {
			String response = null;
			em = Resource.getEntityManager();
			Query query = em.createQuery("FROM it.unisa.entity.Boook");
			List<Book> list = query.getResultList();
			em.close();
			// Build response
			response = JSON2String(list);
			return Response.ok(response).build();
		}


		public String JSON2String(Object obj) {
			GsonBuilder gsonBuilder = new GsonBuilder();
			gsonBuilder.setDateFormat("yyy-MM-dd'T'HH:mm.ss.SSS'Z'"); // ISO8601 UTC
			
			Gson gson = gsonBuilder.create();
			return gson.toJson(obj);
		}
}
