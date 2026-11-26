package br.com.mutua.createchhub.space;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class SpaceDayTimeline {
  private SpaceDayTimeline() {}

  static List<Window> freeWindows(
      LocalDateTime start, LocalDateTime end, List<SpaceBusyIntervals.Interval> busy) {
    var free = new ArrayList<Window>();
    if (!start.isBefore(end)) return free;

    var ordered =
        busy.stream().sorted(Comparator.comparing(SpaceBusyIntervals.Interval::startsAt)).toList();
    var cursor = start;
    for (var interval : ordered) {
      if (!interval.endsAt().isAfter(cursor)) continue;
      if (interval.startsAt().isAfter(cursor)) {
        free.add(new Window(cursor, interval.startsAt()));
      }
      cursor = interval.endsAt();
    }
    if (cursor.isBefore(end)) free.add(new Window(cursor, end));
    return free;
  }

  static List<SpaceDayAgenda.Period> periods(
      LocalDateTime open, LocalDateTime close, List<Window> windows) {
    var free = windows.stream().sorted(Comparator.comparing(Window::start)).toList();
    var periods = new ArrayList<SpaceDayAgenda.Period>();
    var cursor = open;
    int next = 0;
    while (cursor.isBefore(close)) {
      var until = cursor;
      while (next < free.size() && !free.get(next).start().isAfter(cursor)) {
        var candidate = free.get(next++).end();
        if (candidate.isAfter(until)) until = candidate;
      }
      boolean available = until.isAfter(cursor);
      if (!available) until = next < free.size() ? free.get(next).start() : close;
      periods.add(
          new SpaceDayAgenda.Period(
              cursor.toLocalTime().toString(), until.toLocalTime().toString(), available));
      cursor = until;
    }
    return periods;
  }

  record Window(LocalDateTime start, LocalDateTime end) {
    SpaceDayAgenda.FreeWindow toResponse() {
      return new SpaceDayAgenda.FreeWindow(
          start.toLocalTime().toString(), end.toLocalTime().toString());
    }
  }
}
