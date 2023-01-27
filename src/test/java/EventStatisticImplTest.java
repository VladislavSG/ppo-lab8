import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EventStatisticImplTest {
    private SetableClock clock;
    private EventStatistic eventStatistic;

    @BeforeEach
    public void setup() {
        clock = new SetableClock(Instant.now());
        eventStatistic = new EventStatisticImpl(clock);
    }

    @Test
    public void testNonExistingName() {
        assertEquals(0, eventStatistic.getEventStatisticByName("Event"));
    }

    @Test
    public void testStatisticByName() {
        eventStatistic.incEvent("Event1");
        eventStatistic.incEvent("Event1");
        eventStatistic.incEvent("Event2");

        assertEquals(1. / 30, eventStatistic.getEventStatisticByName("Event1"));
    }

    @Test
    public void testOutdatedEvent() {
        eventStatistic.incEvent("Event");
        clock.plus(2, ChronoUnit.HOURS);

        assertEquals(0, eventStatistic.getEventStatisticByName("Event"));
    }

    @Test
    public void testAllStatistic() {
        eventStatistic.incEvent("Eating");
        clock.plus(10, ChronoUnit.MINUTES);

        eventStatistic.incEvent("Teeth brushing");
        clock.plus(45, ChronoUnit.MINUTES);

        eventStatistic.incEvent("Arrive to work");
        clock.plus(10, ChronoUnit.MINUTES);

        eventStatistic.incEvent("Eating");

        Map<String, Double> statistic = eventStatistic.getAllEventStatistic();
        Set<String> expected = Set.of("Teeth brushing", "Arrive to work", "Eating");
        assertEquals(expected, statistic.keySet());
        for (String name : expected) {
            assertEquals(1. / 60, statistic.get(name));
        }
    }

}
