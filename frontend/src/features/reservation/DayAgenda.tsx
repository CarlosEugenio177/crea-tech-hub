import { useId } from 'react'
import { CalendarStrip } from './CalendarStrip.tsx'
import { useDayAgenda } from './availabilityApi.ts'
import styles from './DayAgenda.module.css'
import type { ReservationPeriod } from './reservationDraft.ts'

type DayAgendaProps = {
  spaceId: string
  start: string
  end: string
  date: string
  onDateChange: (date: string) => void
  onChoose: (period: ReservationPeriod) => void
}

export function DayAgenda({
  spaceId,
  start: chosenStart,
  end: chosenEnd,
  date,
  onDateChange,
  onChoose,
}: DayAgendaProps) {
  const id = useId()
  const agenda = useDayAgenda(spaceId, date)

  return (
    <section className={styles.agenda} aria-labelledby={id}>
      <CalendarStrip date={date} onDateChange={onDateChange} />
      <h2 id={id}>Disponibilidade do dia</h2>
      {agenda.isError ? (
        <p role="status">Agenda indisponível. Use os seletores de horário.</p>
      ) : !agenda.data ? (
        <p role="status">Consultando horários…</p>
      ) : agenda.data.closed ? (
        <p role="status">O Hub está fechado neste dia.</p>
      ) : (
        <ul className={styles.periods} aria-label="Horários do dia">
          {agenda.data.periods.map(({ start, end, available }) => (
            <li key={start}>
              {available ? (
                <button
                  type="button"
                  className={styles.free}
                  aria-label={`Usar horário das ${start} às ${end}`}
                  aria-pressed={start === chosenStart && end === chosenEnd}
                  disabled={agenda.isFetching}
                  onClick={() => onChoose({ date, start, end })}
                >
                  <span>
                    {start} às {end}
                  </span>
                  <span>Livre</span>
                </button>
              ) : (
                <div className={styles.busy}>
                  <span>
                    {start} às {end}
                  </span>
                  <span>Indisponível</span>
                </div>
              )}
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
