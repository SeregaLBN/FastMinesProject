package fmg.core.app.model;

public class Statistics {

    /** количество сыгранных игр */
    public long gameNumber;
    /** количество выиграных игр */
    public long gameWin;
    /** суммарное число открытых ячеек - вывожу средний процент открытия поля */
    public long openField;
    /** суммарное время игр (milliseconds) - вывожу сколько всреднем игрок провёл времени за данной игрой */
    public long playTime;
    /** суммарное число кликов - вывожу среднее число кликов в данной игре */
    public long clickCount;

    /** clone */
    public Statistics getCopy() {
        Statistics res = new Statistics();
        res.gameNumber = this.gameNumber;
        res.gameWin    = this.gameWin;
        res.openField  = this.openField;
        res.playTime   = this.playTime;
        res.clickCount = this.clickCount;
        return res;
    }

}