import java.time.Duration;
import java.time.LocalDateTime;

public class GameSession {
    private final long sessionId;
    private final int userId;
    private final String username;
    private final int gameId;
    private final String gameName;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final long durationSeconds;
    private final int limitMinutesAtStart;

    public GameSession(long sessionId, int userId, String username, int gameId, String gameName,
                       LocalDateTime startTime, LocalDateTime endTime, long durationSeconds, int limitMinutesAtStart) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.username = username;
        this.gameId = gameId;
        this.gameName = gameName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationSeconds = durationSeconds;
        this.limitMinutesAtStart = limitMinutesAtStart;
    }
    public long getSessionId() { return sessionId; }
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public int getGameId() { return gameId; }
    public String getGameName() { return gameName; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public long getDurationSeconds() {
        return endTime == null ? Math.max(0, Duration.between(startTime, LocalDateTime.now()).getSeconds()) : durationSeconds;
    }
    public int getLimitMinutesAtStart() { return limitMinutesAtStart; }
    public boolean isActive() { return endTime == null; }
}
