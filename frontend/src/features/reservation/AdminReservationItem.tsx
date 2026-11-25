import {
  statusLabels,
  type AdminReservationEntry,
} from './adminReservationsApi.ts'
import styles from './AdminReservationPage.module.css'
import { formatDate } from './reservationDates.ts'

export function AdminReservationItem({
  entry,
}: {
  entry: AdminReservationEntry
}) {
  return (
    <li className={styles.item}>
      <h2>
        {entry.spaceName}
        {entry.stationNumber ? ` · Estação ${entry.stationNumber}` : ''}
      </h2>
      <p>
        {formatDate(entry.startsAt.slice(0, 10))} ·{' '}
        {entry.startsAt.slice(11, 16)} às {entry.endsAt.slice(11, 16)}
      </p>
      <p>
        <strong>{statusLabels[entry.status]}</strong>
        {entry.lateCancellation && ' · Cancelamento tardio'}
      </p>
      <p>{entry.fullName}</p>
      <details>
        <summary>Ver contato e detalhes</summary>
        <dl className={styles.details}>
          <dt>Protocolo</dt>
          <dd>{entry.protocol}</dd>
          <dt>E-mail</dt>
          <dd>{entry.email}</dd>
          <dt>WhatsApp</dt>
          <dd>{entry.whatsapp}</dd>
          {entry.reason && (
            <>
              <dt>Motivo</dt>
              <dd>{entry.reason}</dd>
            </>
          )}
          {entry.notes && (
            <>
              <dt>Observações</dt>
              <dd>{entry.notes}</dd>
            </>
          )}
        </dl>
      </details>
    </li>
  )
}
