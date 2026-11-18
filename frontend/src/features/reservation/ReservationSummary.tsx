import { PROFILE_LABELS, type Requester } from './requesterSchema.ts'
import { formatDate } from './reservationDates.ts'
import type { ReservationPeriod } from './reservationDraft.ts'
import styles from './ReservationSummary.module.css'

type ReservationSummaryProps = {
  spaceName: string
  stationNumber?: number
  period: ReservationPeriod
  requester: Requester
}

export function ReservationSummary({
  spaceName,
  stationNumber,
  period,
  requester,
}: ReservationSummaryProps) {
  const rows = [
    ['Espaço', spaceName],
    ['Estação', stationNumber?.toString() ?? ''],
    ['Data', formatDate(period.date)],
    ['Horário', `${period.start} às ${period.end}`],
    ['Solicitante', requester.fullName],
    ['Perfil', PROFILE_LABELS[requester.profile]],
    ['ID ou cartão da Mútua', requester.mutuaId],
    ['WhatsApp', requester.whatsapp],
    ['E-mail', requester.email],
    ['Motivo', requester.reason],
    ['Observação', requester.notes],
  ].filter(([, value]) => value !== '')

  return (
    <dl className={styles.summary}>
      {rows.map(([term, value]) => (
        <div key={term} className={styles.row}>
          <dt className={styles.term}>{term}</dt>
          <dd className={styles.value}>{value}</dd>
        </div>
      ))}
    </dl>
  )
}
