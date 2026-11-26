import {
  statusLabels,
  type AdminReservationEntry,
} from './adminReservationsApi.ts'
import { AdminReservationAttendance } from './AdminReservationAttendance.tsx'
import styles from './AdminReservationPage.module.css'
import { AdminReservationRequesterBlock } from './AdminReservationRequesterBlock.tsx'
import { AdminReservationDecisionForm } from './AdminReservationDecisionForm.tsx'
import { formatDate } from './reservationDates.ts'

const emailStatusLabels: Record<
  NonNullable<AdminReservationEntry['emailDeliveryStatus']>,
  string
> = {
  QUEUED: 'Aguardando envio',
  ACCEPTED: 'Aceito pelo serviço de e-mail',
  FAILED: 'Falha no envio',
}

export function AdminReservationItem({
  entry,
}: {
  entry: AdminReservationEntry
}) {
  return (
    <li className={styles.item}>
      <div className={styles.row}>
        <div>
          <h2>
            {entry.spaceName}
            {entry.stationNumber ? ` · Estação ${entry.stationNumber}` : ''}
          </h2>
          <p className={styles.date}>
            {formatDate(entry.startsAt.slice(0, 10))} ·{' '}
            {entry.startsAt.slice(11, 16)} às {entry.endsAt.slice(11, 16)}
          </p>
          <p className={styles.requester}>{entry.fullName}</p>
        </div>
        <div className={styles.situation}>
          <span className={styles.badge} data-status={entry.status}>
            {statusLabels[entry.status]}
          </span>
          {entry.lateCancellation && <span>Cancelamento tardio</span>}
          {entry.noShow && <span>Falta registrada</span>}
        </div>
      </div>
      <details>
        <summary>Ver contato e detalhes</summary>
        <dl className={styles.details}>
          <dt>Protocolo</dt>
          <dd>{entry.protocol}</dd>
          <dt>Perfil informado</dt>
          <dd>
            {entry.profile === 'EXTERNAL' ? 'Público externo' : 'Mutualista'}
          </dd>
          <dt>E-mail</dt>
          <dd>{entry.email}</dd>
          {entry.emailDeliveryStatus && (
            <>
              <dt>Último aviso por e-mail</dt>
              <dd>{emailStatusLabels[entry.emailDeliveryStatus]}</dd>
            </>
          )}
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
        {!entry.noShow && (
          <AdminReservationDecisionForm
            protocol={entry.protocol}
            status={entry.status}
          />
        )}
        {entry.attendanceEditable && (
          <AdminReservationAttendance
            protocol={entry.protocol}
            noShow={entry.noShow}
          />
        )}
        {entry.noShow && entry.noShowCount >= 2 && (
          <AdminReservationRequesterBlock
            protocol={entry.protocol}
            count={entry.noShowCount}
            blockedUntil={entry.requesterBlockedUntil}
          />
        )}
      </details>
    </li>
  )
}
