package it.etuitus.service;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


@Path("/OTPServices")
public class OTPServices {

	
    @GET
    @Path("/step1")
	@Produces(MediaType.APPLICATION_JSON)
    public Response getLogin( @QueryParam("user") String user,	@QueryParam("password") String password) {
    	
    	String response = null;
    	int status;
    	
    	if (user.equals(password)) {
        	response = "{\"msg\":\"Login OK\", \"user\":\"" + user + "\",\"password\":\"" + password +"\"}";
            status = 200;
    	}
    	else {
    		response = "{\"msg\":\"Login Failed\", \"user\":\"" + user + "\",\"password\":\"" + password +"\"}";
    		status = 400;
    	}
        return Response.status(status).entity(response).build();
    }

    
    
    @GET
    @Path("/step2")
	@Produces(MediaType.APPLICATION_JSON)
    public Response getSeed( @QueryParam("user") String user, @QueryParam("otp") String otp) { 

    	String response = null;
    	int status;
    	
    	if (user.equals(otp)) {
    		SeedResponse s = new SeedResponse();
    		
        	response = JSON2String(s);
        	status = 200;
    	}
    	else {
    		response = "{\"msg\":\"OTP Login Failed\", \"user\":\"" + user + "\",\"password\":\"" + otp +"\"}";
    		status = 400;
    	}
        return Response.status(status).entity(response).build(); 
    }

    
	public String JSON2String(Object obj) {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.setDateFormat("yyy-MM-dd'T'HH:mm.ss.SSS'Z'"); // ISO8601 UTC
		
		Gson gson = gsonBuilder.create();
		return gson.toJson(obj);
	}
	
}
