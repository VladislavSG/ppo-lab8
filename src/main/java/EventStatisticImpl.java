import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EventStatisticImpl implements EventStatistic {
    private final long WINDOW_MIN = 60;

    private final Clock clock;
    private final Map<String, List<Instant>> events = new HashMap<>();

    public EventStatisticImpl(Clock clock) {
        this.clock = clock;
    }

    @Override
    public void incEvent(String name) {
        events.compute(name, (key, value) -> {
            if (value == null) {
                value = new ArrayList<>();
            }
            value.add(clock.instant());
            return value;
        });
    }

    @Override
    public double getEventStatisticByName(String name) {
        cleanOld();
        return calculateEventStat(events.getOrDefault(name, Collections.emptyList()));
    }

    private double calculateEventStat(List<Instant> instants) {
        return instants.size() / (double) WINDOW_MIN;
    }

    @Override
    public Map<String, Double> getAllEventStatistic() {
        cleanOld();
        return events.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> calculateEventStat(entry.getValue())
                ));
    }

    @Override
    public void printStatistic() {
        Map<String, Double> statistic = getAllEventStatistic();

        for (String name : statistic.keySet()) {
            System.out.printf("requests per minute for %s = %f%n", name, statistic.get(name));
        }
    }

    private void cleanOld() {
        Instant hourAgo = clock.instant().minus(WINDOW_MIN, ChronoUnit.MINUTES);

        events.replaceAll(
            (key, value) -> value.stream()
                .filter(instant -> instant.isAfter(hourAgo))
                .collect(Collectors.toList())
        );
        events.values().removeIf(List::isEmpty);
    }
}
