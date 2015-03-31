package fmg.data.controller.types;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.UUID;

public class User implements Externalizable {
	/** неизменный id пользователя */
	private UUID  guid;
	/** юзер может менять и имя ... */
	private String name;
	/** ... и пароль */
	private String password;
	public String imgAvatar;

	/** new User */
	public User(String name, String password, String imgAvatar) {
		this.guid = UUID.randomUUID();

		if ((name == null) || name.isEmpty())
			throw new RuntimeException("Invalid player name. Need not empty.");
		this.name = name;

		if ((password != null) && password.isEmpty())
			password = null;
		this.password = password;

		if ((imgAvatar != null) && imgAvatar.isEmpty())
			imgAvatar = null;
		this.imgAvatar = imgAvatar;
	}

	/** load from file */
	public User(ObjectInput in) throws IOException {
		try {
			readExternal(in);
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException(ex);
		}
	}

	public UUID getGuid() { return guid; }
	public String getName() { return name; }
	public void setName(String name) {
		if (name == null)
			throw new IllegalArgumentException("Invalid player name. Need not empty.");
		if (name.isEmpty())
			throw new IllegalArgumentException("Invalid player name. Need not empty.");

		this.name = name;
	}
	
	@Override
	public String toString() {
//		return super.toString();
		return name + "; passw " + (((password!=null) && !password.isEmpty()) ? "is exist" : "not exist");
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		guid = UUID.fromString(in.readUTF());
		name = in.readUTF();

		boolean exist = in.readBoolean();
		if (exist)
			password = in.readUTF();

		exist = in.readBoolean();
		if (exist)
			imgAvatar = in.readUTF();
	}
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(guid.toString());
		out.writeUTF(name);

		boolean exist = (password !=null) && !password.isEmpty();
		out.writeBoolean(exist);
		if (exist)
			out.writeUTF(password);

		exist = (imgAvatar !=null) && !imgAvatar.isEmpty();
		out.writeBoolean(exist);
		if (exist)
			out.writeUTF(imgAvatar);
	}
}