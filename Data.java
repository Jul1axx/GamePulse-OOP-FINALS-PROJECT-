public class Data {
    private String Game;
    private int SessionTimeIn;
    private int SessionTimeOut;
    private int SessionLimit;
    private int SessionFrequency;
    private String data;

    public Data(String game, int sessionTimeIn, int sessionTimeOut, int sessionLimit, int sessionFrequency, String data) {
        this.Game = game;
        this.SessionTimeIn = sessionTimeIn;
        this.SessionTimeOut = sessionTimeOut;
        this.SessionLimit = sessionLimit;
        this.SessionFrequency = sessionFrequency;
        this.data = data;
    }
}