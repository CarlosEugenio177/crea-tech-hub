import { Navigate, useNavigate } from 'react-router'
import { RequesterForm } from './RequesterForm.tsx'
import { useReservationDraft } from './reservationDraft.ts'
import { RESERVE_REVIEW, RESERVE_TIME } from './reservationRoutes.ts'
import { StepPage } from './StepPage.tsx'

export function RequesterPage() {
  const navigate = useNavigate()
  const { draft, update } = useReservationDraft()

  if (!draft.period) return <Navigate to={RESERVE_TIME} replace />

  return (
    <StepPage
      step={3}
      title="Seus dados"
      description="Informe quem está solicitando. Não é preciso criar conta."
    >
      <RequesterForm
        requester={draft.requester}
        onSubmit={(requester) => {
          update({ requester })
          void navigate(RESERVE_REVIEW)
        }}
      />
    </StepPage>
  )
}
