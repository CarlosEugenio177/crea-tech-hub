import { useState } from 'react'
import { useLocation } from 'react-router'
import { ButtonLink } from '../../shared/ui/ButtonLink.tsx'
import { IllustratedPage } from '../../shared/ui/IllustratedPage.tsx'
import { Notice } from '../../shared/ui/Notice.tsx'
import { ProblemNotice } from '../../shared/ui/ProblemNotice.tsx'
import { QueryStatus } from '../../shared/ui/QueryStatus.tsx'
import illustration from './assets/documento.svg'
import { CancelReservationBox } from './CancelReservationBox.tsx'
import {
  type ReservationReceipt,
  useCancelReservation,
  useReservationReceipt,
} from './reservationApi.ts'
import { formatDate, formatHubInstant } from './reservationDates.ts'
import { RESERVE } from './reservationRoutes.ts'
import styles from './TrackingPage.module.css'

function headerCopy(data: ReservationReceipt | undefined) {
  if (!data) {
    return {
      title: 'Consultar pedido',
      description: 'Use o link individual que você recebeu no comprovante.',
    }
  }
  switch (data.status) {
    case 'CONFIRMED':
      return {
        title: 'Reserva confirmada',
        description:
          'Sua reserva está confirmada. Guarde o endereço desta página caso precise consultar ou cancelar.',
      }
    case 'REJECTED':
      return {
        title: 'Solicitação recusada',
        description:
          'Esta solicitação não pôde ser aprovada. Consulte os espaços disponíveis para escolher outro horário.',
      }
    case 'CANCELLED':
      return {
        title: 'Reserva cancelada',
        description: 'Esta reserva foi cancelada e o horário foi liberado.',
      }
    case 'PENDING':
      return {
        title: 'Pedido recebido',
        description:
          'A equipe ainda vai confirmar sua reserva. Guarde o endereço desta página para acompanhar ou cancelar o pedido.',
      }
  }
}

function statusLabel(data: ReservationReceipt) {
  switch (data.status) {
    case 'PENDING':
      return 'Pendente de análise'
    case 'CONFIRMED':
      return 'Confirmada'
    case 'REJECTED':
      return 'Recusada'
    case 'CANCELLED':
      return data.lateCancellation
        ? 'Cancelada (tardia — menos de 24 h)'
        : 'Cancelada'
  }
}

export function TrackingPage() {
  const token = useLocation().hash.slice(1)
  const [cancelledToken, setCancelledToken] = useState<string | null>(null)
  const receipt = useReservationReceipt(token)
  const cancellation = useCancelReservation(token, () =>
    setCancelledToken(token),
  )

  if (/^[A-Za-z0-9_-]{43}$/.test(token)) {
    const data = receipt.data
    const copy = headerCopy(data)
    const justCancelled =
      cancelledToken === token && data?.status === 'CANCELLED'

    return (
      <IllustratedPage
        eyebrow="Mútua · Sua solicitação"
        title={copy.title}
        description={copy.description}
        illustration={illustration}
        illustrationAlt="Pessoa ao lado de um documento"
      >
        {data ? (
          <>
            {justCancelled ? (
              <Notice tone="success">
                {data.lateCancellation
                  ? 'Reserva cancelada com menos de 24 horas de antecedência (registrada como tardia). O horário foi liberado.'
                  : 'Reserva cancelada com sucesso. O horário foi liberado.'}
              </Notice>
            ) : null}
            <dl className={styles.details}>
              <div className={styles.row}>
                <dt className={styles.term}>Protocolo</dt>
                <dd className={styles.value}>{data.protocol}</dd>
              </div>
              <div className={styles.row}>
                <dt className={styles.term}>Espaço</dt>
                <dd className={styles.value}>
                  {data.spaceName}
                  {data.stationNumber ? ` · Estação ${data.stationNumber}` : ''}
                </dd>
              </div>
              <div className={styles.row}>
                <dt className={styles.term}>Data e horário</dt>
                <dd className={styles.value}>
                  {formatDate(data.startsAt.slice(0, 10))} ·{' '}
                  {data.startsAt.slice(11, 16)} às {data.endsAt.slice(11, 16)}
                </dd>
              </div>
              <div className={styles.row}>
                <dt className={styles.term}>Situação</dt>
                <dd className={styles.value}>{statusLabel(data)}</dd>
              </div>
              {data.cancelledAt ? (
                <div className={styles.row}>
                  <dt className={styles.term}>Cancelada em</dt>
                  <dd className={styles.value}>
                    {formatHubInstant(data.cancelledAt)}
                  </dd>
                </div>
              ) : null}
            </dl>
            {data.cancellable ? (
              <CancelReservationBox
                startsAt={data.startsAt}
                lateCancellationWarning={data.lateCancellationWarning}
                isPending={cancellation.isPending}
                error={cancellation.error}
                onCancel={cancellation.cancel}
              />
            ) : (
              <ProblemNotice error={cancellation.error} />
            )}
          </>
        ) : (
          <QueryStatus error={receipt.error} />
        )}
        <ButtonLink to={RESERVE}>Voltar aos espaços</ButtonLink>
      </IllustratedPage>
    )
  }

  return (
    <IllustratedPage
      eyebrow="Mútua · Sua solicitação"
      title="Acompanhe pelo link individual"
      description="Abra o link individual mostrado no comprovante para consultar seu pedido."
      illustration={illustration}
      illustrationAlt="Pessoa ao lado de um documento"
    >
      <ButtonLink to={RESERVE}>Fazer uma solicitação</ButtonLink>
    </IllustratedPage>
  )
}
