package ua.ksn.fmg.controller.serializable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ua.ksn.crypt.Simple3DES;
import ua.ksn.fmg.controller.event.PlayerModelEvent;
import ua.ksn.fmg.controller.event.PlayerModelListener;
import ua.ksn.fmg.controller.types.ESkillLevel;
import ua.ksn.fmg.controller.types.User;
import ua.ksn.fmg.model.mosaics.EMosaic;

/** хранилище пользователей и их игровой статистики */
public class PlayersModel implements Externalizable {
	//private static final long version = Main.serialVersionUID;
	private final long version;
	
	public PlayersModel(long version) { this.version = version; }

	private class Record implements Externalizable {
		private User user;
		private StatisticCounts statistics[][] = new StatisticCounts[EMosaic.values().length][ESkillLevel.values().length-1];

		/** new User */
		public Record(User user) {
			this.user = user;
			for (EMosaic mosaic : EMosaic.values())
				for (ESkillLevel skill : ESkillLevel.values())
					if (skill == ESkillLevel.eCustom)
						continue;
					else
						statistics[mosaic.ordinal()][skill.ordinal()] = new StatisticCounts();
		}
		/** from file */
		public Record(ObjectInput in) throws IOException {
			try {
				for (EMosaic mosaic : EMosaic.values())
					for (ESkillLevel skill : ESkillLevel.values())
						if (skill == ESkillLevel.eCustom)
							continue;
						else
							statistics[mosaic.ordinal()][skill.ordinal()] = new StatisticCounts();
				readExternal(in);
			} catch (ClassNotFoundException ex) {
				throw new RuntimeException(ex);
			}
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			user = new User(in);
			for (StatisticCounts[] record: statistics)
				for (StatisticCounts subRecord: record)
					subRecord.readExternal(in);
		}
		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			user.writeExternal(out);
			for (StatisticCounts[] record: statistics)
				for (StatisticCounts subRecord: record)
					subRecord.writeExternal(out);
		}
		@Override
		public String toString() {
			return user.getName();
		}
	}

	private List<PlayersModel.Record> players = new ArrayList<PlayersModel.Record>();

	public void removePlayer(UUID userId) {
		Record rec = find(userId);
		if (rec == null)
			throw new IllegalArgumentException("User " + userId + " not exist");
		int pos = players.indexOf(rec);
		players.remove(pos);

		fireChanged(new PlayerModelEvent(this, pos, PlayerModelEvent.DELETE));
	}
	public boolean isExist(UUID userId) { return find(userId) != null; }
	public int size() { return players.size(); };
	public UUID addNewPlayer(String name, String pass) {
		if ((name == null) || name.isEmpty())
			throw new IllegalArgumentException("Invalid player name. Need not empty.");
		for (Record rec: players)
			if (rec.user.getName().equalsIgnoreCase(name))
				throw new IllegalArgumentException("Please enter a unique name");

		User user = new User(name, pass, null);
		players.add(new PlayersModel.Record(user));
		fireChanged(new PlayerModelEvent(this, players.size()-1, PlayerModelEvent.INSERT));
		return user.getGuid();
	}
	public int indexOf(User user) {
		Record recFind = null;
		for (Record rec: players)
			if (rec.user.getGuid().equals(user.getGuid())) {
				recFind = rec;
				break;
			}
		if (recFind == null)
			return -1;
		return players.indexOf(recFind);
	}

	/**
	 * Установить статистику для игрока
	 * @param userId - идентификатор игрока
	 * @param mosaic - на какой мозаике
	 * @param skill - на каком уровне сложности
	 * @param victory - выиграл ли?
	 * @param countOpenField - кол-во открытых ячеек
	 * @param playTime - время игры
	 * @param clickCount - кол-во кликов
	 */
	public void setStatistic(UUID userId, EMosaic mosaic, ESkillLevel skill, boolean victory, long countOpenField, long playTime, long clickCount) {
		if (skill == ESkillLevel.eCustom)
			return;
		Record rec = find(userId);
		if (rec == null)
			throw new IllegalArgumentException("User " + userId + " not exist");

		StatisticCounts subRec = rec.statistics[mosaic.ordinal()][skill.ordinal()];
		subRec.gameNumber++;
		subRec.gameWin    += victory ? 1:0;
		subRec.openField  += countOpenField;
		if (victory) {
			subRec.playTime   += playTime;
			subRec.clickCount += clickCount;
		}

		int pos = players.indexOf(rec);
		fireChanged(new PlayerModelEvent(this, pos, PlayerModelEvent.CHANGE_STATISTICS, mosaic, skill));
	}
	private Record find(UUID userId) {
		if (userId != null)
			for (Record rec: players)
				if (rec.user.getGuid().equals(userId))
					return rec;
		return null;
	}
	public User getUser(int pos) {
		if ((pos < 0) || (pos>=players.size()))
			throw new IllegalArgumentException("Invalid position " + pos);
			//return null;
		return players.get(pos).user;
	}
	public User getUser(UUID userId) {
		Record rec = find(userId);
		if (rec == null)
			throw new IllegalArgumentException("User " + userId + " not exist");
		return rec.user;
	}
	public StatisticCounts getInfo(UUID userId, EMosaic mosaic, ESkillLevel skillLevel) {
		Record rec = find(userId);
		if (rec == null)
			throw new IllegalArgumentException("User " + userId + " not exist");

		try {
			return rec.statistics[mosaic.ordinal()][skillLevel.ordinal()].clone();
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
		}
	}
	public int getPos(UUID userId) {
		if (userId == null)
			return -1;
		Record rec = find(userId);
		if (rec == null)
			return -1;
		return players.indexOf(rec);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(version);
		out.writeInt(players.size());
		for (PlayersModel.Record rec : players)
			rec.writeExternal(out);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		setDefaults();
		if (version != in.readLong())
			throw new RuntimeException("Unknown version");
		int size = in.readInt();
		for (int i=0; i<size; i++)
			players.add(new PlayersModel.Record(in));

		fireChanged(new PlayerModelEvent(this, players.size()-1, PlayerModelEvent.INSERT_ALL));
	}

	private void setDefaults() {
		int len = players.size();
		players.clear();
		fireChanged(new PlayerModelEvent(this, len-1, PlayerModelEvent.DELETE_ALL));
	}

	/**
	 * Load STC data from file
	 * @return <b>true</b> - successful read; <b>false</b> - not exist or fail read, and set to defaults
	 */
	public boolean Load() {
		File file = getStcFile();
		if (!file.exists()) {
			setDefaults();
			return false;
		}

		try {
			// 1. read from file
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
			if (in.readLong() != version)
				throw new RuntimeException("Invalid file data - unknown version");
			byte[] data = new byte[in.readInt()];
			int read = 0;
			do {
				int curr = in.read(data, read, data.length-read);
				if (curr == -1)
					break;
				read += curr;
			} while(read < data.length);
			if (read != data.length)
				throw new IOException("Invalid data length. Ожидалось " + data.length + " байт, а прочитано " + read + " байт.");
			in.close();

			// 2. decrypt data
			try {
				data = new Simple3DES(Long.toString(version)).decrypt(data);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}

			// 3. deserializable object
			in = new ObjectInputStream(new ByteArrayInputStream(data));
			this.readExternal(in);

			in.close();

			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			setDefaults();
			return false;
		}
	}

	public void Save() throws FileNotFoundException, IOException {
		// 1. serializable object
		ByteArrayOutputStream byteRaw = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(byteRaw);
		this.writeExternal(out);
		out.flush();

		// 2. crypt data
		byte[] cryptData;
		try {
			cryptData = new Simple3DES(Long.toString(version)).encrypt(byteRaw.toByteArray());
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}

		// 3. write to file
		out = new ObjectOutputStream(new FileOutputStream(getStcFile()));
		out.writeLong(version); // save version and decrypt key
		int len = cryptData.length;
		out.writeInt(len);
		out.write(cryptData);

		out.flush();
		out.close();
	}

	/**
	 * STatistiCs file name
	 */
	public static File getStcFile() {
		return new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "Mines.stc");
	}

	public void addPlayerListener(PlayerModelListener l) {
		arrPlayerListener.add(l);
	}
	public void removeTableModelListener(PlayerModelListener l) {
		arrPlayerListener.remove(l);
	}
	private List<PlayerModelListener> arrPlayerListener = new ArrayList<PlayerModelListener>();
	private void fireChanged(PlayerModelEvent e) {
		for (PlayerModelListener listener: arrPlayerListener)
			listener.playerChanged(e);
	}
	public void setUserName(int pos, String name) {
		User user = getUser(pos);
		user.setName(name);
		fireChanged(new PlayerModelEvent(this, pos, PlayerModelEvent.UPDATE));
	}
}