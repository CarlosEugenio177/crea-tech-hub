import { useRef, useState } from 'react'
import { CalendarMonth } from './CalendarMonth.tsx'
import styles from './CalendarStrip.module.css'
import { formatDate, shiftDate, today } from './reservationDates.ts'

const weekday = new Intl.DateTimeFormat('pt-BR', {
  weekday: 'short',
  timeZone: 'UTC',
})
const month = new Intl.DateTimeFormat('pt-BR', {
  month: 'long',
  year: 'numeric',
  timeZone: 'UTC',
})
const LAST_DATE = '9999-12-31'

type CalendarStripProps = {
  date: string
  onDateChange: (date: string) => void
}

export function CalendarStrip({ date, onDateChange }: CalendarStripProps) {
  const [visibleMonth, setVisibleMonth] = useState<string>()
  const monthToggle = useRef<HTMLButtonElement>(null)
  const firstAllowed = today()
  const currentMonth = firstAllowed.slice(0, 7)
  const titleDate = new Date(`${visibleMonth ?? date}T12:00:00Z`)
  const selected = new Date(`${date}T12:00:00Z`)
  const monday = shiftDate(date, -((selected.getUTCDay() + 6) % 7))
  const firstVisible = monday < firstAllowed ? firstAllowed : monday
  const days = Array.from({ length: 7 }, (_, offset) =>
    shiftDate(firstVisible, offset),
  ).filter((day) => /^\d{4}-/.test(day))

  function move(direction: -1 | 1) {
    if (!visibleMonth) {
      onDateChange(shiftDate(date, direction))
      return
    }
    const next = new Date(`${visibleMonth}T12:00:00Z`)
    next.setUTCMonth(next.getUTCMonth() + direction)
    setVisibleMonth(next.toISOString().slice(0, 10))
  }

  function selectMonthDay(day: string) {
    onDateChange(day)
    setVisibleMonth(undefined)
    monthToggle.current?.focus()
  }

  return (
    <div className={styles.calendar} role="group" aria-label="Escolher dia">
      <div className={styles.header}>
        <strong aria-live="polite">{month.format(titleDate)}</strong>
        <div className={styles.navigation}>
          <button
            ref={monthToggle}
            type="button"
            className={styles.monthToggle}
            aria-expanded={Boolean(visibleMonth)}
            onClick={() =>
              setVisibleMonth(
                visibleMonth ? undefined : `${date.slice(0, 7)}-01`,
              )
            }
          >
            {visibleMonth ? 'Ver semana' : 'Ver mês'}
          </button>
          <button
            type="button"
            aria-label={visibleMonth ? 'Mês anterior' : 'Dia anterior'}
            disabled={
              visibleMonth
                ? visibleMonth.startsWith(currentMonth)
                : date <= firstAllowed
            }
            onClick={() => move(-1)}
          >
            <span className={styles.previous} aria-hidden="true" />
          </button>
          <button
            type="button"
            aria-label={visibleMonth ? 'Mês seguinte' : 'Dia seguinte'}
            disabled={
              visibleMonth ? visibleMonth === '9999-12-01' : date === LAST_DATE
            }
            onClick={() => move(1)}
          >
            <span className={styles.next} aria-hidden="true" />
          </button>
        </div>
      </div>
      {visibleMonth ? (
        <CalendarMonth
          firstDay={visibleMonth}
          selectedDate={date}
          firstAllowed={firstAllowed}
          onSelectDate={selectMonthDay}
        />
      ) : (
        <div className={styles.days}>
          {days.map((day) => (
            <button
              key={day}
              type="button"
              aria-label={formatDate(day)}
              aria-pressed={day === date}
              disabled={day < firstAllowed}
              onClick={() => onDateChange(day)}
            >
              <span>
                {weekday.format(new Date(`${day}T12:00:00Z`)).slice(0, 3)}
              </span>
              <strong>{day.slice(-2)}</strong>
            </button>
          ))}
        </div>
      )}
    </div>
  )
}
