package fmg.swing.app.serializers;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.UUID;

import fmg.core.app.ISerializer;
import fmg.core.types.model.User;

public class UserSerializer implements ISerializer {

    private static final long VERSION = 1;

    public void write(User user, ObjectOutput out) throws IOException {
        out.writeLong(VERSION);
        out.writeUTF(user.getGuid().toString());
        out.writeUTF(user.getName());

        String pass = user.getPassword();
        boolean exist = (pass != null) && !pass.isEmpty();
        out.writeBoolean(exist);
        if (exist)
            out.writeUTF(pass);

        exist = (user.imgAvatar != null) && !user.imgAvatar.isEmpty();
        out.writeBoolean(exist);
        if (exist)
            out.writeUTF(user.imgAvatar);
    }

    public User read(ObjectInput in) throws IOException {
        long ver = in.readLong();
        if (ver != VERSION)
            throw new IllegalArgumentException(UserSerializer.class.getSimpleName() + ": Unsupported version #" + ver);

        User user = new User();
        user.setGuid(UUID.fromString(in.readUTF()));
        user.setName(in.readUTF());

        boolean exist = in.readBoolean();
        if (exist)
            user.setPassword(in.readUTF());

        exist = in.readBoolean();
        if (exist)
            user.imgAvatar = in.readUTF();

        return user;
    }

}
