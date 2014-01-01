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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import ua.ksn.crypt.Simple3DES;
import ua.ksn.fmg.controller.event.ChampionModelEvent;
import ua.ksn.fmg.controller.event.ChampionModelListener;
import ua.ksn.fmg.controller.event.PlayerModelEvent;
import ua.ksn.fmg.controller.event.PlayerModelListener;
import ua.ksn.fmg.controller.types.ESkillLevel;
import ua.ksn.fmg.controller.types.User;
import ua.ksn.fmg.model.mosaics.EMosaic;

/** ��������� ��������� */
public class ChampionsModel implements Externalizable {
	//private static final long version = Main.serialVersionUID;
	private final long version;

	private static final int MAX_SIZE = 10;

	class Record implements Externalizable, Comparable<Record> {
		private UUID userId;
		private String userName;
		private long playTime = Integer.MAX_VALUE;

		public Record() {}
		public Record(User user, long playTime) {
			this.userId = user.getGuid();
			this.userName = user.getName();
			this.playTime  = playTime;
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			userId = UUID.fromString(in.readUTF());
			userName = in.readUTF();
			playTime = in.readLong();
		}
		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeUTF(userId.toString());
			out.writeUTF(userName);
			out.writeLong(playTime);
		}

		@Override
		public String toString() {
			return userName;
		}

		@Override
		public int compareTo(Record o) {
			long thisVal = this.playTime;
			long anotherVal = o.playTime;
			return (thisVal<anotherVal ? -1 : (thisVal==anotherVal ? 0 : 1));
		}
	}

	@SuppressWarnings("unchecked")
	private List<ChampionsModel.Record>[][] champions = new List[EMosaic.values().length][ESkillLevel.values().length-1];

	public ChampionsModel(long version, final PlayersModel players) {
		this.version = version;
		if (players != null)
			players.addPlayerListener(new PlayerModelListener() {
				@Override
				public void playerChanged(PlayerModelEvent e) {
					if (e.getType() == PlayerModelEvent.UPDATE) {
						// ���� ��� UPDATE, �� ���, �������� ���, ���� �������������� ������������...
						// � ���� ������ �������������� ��� ��� � � ���������
						User user = players.getUser(e.getPos());
						for (EMosaic mosaic : EMosaic.values())
							for (ESkillLevel eSkill : ESkillLevel.values())
								if (eSkill != ESkillLevel.eCustom) {
									List<ChampionsModel.Record> list = champions[mosaic.ordinal()][eSkill.ordinal()];
									boolean isChanged = false;
									for (Record record : list)
										if ((user.getGuid() == record.userId) && !user.getName().equals(record.userName))
										{
											record.userName = user.getName();
											isChanged = true;
										}
									if (isChanged)
										ChampionsModel.this.fireChanged(new ChampionModelEvent(ChampionsModel.this, mosaic, eSkill, ChampionModelEvent.POS_ALL, ChampionModelEvent.UPDATE));
								}
					}
				}
			});

		for (EMosaic mosaic : EMosaic.values())
			for (ESkillLevel eSkill : ESkillLevel.values())
				if (eSkill != ESkillLevel.eCustom)
					champions[mosaic.ordinal()][eSkill.ordinal()] = new ArrayList<ChampionsModel.Record>(MAX_SIZE);
	}

	public int add(User user, long playTime, EMosaic mosaic, ESkillLevel eSkill) {
		if (eSkill == ESkillLevel.eCustom)
			return -1;

		List<ChampionsModel.Record> list = champions[mosaic.ordinal()][eSkill.ordinal()];
		Record newRecord = new Record(user, playTime); 
		list.add(newRecord);

		Collections.sort(list);

		int pos = list.indexOf(newRecord);
		if (pos == -1)
			throw new RuntimeException("Where??");

		if (list.size() > MAX_SIZE)
			//list = list.subList(0, MAX_SIZE-1);
			list.subList(MAX_SIZE, list.size()).clear();

		fireChanged(new ChampionModelEvent(this, mosaic, eSkill, ChampionModelEvent.POS_ALL, ChampionModelEvent.UPDATE));
		if (pos < MAX_SIZE) {
			fireChanged(new ChampionModelEvent(this, mosaic, eSkill, pos, ChampionModelEvent.INSERT));
			return pos;
		}
		return -1;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		for (EMosaic mosaic : EMosaic.values())
			for (ESkillLevel eSkill : ESkillLevel.values())
				if (eSkill != ESkillLevel.eCustom) {
					List<ChampionsModel.Record> list = champions[mosaic.ordinal()][eSkill.ordinal()];
					out.writeInt(list.size());
					for (Record record : list)
						record.writeExternal(out);
				}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		setDefaults();
		for (EMosaic mosaic : EMosaic.values())
			for (ESkillLevel eSkill : ESkillLevel.values())
				if (eSkill != ESkillLevel.eCustom) {
					List<ChampionsModel.Record> list = champions[mosaic.ordinal()][eSkill.ordinal()];
					int size = in.readInt();
					for (int i=0; i<size; i++) {
						Record record = new Record();
						record.readExternal(in);
						list.add(record);
					}
					fireChanged(new ChampionModelEvent(this, mosaic, eSkill, ChampionModelEvent.POS_ALL, ChampionModelEvent.INSERT));
				}
	}

	private void setDefaults() {
		for (EMosaic mosaic : EMosaic.values())
			for (ESkillLevel eSkill : ESkillLevel.values())
				if (eSkill != ESkillLevel.eCustom) {
					List<ChampionsModel.Record> list = champions[mosaic.ordinal()][eSkill.ordinal()];
					list.clear();
					fireChanged(new ChampionModelEvent(this, mosaic, eSkill, ChampionModelEvent.POS_ALL, ChampionModelEvent.DELETE));
				}
	}

	/**
	 * Load BST data from file
	 * @return <b>true</b> - successful read; <b>false</b> - not exist or fail read, and set to defaults
	 */
	public boolean Load() {
		File file = getChampFile();
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
				throw new IOException("Invalid data length. ��������� " + data.length + " ����, � ��������� " + read + " ����.");
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
		out = new ObjectOutputStream(new FileOutputStream(getChampFile()));
		out.writeLong(version); // save version and decrypt key
		int len = cryptData.length;
		out.writeInt(len);
		out.write(cryptData);

		out.flush();
		out.close();
	}

	public static File getChampFile() {
		return new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "Mines.bst");
	}

	public void addChampionListener(ChampionModelListener l) {
		arrChampionListener.add(l);
	}
	public void removeTableModelListener(ChampionModelListener l) {
		arrChampionListener.remove(l);
	}
	private List<ChampionModelListener> arrChampionListener = new ArrayList<ChampionModelListener>();
	private void fireChanged(ChampionModelEvent e) {
		for (ChampionModelListener listener: arrChampionListener)
			listener.championChanged(e);
	}

	public String getUserName(int index, EMosaic mosaic, ESkillLevel eSkill) {
		if (eSkill == ESkillLevel.eCustom)
			throw new IllegalArgumentException("Invalid input data - " + eSkill);
		return champions[mosaic.ordinal()][eSkill.ordinal()].get(index).userName;
	}
	public long getUserPlayTime(int index, EMosaic mosaic, ESkillLevel eSkill) {
		if (eSkill == ESkillLevel.eCustom)
			throw new IllegalArgumentException("Invalid input data - " + eSkill);
		return champions[mosaic.ordinal()][eSkill.ordinal()].get(index).playTime;
	}
	public int getUsersCount(EMosaic mosaic, ESkillLevel eSkill) {
		if (eSkill == ESkillLevel.eCustom)
			throw new IllegalArgumentException("Invalid input data - " + eSkill);
		return champions[mosaic.ordinal()][eSkill.ordinal()].size();
	}

	/** ����� ������� ������� ���������� ���������� ������������ */
	public int getPos(UUID userId, EMosaic mosaic, ESkillLevel eSkill) {
		if (userId == null)
			return -1;
		if (eSkill == ESkillLevel.eCustom)
			throw new IllegalArgumentException("Invalid input data - " + eSkill);

		List<ChampionsModel.Record> list = champions[mosaic.ordinal()][eSkill.ordinal()];
		int pos = 0;
		for (Record record : list) {
			if (record.userId.equals(userId))
				return pos;
			pos++;
		}
		return -1;
	}
}