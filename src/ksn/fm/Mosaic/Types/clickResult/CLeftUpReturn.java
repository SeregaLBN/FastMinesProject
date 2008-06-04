package ksn.fm.Mosaic.Types.clickResult;

public class CLeftUpReturn {
	public int m_iCountFlag, m_iCountOpen, m_iCountUnknown;
	public boolean m_bEndGame, m_bVictory;

	public CLeftUpReturn(int iCountFlag, int iCountOpen, int iCountUnknown, boolean bEndGame, boolean bVictory) {
		m_iCountFlag    = iCountFlag;
		m_iCountOpen    = iCountOpen;
		m_iCountUnknown = iCountUnknown;
		m_bEndGame      = bEndGame;
		m_bVictory      = bVictory;
	}
}
