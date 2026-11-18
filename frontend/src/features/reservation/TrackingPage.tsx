import { useLocation } from 'react-router'
import { ButtonLink } from '../../shared/ui/ButtonLink.tsx'
import { IllustratedPage } from '../../shared/ui/IllustratedPage.tsx'
import { QueryStatus } from '../../shared/ui/QueryStatus.tsx'
import illustration from './assets/documento.svg'
import { RESERVE } from './reservationRoutes.ts'
import { useReservationReceipt } from './reservationApi.ts'
import { formatDate } from './reservationDates.ts'

export function TrackingPage() {
  const token = useLocation().hash.slice(1)
  const receipt = useReservationReceipt(token)
  if (/^[A-Za-z0-9_-]{43}$/.test(token)) {
    const data = receipt.data
    return (
      <IllustratedPage
        eyebrow="Mútua · Sua solicitação"
        title={data ? 'Pedido recebido' : 'Consultar pedido'}
        description={
          data
            ? 'A equipe ainda vai confirmar sua reserva. Guarde o endereço desta página para consultar o pedido.'
            : 'Use o link individual que você recebeu no comprovante.'
        }
        illustration={illustration}
        illustrationAlt="Pessoa ao lado de um documento"
      >
        {data ? (
          <dl>
            <dt>Protocolo</dt>
            <dd>{data.protocol}</dd>
            <dt>Espaço</dt>
            <dd>
              {data.spaceName}
              {data.stationNumber ? ` · Estação ${data.stationNumber}` : ''}
            </dd>
            <dt>Data e horário</dt>
            <dd>
              {formatDate(data.startsAt.slice(0, 10))} ·{' '}
              {data.startsAt.slice(11, 16)} às {data.endsAt.slice(11, 16)}
            </dd>
            <dt>Situação</dt>
            <dd>Pendente de análise</dd>
          </dl>
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
