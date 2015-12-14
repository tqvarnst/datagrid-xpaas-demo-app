package org.jboss.infinispan.demo;

import java.util.Map;

import javax.enterprise.inject.Produces;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.jboss.infinispan.demo.model.Task;

/**
 * This class produces configured cache objects via CDI
 *  
 * @author tqvarnst
 *
 */
public class Config {

	
	/**
	 * DONE: Add a Producer for org.infinispan.client.hotrod.RemoteCache<Long, Task> 
	 * 		  using org.infinispan.client.hotrod.configuration.ConfigurationBuilder
	 * 		  and org.infinispan.client.hotrod.RemoteCacheManager
	 * 
	 * @return org.infinispan.client.hotrod.RemoteCache<Long, Task>
	 */
	@Produces @MyCache
	public RemoteCache<Long, Task> getRemoteCache() throws DataGridConfigurationException{
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.addServer()
			.host(getHotRodHostFromEnvironment())
			.port(getHotRodPortFromEnvironment());
		return new RemoteCacheManager(builder.build(), true).getCache("default");
	}
	
	private static String getHotRodHostFromEnvironment() throws DataGridConfigurationException {
		String hotrodServiceName = System.getenv("JDG_SERVICE_NAME");
		if(hotrodServiceName == null) {
			throw new DataGridConfigurationException("Failed to get JDG Service Name from environment variables. please make sure that you set this value before starting the container");
		}
		String hotRodHost=System.getenv(hotrodServiceName.toUpperCase() + "_HOTROD_SERVICE_HOST");
		if(hotRodHost == null) {
			throw new DataGridConfigurationException(String.format("Failed to get hostname/ip address for service %s",hotrodServiceName));
		}
		return hotRodHost;
	}
	
	private static int getHotRodPortFromEnvironment() throws DataGridConfigurationException {
		String hotrodServiceName = System.getenv("JDG_SERVICE_NAME");
		if(hotrodServiceName == null) {
			throw new DataGridConfigurationException("Failed to get JDG Service Name from environment variables. please make sure that you set this value before starting the container");
		}
		String hotRodPort=System.getenv(hotrodServiceName.toUpperCase() + "_HOTROD_SERVICE_PORT");
		if(hotRodPort == null) {
			throw new DataGridConfigurationException(String.format("Failed to get Hot Rod Port for service %s",hotrodServiceName));
		}
		return Integer.parseInt(hotRodPort);
	}
	
	public static class DataGridConfigurationException extends Exception
    {
		private static final long serialVersionUID = -4667039447165906505L;
		public DataGridConfigurationException(String msg) {
            super(msg);
        }
    }
}
