public class Game {
    private final int gameId;
    private final String name;
    private final String genre;
    private final boolean active;

    public Game(int gameId, String name, String genre, boolean active) {
        this.gameId = gameId;
        this.name = name;
        this.genre = genre;
        this.active = active;
    }
    public int getGameId() { return gameId; }
    public String getName() { return name; }
    public String getGenre() { return genre; }
    public boolean isActive() { return active; }
    @Override public String toString() { return name; }
}
