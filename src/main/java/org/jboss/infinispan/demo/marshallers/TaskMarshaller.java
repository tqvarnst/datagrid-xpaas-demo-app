package org.jboss.infinispan.demo.marshallers;

import java.io.IOException;
import java.util.Date;

import org.infinispan.protostream.MessageMarshaller;
import org.jboss.infinispan.demo.model.Task;

public class TaskMarshaller implements MessageMarshaller<Task> {

	@Override
	public Class<Task> getJavaClass() {
		return Task.class;
	}

	@Override
	public String getTypeName() {
		return "todo.Task";
	}

	@Override
	public Task readFrom(ProtoStreamReader reader) throws IOException {
		long id = reader.readLong("id");
		String title = reader.readString("title");
		String user = reader.readString("user");
		boolean done = reader.readBoolean("done");
		long createdOn = reader.readLong("createdOn");
		long completedOn= done ? reader.readLong("completedOn") : 0;
		
		Task task = new Task();
		task.setId(id);
		task.setTitle(title);
		task.setUser(user);
		task.setDone(done);
		task.setCreatedOn(new Date(createdOn));
		if(done)
			task.setCompletedOn(new Date(completedOn));
		return task;
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, Task task)
			throws IOException {
		writer.writeLong("id", task.getId());
		writer.writeString("title",	task.getTitle());
		writer.writeString("user",	task.getUser());
		writer.writeBoolean("done", task.isDone());
		writer.writeLong("createdOn", task.getCreatedOn().getTime());
		if(task.isDone()) {
			writer.writeLong("completedOn", task.getCompletedOn().getTime());
		}
	}

}
