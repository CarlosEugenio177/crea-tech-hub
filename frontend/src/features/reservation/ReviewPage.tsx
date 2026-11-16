import { Navigate } from 'react-router'
import { Button } from '../../shared/ui/Button.tsx'
import { Notice } from '../../shared/ui/Notice.tsx'
import { useReservationDraft } from './reservationDraft.ts'
import { RESERVE, RESERVE_REQUESTER } from './reservationRoutes.ts'
import { ReservationSummary } from './ReservationSummary.tsx'
import { findSpaceOption } from './spaceOptions.ts'
import { StepActions } from './StepActions.tsx'
import { StepPage } from './StepPage.tsx'

export function ReviewPage() {
  const { draft } = useReservationDraft()
  const space = findSpaceOption(draft.spaceId)

  if (!space || !draft.period || !draft.requester) {
    return <Navigate to={RESERVE} replace />
  }

  return (
    <StepPage
      step={4}
      title="Revise sua solicitação"
      description="Confira os dados antes de enviar para a análise da equipe."
    >
      <ReservationSummary
        spaceName={space.name}
        period={draft.period}
        requester={draft.requester}
      />
      <Notice tone="info">
        O envio de solicitações ainda não está disponível. Quando estiver, você
        receberá um comprovante com protocolo e um link individual para
        acompanhar ou cancelar, e a reserva só será confirmada após a análise da
        equipe.
      </Notice>
      <StepActions backTo={RESERVE_REQUESTER} backLabel="Voltar aos dados">
        <Button disabled>Enviar solicitação para análise</Button>
      </StepActions>
    </StepPage>
  )
}
