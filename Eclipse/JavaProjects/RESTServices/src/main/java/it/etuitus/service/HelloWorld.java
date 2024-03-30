package it.etuitus.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.DefaultValue;



@Path("/RESTEasySample")
public class HelloWorld {
 
    @GET
    @Path("/Greetings")
	@Produces(MediaType.APPLICATION_JSON)
    public Response getHelloWorld( @DefaultValue("Nothing to say") @QueryParam("Param1") String Param1) { 

    	String response = "{\"Param1\":\"" + Param1 + "\"}";

        return Response.status(200).entity(response).build();
    }
}

