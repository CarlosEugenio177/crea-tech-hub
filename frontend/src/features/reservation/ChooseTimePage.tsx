import { Navigate, useNavigate } from 'react-router'
import { PeriodForm } from './PeriodForm.tsx'
import { useReservationDraft } from './reservationDraft.ts'
import { RESERVE, RESERVE_REQUESTER } from './reservationRoutes.ts'
import { StepPage } from './StepPage.tsx'

export function ChooseTimePage() {
  const navigate = useNavigate()
  const { draft, update } = useReservationDraft()

  if (!draft.spaceId || !draft.spaceName)
    return <Navigate to={RESERVE} replace />

  return (
    <StepPage
      step={2}
      title="Escolha uma data e um horário"
      description={`${draft.spaceName}. Informe início e fim com precisão de minutos.`}
    >
      <PeriodForm
        period={draft.period}
        onSubmit={(period) => {
          update({ period })
          void navigate(RESERVE_REQUESTER)
        }}
      />
    </StepPage>
  )
}
