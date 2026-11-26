import { useId } from 'react'
import { Button } from '../../shared/ui/Button.tsx'
import type { DayAvailability, FreeWindow } from './availabilityApi.ts'
import { CalendarStrip } from './CalendarStrip.tsx'
import styles from './DayAgenda.module.css'

type DayAgendaProps = {
  data?: DayAvailability
  unavailable: boolean
  onRetry: () => void
  start: string
  end: string
  date: string
  onDateChange: (date: string) => void
  onChoose: (window: FreeWindow) => void
}

export function DayAgenda({
  data,
  unavailable,
  onRetry,
  start: chosenStart,
  end: chosenEnd,
  date,
  onDateChange,
  onChoose,
}: DayAgendaProps) {
  const id = useId()

  return (
    <section className={styles.agenda} aria-labelledby={id}>
      <CalendarStrip date={date} onDateChange={onDateChange} />
      <h2 id={id}>Disponibilidade do dia</h2>
      {unavailable ? (
        <>
          <p role="status">Não foi possível carregar a agenda.</p>
          <Button variant="secondary" onClick={onRetry}>
            Tentar novamente
          </Button>
        </>
      ) : !data ? (
        <p role="status">Consultando horários…</p>
      ) : data.closed ? (
        <p role="status">O Hub está fechado neste dia.</p>
      ) : (
        <ul className={styles.periods} aria-label="Horários do dia">
          {data.periods.map(({ start, end, available }) => (
            <li key={start}>
              {available ? (
                <button
                  type="button"
                  className={styles.free}
                  aria-label={`Usar horário das ${start} às ${end}`}
                  aria-pressed={start === chosenStart && end === chosenEnd}
                  onClick={() => onChoose({ start, end })}
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
