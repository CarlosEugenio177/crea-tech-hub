import { Navigate, useNavigate } from 'react-router'
import { Button } from '../../shared/ui/Button.tsx'
import { ProblemNotice } from '../../shared/ui/ProblemNotice.tsx'
import { useSubmitReservation } from './reservationApi.ts'
import { useReservationDraft } from './reservationDraft.ts'
import {
  RESERVE,
  RESERVE_REQUESTER,
  RESERVE_TIME,
  TRACKING,
} from './reservationRoutes.ts'
import { ReservationSummary } from './ReservationSummary.tsx'
import { StepActions } from './StepActions.tsx'
import { StepPage } from './StepPage.tsx'

export function ReviewPage() {
  const { draft } = useReservationDraft()
  const navigate = useNavigate()
  const submission = useSubmitReservation((token) => {
    void navigate(`${TRACKING}#${token}`, { replace: true })
  })
  const { space, period, requester, station } = draft

  if (!space || !period || !requester) {
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
        stationNumber={station?.number}
        period={period}
        requester={requester}
      />
      <ProblemNotice error={submission.error} />
      <StepActions
        backTo={
          submission.error?.status === 409 ? RESERVE_TIME : RESERVE_REQUESTER
        }
        backLabel={
          submission.error?.status === 409
            ? 'Escolher outro horário ou estação'
            : 'Voltar aos dados'
        }
      >
        <Button
          disabled={submission.isPending || submission.error?.status === 409}
          onClick={() =>
            submission.submit(space.id, station?.id, period, requester)
          }
        >
          {submission.isPending
            ? 'Enviando…'
            : 'Enviar solicitação para análise'}
        </Button>
      </StepActions>
    </StepPage>
  )
}
