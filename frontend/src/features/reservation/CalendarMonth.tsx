import styles from './CalendarStrip.module.css'
import { formatDate, shiftDate } from './reservationDates.ts'

const weekdays = ['seg', 'ter', 'qua', 'qui', 'sex', 'sáb', 'dom']

type CalendarMonthProps = {
  firstDay: string
  selectedDate: string
  firstAllowed: string
  onSelectDate: (date: string) => void
}

export function CalendarMonth({
  firstDay,
  selectedDate,
  firstAllowed,
  onSelectDate,
}: CalendarMonthProps) {
  const date = new Date(`${firstDay}T12:00:00Z`)
  const firstColumn = ((date.getUTCDay() + 6) % 7) + 1
  date.setUTCMonth(date.getUTCMonth() + 1, 0)
  const days = Array.from({ length: date.getUTCDate() }, (_, index) =>
    shiftDate(firstDay, index),
  )

  return (
    <div className={`${styles.days} ${styles.monthDays}`}>
      {weekdays.map((day) => (
        <span key={day} className={styles.weekday} aria-hidden="true">
          {day}
        </span>
      ))}
      {days.map((day, index) => (
        <button
          key={day}
          type="button"
          style={index === 0 ? { gridColumnStart: firstColumn } : undefined}
          aria-label={formatDate(day)}
          aria-pressed={day === selectedDate}
          aria-current={day === firstAllowed ? 'date' : undefined}
          disabled={day < firstAllowed}
          onClick={() => onSelectDate(day)}
        >
          <strong>{index + 1}</strong>
        </button>
      ))}
    </div>
  )
}
