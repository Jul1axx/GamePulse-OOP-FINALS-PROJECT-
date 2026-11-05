public class AnalyticsData {
    private final long todaySeconds, weekSeconds, averageSeconds;
    private final String topGame;
    private final int exceededSessions;
    public AnalyticsData(long todaySeconds, long weekSeconds, long averageSeconds, String topGame, int exceededSessions) {
        this.todaySeconds=todaySeconds; this.weekSeconds=weekSeconds; this.averageSeconds=averageSeconds; this.topGame=topGame; this.exceededSessions=exceededSessions;
    }
    public long getTodaySeconds() { return todaySeconds; }
    public long getWeekSeconds() { return weekSeconds; }
    public long getAverageSeconds() { return averageSeconds; }
    public String getTopGame() { return topGame; }
    public int getExceededSessions() { return exceededSessions; }
}
