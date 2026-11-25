import { useId } from 'react'
import { Button } from '../../shared/ui/Button.tsx'
import { useDayAgenda } from './availabilityApi.ts'
import styles from './DayAgenda.module.css'
import { formatDate, shiftDate, today } from './reservationDates.ts'
import type { ReservationPeriod } from './reservationDraft.ts'

type DayAgendaProps = {
  spaceId: string
  date: string
  onDateChange: (date: string) => void
  onChoose: (period: ReservationPeriod) => void
}

export function DayAgenda({
  spaceId,
  date,
  onDateChange,
  onChoose,
}: DayAgendaProps) {
  const id = useId()
  const agenda = useDayAgenda(spaceId, date)

  return (
    <section className={styles.agenda} aria-labelledby={id}>
      <div className={styles.header}>
        <h2 id={id}>{formatDate(date)}</h2>
        <div className={styles.navigation}>
          <Button
            variant="secondary"
            disabled={date <= today()}
            onClick={() => onDateChange(shiftDate(date, -1))}
          >
            Dia anterior
          </Button>
          <Button
            variant="secondary"
            disabled={date === '9999-12-31'}
            onClick={() => onDateChange(shiftDate(date, 1))}
          >
            Dia seguinte
          </Button>
        </div>
      </div>
      {agenda.isError ? (
        <p role="status">
          Agenda indisponível. Você ainda pode informar o horário abaixo.
        </p>
      ) : !agenda.data ? (
        <p role="status">Consultando horários…</p>
      ) : agenda.data.closed ? (
        <p role="status">O Hub está fechado neste dia.</p>
      ) : (
        <ul className={styles.periods} aria-label="Horários do dia">
          {agenda.data.periods.map(({ start, end, available }) => (
            <li key={start} className={styles.period}>
              {available ? (
                <button
                  type="button"
                  className={styles.free}
                  aria-label={`Usar horário das ${start} às ${end}`}
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
