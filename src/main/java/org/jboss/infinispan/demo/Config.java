package org.jboss.infinispan.demo;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

import javax.enterprise.inject.Produces;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.marshall.ProtoStreamMarshaller;
import org.infinispan.protostream.FileDescriptorSource;
import org.infinispan.protostream.SerializationContext;
import org.infinispan.query.remote.client.ProtobufMetadataManagerConstants;
import org.jboss.infinispan.demo.marshallers.TaskMarshaller;
import org.jboss.infinispan.demo.model.Task;

/**
 * This class produces configured cache objects via CDI
 *  
 * @author tqvarnst
 *
 */
public class Config {

	private static final String ENV_VAR_JDG_SERVICE_NAME = "JDG_SERVICE_NAME";
	private static final String ENV_VAR_SUFFIX_HOTROD_SERVICE_PORT = "_HOTROD_SERVICE_PORT";
	private static final String ENV_VAR_SUFFIX_HOTROD_SERVICE_HOST = "_HOTROD_SERVICE_HOST";

	private static final String CACHE_NAME = "default";
	private static final String PROTOBUF_DEFINITION_RESOURCE = "/todo/task.proto";
	
	
	
	/**
	 * DONE: Add a Producer for org.infinispan.client.hotrod.RemoteCache<Long, Task> 
	 * 		  using org.infinispan.client.hotrod.configuration.ConfigurationBuilder
	 * 		  and org.infinispan.client.hotrod.RemoteCacheManager
	 * 
	 * @return org.infinispan.client.hotrod.RemoteCache<Long, Task>
	 * @throws IOException 
	 * @throws DataGridConfigurationException 
	 */
	@Produces @MyCache
	public RemoteCache<Long, Task> getRemoteCache() throws IOException, DataGridConfigurationException {
		RemoteCacheManager cacheManager = this.getCacheManager();
		this.registerSchemasAndMarshallers(cacheManager);
		return cacheManager.getCache(CACHE_NAME);
	}
	
	public RemoteCacheManager getCacheManager() throws DataGridConfigurationException {
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.addServer()
			.host(getHotRodHostFromEnvironment())
			.port(getHotRodPortFromEnvironment())
			.marshaller(new ProtoStreamMarshaller());
		return new RemoteCacheManager(builder.build(), true);
	}
	
	private static String getHotRodHostFromEnvironment() throws DataGridConfigurationException {
		String hotrodServiceName = System.getenv(ENV_VAR_JDG_SERVICE_NAME);
		if(hotrodServiceName == null) {
			throw new DataGridConfigurationException("Failed to get JDG Service Name from environment variables. please make sure that you set this value before starting the container");
		}
		String hotRodHost=System.getenv(hotrodServiceName.toUpperCase() + ENV_VAR_SUFFIX_HOTROD_SERVICE_HOST);
		if(hotRodHost == null) {
			throw new DataGridConfigurationException(String.format("Failed to get hostname/ip address for service %s",hotrodServiceName));
		}
		return hotRodHost;
	}
	
	private static int getHotRodPortFromEnvironment() throws DataGridConfigurationException {
		String hotrodServiceName = System.getenv(ENV_VAR_JDG_SERVICE_NAME);
		if(hotrodServiceName == null) {
			throw new DataGridConfigurationException("Failed to get JDG Service Name from environment variables. please make sure that you set this value before starting the container");
		}
		String hotRodPort=System.getenv(hotrodServiceName.toUpperCase() + ENV_VAR_SUFFIX_HOTROD_SERVICE_PORT);
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
	
	@Produces @CurrentUser
	public String getUser() {
		return CACHE_NAME;
	}
	
	private void registerSchemasAndMarshallers(RemoteCacheManager cacheManager) throws IOException {
      // Register entity marshallers on the client side ProtoStreamMarshaller instance associated with the remote cache manager.
      SerializationContext ctx = ProtoStreamMarshaller.getSerializationContext(cacheManager);
      ctx.registerProtoFiles(FileDescriptorSource.fromResources(PROTOBUF_DEFINITION_RESOURCE));
      ctx.registerMarshaller(new TaskMarshaller());

      // register the schemas with the server too
      RemoteCache<String, String> metadataCache = cacheManager.getCache(ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME);
      metadataCache.put(PROTOBUF_DEFINITION_RESOURCE, readResource(PROTOBUF_DEFINITION_RESOURCE));
      String errors = metadataCache.get(ProtobufMetadataManagerConstants.ERRORS_KEY_SUFFIX);
      if (errors != null) {
         throw new IllegalStateException("Some Protobuf schema files contain errors:\n" + errors);
      }
	}
	
	private String readResource(String resourcePath) throws IOException {
	      InputStream is = getClass().getResourceAsStream(resourcePath);
	      try {
	         final Reader reader = new InputStreamReader(is, "UTF-8");
	         StringWriter writer = new StringWriter();
	         char[] buf = new char[1024];
	         int len;
	         while ((len = reader.read(buf)) != -1) {
	            writer.write(buf, 0, len);
	         }
	         return writer.toString();
	      } finally {
	         is.close();
	      }
	   }
}
