/***
 * Copyright (c) 2009 Caelum - www.caelum.com.br/opensource
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.com.caelum.vraptor.jpa;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import br.com.caelum.vraptor.environment.Environment;

/**
 * An {@link EntityManager} producer, that creates an instance for each request.
 * 
 * @author Lucas Cavalcanti
 * @author Otávio Garcia
 */
public class EntityManagerFactoryCreator {
	@Inject
	private Environment environment;
	@Inject
	private Properties propertiesOfJPAConnection;

	
	/**
	 * Produces EntityManagerFactory.  For default  is created an connect 
	 * based on configurations of the persistence.xml. 
	 * However, if the database properties are unset in persistence.xml,  
	 * the connection will created programmatically by propertiesOfJPAConnection,
	 * overriding the default settings.
	 * 
	 * @return the EntityManagerFactory that will create all Entity managers
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	@ApplicationScoped
	@Produces
	public EntityManagerFactory getEntityManagerFactory(){
		EntityManagerFactory factory = null;
		String persistenceUnit = environment.get("br.com.caelum.vraptor.jpa.persistenceunit", "default");
		
		if(!propertiesOfJPAConnection.isEmpty() && !hasXMLConfiguration(persistenceUnit)) {
			factory = Persistence.createEntityManagerFactory(persistenceUnit, propertiesOfJPAConnection);
		} else {
			factory = Persistence.createEntityManagerFactory(persistenceUnit);
		}
		return factory;			
	}
	
	/**
	 * Checks if exist properties of persistence unit in persistence.xml
	 * 
	 * @param persistenceUnit to validate
	 * @return true if settings exist, this not checks if are invalids
	 */
	private boolean hasXMLConfiguration(String persistenceUnit){
		final String url = "hibernate.connection.url";
		EntityManagerFactory toValidate = Persistence.createEntityManagerFactory(persistenceUnit);
		if(toValidate.getProperties().containsKey(url)){
			return true;
		}else{
			System.err.println("Vraptor-JPA: Try create connection programmatically...");
			return false;
		}
	}

	public void destroy(@Disposes EntityManagerFactory factory) {
		if (factory.isOpen()) {
			factory.close();
		}
	}
}
